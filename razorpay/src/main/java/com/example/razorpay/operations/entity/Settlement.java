package com.example.razorpay.operations.entity;

import com.example.razorpay.common.entity.BaseEntity;
import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.SettlementStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "settlement")
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits" , column = @Column(name = "gross_amount_units",nullable = false)),
            @AttributeOverride(name = "currency",column = @Column(name = "gross_amount_currency",nullable = false))
    })
    private Money grossAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits" , column = @Column(name = "refund_amount_units",nullable = false)),
            @AttributeOverride(name = "currency",column = @Column(name = "refund_amount_currency",nullable = false))
    })
    private Money refundAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits" , column = @Column(name = "fee_amount_units",nullable = false)),
            @AttributeOverride(name = "currency",column = @Column(name = "fee_amount_currency",nullable = false))
    })
    private Money feeAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits" , column = @Column(name = "gst_amount_units",nullable = false)),
            @AttributeOverride(name = "currency",column = @Column(name = "gst_amount_currency",nullable = false))
    })
    private Money gstAmount;

    @AttributeOverrides({
            @AttributeOverride(name = "amountUnits" , column = @Column(name = "net_amount_units",nullable = false)),
            @AttributeOverride(name = "currency",column = @Column(name = "net_amount_currency",nullable = false))
    })
    private Money netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false , length = 20)
    private SettlementStatus status;

    @Column(nullable = false , length = 50)
    private String bankReference;

    private LocalDateTime processedAt;

}
