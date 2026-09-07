package com.example.distributed_razorpay.payment_service.config;


import com.example.distributed_razorpay.common_lib.enums.PaymentMethod;
import com.example.distributed_razorpay.payment_service.processor.PaymentProcessor;
import com.example.distributed_razorpay.payment_service.processor.startegy.CardPaymentProcessor;
import com.example.distributed_razorpay.payment_service.processor.startegy.NetBankingPaymentProcessor;
import com.example.distributed_razorpay.payment_service.processor.startegy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PaymentProcessorConfig {

    private final CardPaymentProcessor cardPaymentProcessor;
    private final UpiPaymentProcessor upiPaymentProcessor;
    private final NetBankingPaymentProcessor netBankingPaymentProcessor;
    @Bean
    public Map<PaymentMethod, PaymentProcessor> paymentProcessorMap(){
        return Map.of(PaymentMethod.CARD,cardPaymentProcessor,
                PaymentMethod.NETBANKING,netBankingPaymentProcessor,
                PaymentMethod.UPI,upiPaymentProcessor);
    }
}
