package com.example.razorpay.payment.config;

import com.example.razorpay.common.enums.PaymentMethod;
import com.example.razorpay.payment.gateway.adapter.CardPaymentAdapter;
import com.example.razorpay.payment.processor.PaymentProcessor;
import com.example.razorpay.payment.processor.startegy.CardPaymentProcessor;
import com.example.razorpay.payment.processor.startegy.NetBankingPaymentProcessor;
import com.example.razorpay.payment.processor.startegy.UpiPaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.smartcardio.Card;
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
