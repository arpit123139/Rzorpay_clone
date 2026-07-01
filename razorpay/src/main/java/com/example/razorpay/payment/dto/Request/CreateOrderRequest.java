package com.example.razorpay.payment.dto.Request;

import com.example.razorpay.common.entity.Money;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public record CreateOrderRequest(

        @NotNull(message = "Amount is required")
        Money amount,
        //Receipt -- When the user clicks on pay now the merchant backend may also create order thet will remain to his backend only and it will pass this order_id to razorpay in the receipt field
        //The razorpay backend will check if this receipt exsist in the order if exsist than it means it is a duplicated request an order has been previously created with this receipt number so do not create it again return the previous order_id

        @Size(max=100)
        String receipt, // order-d (known to merchant)

        //They give information such as user_phone , item_id etc...
        Map<String,Object> notes,

        LocalDateTime expiresAt
) {
}
