package com.example.razorpay.operations.settlement;

import com.example.razorpay.common.dto.SettlementBankDetails;
import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.EventAggregateType;
import com.example.razorpay.common.enums.SettlementStatus;
import com.example.razorpay.common.exceptions.BankTransfferException;
import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.merchant.api.MerchantLookupService;
import com.example.razorpay.operations.entity.Settlement;
import com.example.razorpay.operations.entity.SettlementPayment;
import com.example.razorpay.operations.entity.SettlementPaymentId;
import com.example.razorpay.operations.settlement.dto.BankTransfferResult;
import com.example.razorpay.payment.api.PaymentLookupService;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.operations.repository.SettlementRepository;
import com.example.razorpay.operations.repository.SettlementPaymentRepository;
import com.example.razorpay.payment.outbox.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementTransactionExecutor {

    private final PaymentLookupService paymentLookupService;
    private final SettlementRepository settlementRepository;
    private final SettlementPaymentRepository settllementPaymentRepository;
    private final MerchantLookupService merchantLookupService;
    private final BankTransfferProcessor bankTransfferProcessor;
    // TODO: Publisher inside it's own DB
    private final OutboxEventPublisher outboxEventPublisher;

    private final double FEE_RATE = 0.02;
    private final double GST_RATE = 0.18;

    @Transactional
    public void processForMerchant(UUID merchantId , LocalDate settlementDate){

            List<Payment> unsettledPayment = paymentLookupService.findUnsettledCapturedPayments(merchantId);
            if(unsettledPayment.isEmpty()) return;

            //Calculate the total payment need to be settlesd by deducting the gateway fees

            Money gross  = unsettledPayment.stream().map(payment -> payment.getAmount() ).reduce((money, money2) -> money.add(money2)).orElseThrow();

            log.info("Gross Amount to be settled for Merchant {} is  {}",merchantId,gross);

            //Payment Gateway fee
            int fee = (int)Math.round(gross.getAmountUnits() * FEE_RATE);
            int gst = (int)Math.round(fee * GST_RATE);

            Money feeAmount = Money.of(fee, gross.getCurrency());
            Money gstAmount = Money.of(gst , gross.getCurrency());

            Money netAmount = gross.subtract(feeAmount).subtract(gstAmount);

            log.info("Net Amount After deducting Payment Gateway fee and GST to be settled for Merchant {} is  {}",merchantId,netAmount.getAmountUnits());

            Settlement settlement = Settlement.builder()
                    .merchantId(merchantId)
                    .grossAmount(gross)
                    .feeAmount(feeAmount)
                    .gstAmount(gstAmount)
                    .netAmount(netAmount)
                    .status(SettlementStatus.INITIATED)
                    .processedAt(LocalDateTime.now())
                    .build();

            settlementRepository.save(settlement);

        List<SettlementPayment> links = new ArrayList<>();

        for(Payment p: unsettledPayment){
            links.add(SettlementPayment.builder()
                    .id(new SettlementPaymentId(settlement.getId(),p.getId()))
                    .settlement(settlement)
                    .payment(p)
                    .build());
        }
        settllementPaymentRepository.saveAll(links);

            // This method has moved to a different transaction because:
            // Suppose if the bank transffer call succeded and we set the status to TRANFFER_PENDING but the last save call to the DB fails and the transaction roll back if we do it in a same transaction
            // then the SettleObject with status INITITATED is also not saved in the DB leaving no way to track this settlement if we do these 2 operation in a single transaction
            // Having multiple transaction at least save the Settlement Object with INITIATED status even if the bankTransffer is successfull and we are not able to mark the status as TRANSFFER_PENDING then also
            // we can have a reconcilation service that takes all the PENDING/FAILED Settlement and do a bankTransffer call with a idempotency key = SettlementId if the transffercall was success for this idempodentcy key return the bank reference
            // if that was a failure then retry the request
            initiateBankTransffer(settlement,unsettledPayment,merchantId,netAmount);



    }

    @Transactional
    private void initiateBankTransffer(Settlement settlement,List<Payment> unsettledPayment,UUID merchantId,Money netAmount) {
        try {


            SettlementBankDetails settlementBankDetails = merchantLookupService.getSettlementBankDetail(merchantId);

            // call the Bank transffer service to transffer the net amount to the merchant bank account from payment gateway nodal account

            BankTransfferResult bankTransfferResult= bankTransfferProcessor.initiate(settlement.getId() , merchantId , netAmount , settlementBankDetails.accountNumber() , settlementBankDetails.ifsc());

            settlement.setStatus(SettlementStatus.TRANSFER_PENDING);
            settlement.setBankReference(bankTransfferResult.registrationRef());


        } catch (BankTransfferException e) {

            settlement.setStatus(SettlementStatus.FAILED);
            log.error("Settlement {} for the Merchant {} failed",settlement.getId(),merchantId);
        }
        // Suppose here the save fails and the transaction rollBacks this means
        settlementRepository.save(settlement);
    }

    @Transactional
    public void resolveTransfer(UUID settlementId , String errorCode , String errorDescription){

        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow(()-> new ResourceNotFoundException("Settlement ,",settlementId));

        if(settlement.getStatus() != SettlementStatus.TRANSFER_PENDING){
            log.info("Settlement resolved , skipping for id :{}",settlement.getId());
            return;
        }

        if(errorCode == null) //success
        {
            settlement.setStatus(SettlementStatus.PROCESSED);
            settlement.setProcessedAt(LocalDateTime.now());
            settlementRepository.save(settlement);
            log.info("Settlemnt Processed Successfully , settlementId :{}",settlementId);
            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT,settlementId,"SETTLEMENT_PROCESSED", Map.of(
                    "settlementId",settlementId,
                    "merchantId",settlement.getMerchantId(),
                    "stattus",settlement.getStatus().name(),
                    "settlementAmount",settlement.getNetAmount(),
                    "settlementCurrency",settlement.getNetAmount().getCurrency()
            ));
        }
        else{ // failes
            settlement.setStatus(SettlementStatus.FAILED);
            settlement.setFailureReason(errorCode+" "+errorDescription);
            log.warn("Settlement  Failed , settlementId :{} with error {}",settlementId,errorDescription);
            outboxEventPublisher.publish(EventAggregateType.SETTLEMENT,settlementId,"SETTLEMENT_FAILED", Map.of(
                    "settlementId",settlementId,
                    "merchantId",settlement.getMerchantId(),
                    "stattus",settlement.getStatus().name(),
                    "settlementAmount",settlement.getNetAmount(),
                    "settlementCurrency",settlement.getNetAmount().getCurrency()
            ));
            settlementRepository.save(settlement);
        }
    }
}
