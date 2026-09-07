package com.example.distributed_razorpay.common_lib.dto;

import java.util.UUID;

public record PaymentSettlementView(

        UUID paymentId,
        int amountUnits,
        int refundedAmountUnits,
        String currency
) {

}
