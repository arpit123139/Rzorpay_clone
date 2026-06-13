package com.example.razorpay.merchant.entity;

import com.example.razorpay.common.enums.BusinessType;
import com.example.razorpay.common.enums.MerchantStatus;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "merchant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

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
