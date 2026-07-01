package com.example.razorpay.payment.entity;

import com.example.razorpay.common.entity.BaseEntity;
import com.example.razorpay.common.entity.Money;
import com.example.razorpay.common.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_record",indexes = {
        @Index(name = "idx_order_id_merchant_id",columnList = "id, merchant_id"),
        @Index(name = "idx_order_merchant_id",columnList = "merchant_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    private Money amount;

    @Column(name = "merchant_id",nullable = false)
    private UUID merchantId;

    //TODO: Need to Note
    @Column(length = 100)
    private String receipt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private OrderStatus orderStatus=OrderStatus.CREATED;

    @Column(nullable = false)
    @Builder.Default
    private int attempts=0;

    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode((SqlTypes.JSON))
    private Map<String,Object> notes;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


}
