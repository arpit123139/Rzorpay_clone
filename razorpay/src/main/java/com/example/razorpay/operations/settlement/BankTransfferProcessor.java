package com.example.razorpay.operations.settlement;


import com.example.razorpay.common.entity.Money;
import com.example.razorpay.operations.settlement.dto.BankTransfferResult;

import java.util.UUID;

public interface BankTransfferProcessor {

    public BankTransfferResult initiate(UUID settlementId , UUID merchantId , Money amount , String bankAccount , String ifsc);
}
