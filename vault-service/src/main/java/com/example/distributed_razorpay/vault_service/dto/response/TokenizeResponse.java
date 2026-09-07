package com.example.distributed_razorpay.vault_service.dto.response;


import com.example.distributed_razorpay.common_lib.enums.CardBrand;

public record TokenizeResponse(

            String token,
            String lastFour,
            CardBrand cardBrand,
            Integer expiryMonth,
            Integer expiryYear
) {


}
