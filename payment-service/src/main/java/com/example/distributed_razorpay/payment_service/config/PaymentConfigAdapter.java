package com.example.distributed_razorpay.payment_service.config;


import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;
import com.example.distributed_razorpay.payment_service.gateway.PaymentAdapter;
import com.example.distributed_razorpay.payment_service.gateway.adapter.CardPaymentAdapter;
import com.example.distributed_razorpay.payment_service.gateway.adapter.NetBankingAdapter;
import com.example.distributed_razorpay.payment_service.gateway.adapter.UpiPaymentAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentConfigAdapter {

    private final NetBankingAdapter netBankingAdapter;
    private final UpiPaymentAdapter upiPaymentAdapter;
    private final CardPaymentAdapter cardPaymentAdapter;

    @Bean
    public Map<PaymentMethod, PaymentAdapter> paymentAdapter(){
        return Map.of(PaymentMethod.CARD,cardPaymentAdapter,
                PaymentMethod.NETBANKING,netBankingAdapter,
                PaymentMethod.UPI,upiPaymentAdapter);
    }
}
