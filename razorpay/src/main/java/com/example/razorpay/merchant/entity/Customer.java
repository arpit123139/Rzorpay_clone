package com.example.razorpay.merchant.entity;

import com.example.razorpay.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer",indexes = {
        @Index(name = "idx_customer_merchant_id",columnList = "merchant_id"),
        @Index(name = "idx_customer_email",columnList = "email")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false , length = 200)
    private String name;

    @Column(nullable = false ,length = 20)
    private String contactNumber;


    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "merchant_id" , nullable = false)
    private Merchant merchant;

    @Column(length = 200)
    private String email;


    private LocalDateTime deletedAt;

}
