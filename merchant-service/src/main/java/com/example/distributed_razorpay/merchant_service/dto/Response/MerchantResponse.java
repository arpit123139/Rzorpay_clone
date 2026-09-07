package com.example.distributed_razorpay.merchant_service.dto.Response;


import com.example.distributed_razorpay.common_lib.enums.BusinessType;
import com.example.distributed_razorpay.common_lib.enums.MerchantStatus;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MerchantResponse(

        UUID id,
        String name,
        String email,
        String buisnessName,
        BusinessType buisnessType,
        MerchantStatus merchantStatus
) {
}
