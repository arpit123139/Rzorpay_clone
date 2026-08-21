package com.example.razorpay.operations.entity;


import com.example.razorpay.common.entity.BaseEntity;
import com.example.razorpay.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

@Entity
@Table(name = "settlement_payment")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
public class SettlementPayment extends  BaseEntity {

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
