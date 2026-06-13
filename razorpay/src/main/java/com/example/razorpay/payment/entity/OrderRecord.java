package com.example.razorpay.payment.entity;

import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.OrderStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_record")
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private Money amount;

    @Column(name = "merchant_id",nullable = false)
    private UUID merchantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private OrderStatus orderStatus=OrderStatus.CREATED;

    @Column(nullable = false)
    private int attempts;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode((SqlTypes.JSON))
    private Map<String,Object> noted;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


}
