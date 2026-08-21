package com.example.razorpay.operations.settlement;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.exceptions.BankTransfferException;
import com.example.razorpay.common.utils.RandomizerUtil;
import com.example.razorpay.operations.settlement.dto.BankTransfferResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class BankTransfferProcessorImpl implements  BankTransfferProcessor{


    @Override
    public BankTransfferResult initiate(UUID settlementId, UUID merchantId, Money amount, String bankAccount, String ifsc) {

        try {
            String registrationRef = "TXN_"+ RandomizerUtil.randomBase64(12);

            log.debug("Bank Transffer Call ");
            return new BankTransfferResult(registrationRef);
        } catch (Exception e) {
            throw new BankTransfferException("BANK_TRANSFFER_FAILS",e.getMessage());
        }
    }
}
