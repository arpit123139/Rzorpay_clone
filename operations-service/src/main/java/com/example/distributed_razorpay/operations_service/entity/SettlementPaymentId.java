package com.example.distributed_razorpay.operations_service.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
public class SettlementPaymentId{
    private UUID settlementId;

    private UUID paymentId;

}
