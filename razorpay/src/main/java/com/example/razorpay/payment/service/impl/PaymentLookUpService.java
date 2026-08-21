package com.example.razorpay.payment.service.impl;

import com.example.razorpay.common.enums.PaymentStatus;
import com.example.razorpay.payment.api.PaymentLookupService;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentLookUpService implements PaymentLookupService {
    private final PaymentRepository paymentRepository;

    @Override
    public List<Payment> findUnsettledCapturedPayments(UUID merchantId) {
        return paymentRepository.findByMerchantIdAndStatusForUpdate(merchantId, PaymentStatus.CAPTURED);

    }


}
