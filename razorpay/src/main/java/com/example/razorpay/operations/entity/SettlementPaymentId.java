package com.example.razorpay.operations.entity;

import com.example.razorpay.common.entity.BaseEntity;
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
