package com.example.distributed_razorpay.common_lib.dto;

public record SettlementBankDetails(
        String accountNumber,
        String ifsc,
        String accountHolderName
) {
}
