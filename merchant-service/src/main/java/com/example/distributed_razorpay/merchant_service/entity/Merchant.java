package com.example.distributed_razorpay.merchant_service.entity;

import com.example.distributed_razorpay.common_lib.entity.BaseEntity;
import com.example.distributed_razorpay.common_lib.enums.BusinessType;
import com.example.distributed_razorpay.common_lib.enums.MerchantStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "merchant",indexes = {
        @Index(name = "idx_merchant_status",columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false,length = 200)
    private String name;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(length = 20)
    private String contactNumber;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private BusinessType buisnessType;

    @Column(length = 50)
    private String buisnessName;

    @Column(length = 200)
    private String websiteUrl;

    @Column(nullable = false , length = 20)
    @Enumerated(EnumType.STRING)
    private MerchantStatus status=MerchantStatus.PENDING_KYC;

    @Column(length = 20)
    private String gstId;

    @Column(length = 20)
    private String panId;

    @Column(length = 20)
    private String settlementBankAccount;

    @Column(length = 20)
    private String settlementBankIfsc;

    @Column(length = 20)
    private String settlementBankAccountHolderName;


}
