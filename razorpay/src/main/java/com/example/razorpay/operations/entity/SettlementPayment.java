package com.example.razorpay.operations.entity;


import com.example.razorpay.payment.entity.Payment;
import jakarta.persistence.*;

@Entity
@Table(name = "settlement_payment")
public class SettlementPayment {

    @EmbeddedId
    private SettlementPaymentId id;

    @MapsId("settlementId")
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "settlement_id")
    private Settlement settlement;

    @MapsId("paymentId")
    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "payment_id")
    private Payment payment;
}
