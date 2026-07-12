package com.example.razorpay.payment.entity;

import com.example.razorpay.common.entity.BaseEntity;
import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.PaymentMethod;
import com.example.razorpay.common.enums.PaymentStatus;
import com.example.razorpay.merchant.entity.Merchant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payment",indexes = {
        @Index(name = "idx_payment_order_id",columnList = "order_id"),
        @Index(name = "idx_payment_merchant_id",columnList = "merchant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name ="order_id" , nullable = false)
    private OrderRecord order;

    @Column(name = "merchant_id",nullable = false)
    private UUID merchantId;

    @Embedded
    private Money amount;

    @Column(nullable = false,length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode((SqlTypes.JSON))
    private Map<String,Object> methodDetails;

    @Column(length = 100)
    private String bankReference;

    @Column(length = 100)
    private String processorReference;

    @Column(length = 100)
    private String errorCode;

    @Column(length = 300)
    private String errorDescription;

    private LocalDateTime authorizedAt;

    private LocalDateTime capturedAt;

    private LocalDateTime failedAt;

    private LocalDateTime refundedAt;

    private LocalDateTime settledAt;




}
