package com.example.razorpay.merchant.dto.Response;

import com.example.razorpay.common.enums.BusinessType;
import com.example.razorpay.common.enums.MerchantStatus;
import lombok.Data;

import java.util.UUID;

public record MerchantResponse(

        UUID id,
        String name,
        String email,
        String buisnessName,
        BusinessType buisnessType,
        MerchantStatus merchantStatus
) {
}
