package com.example.distributed_razorpay.payment_service.service.impl;


import com.example.distributed_razorpay.common_lib.dto.PaymentSettlementView;
import com.example.distributed_razorpay.common_lib.enums.PaymentStatus;
import com.example.distributed_razorpay.payment_service.api.PaymentLookupService;
import com.example.distributed_razorpay.payment_service.entity.Payment;
import com.example.distributed_razorpay.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookUpService implements PaymentLookupService {
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public List<PaymentSettlementView> findUnsettledCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED).stream().map(payment -> new PaymentSettlementView(payment.getId(),payment.getAmount().getAmountUnits(),0,payment.getAmount().getCurrency())).toList();

    }

    @Override
    @Transactional
    public void markSettled(List<UUID> paymentIds) {

        LocalDateTime now =LocalDateTime.now();
        List<Payment> payments = paymentRepository.findAllById(paymentIds);

        for(Payment payment:payments){
            payment.setSettledAt(now);
            payment.setStatus(PaymentStatus.SETTLED);
        }

        paymentRepository.saveAll(payments);
    }


}
