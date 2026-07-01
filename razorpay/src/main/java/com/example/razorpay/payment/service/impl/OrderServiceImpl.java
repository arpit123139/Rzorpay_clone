package com.example.razorpay.payment.service.impl;

import com.example.razorpay.common.enums.OrderStatus;
import com.example.razorpay.common.exceptions.BuisnessRuleViolationException;
import com.example.razorpay.common.exceptions.DuplicateResourceException;
import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.payment.mapper.OrderMapper;
import com.example.razorpay.payment.dto.Request.CreateOrderRequest;
import com.example.razorpay.payment.dto.Response.OrderResponse;
import com.example.razorpay.payment.dto.Response.PaymentResponse;
import com.example.razorpay.payment.entity.OrderRecord;
import com.example.razorpay.payment.entity.Payment;
import com.example.razorpay.payment.mapper.PaymentMapper;
import com.example.razorpay.payment.repository.OrderRepository;
import com.example.razorpay.payment.repository.PaymentRepository;
import com.example.razorpay.payment.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// TOD0:Need to check what it is
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;


    @Override
    @Transactional
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {

        if( request.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE","Order with receipt already exsist: "+request.receipt());
        }

        OrderRecord order=OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .expiresAt(request.expiresAt()!=null ? request.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order=orderRepository.save(order);

        //  TODO: Send kafka event that order is created

      return orderMapper.toOrderResponse(order);


    }

    @Override
    public OrderResponse getById(UUID merchantId, UUID orderId) {
        //Merchant should fetch only his order that is the reason we need to filter for merchantId as well
        OrderRecord orderRecord=
                orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(()->new ResourceNotFoundException(
                "Order",orderId));

        return orderMapper.toOrderResponse(orderRecord);

    }

    @Override
    @Transactional
    public OrderResponse cancel(UUID merchantId, UUID orderId) {
        OrderRecord orderRecord=
                orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(()->new ResourceNotFoundException("Order",orderId));

        if(orderRecord.getOrderStatus()==OrderStatus.PAID || orderRecord.getOrderStatus()==OrderStatus.CANCELLED)
            throw new BuisnessRuleViolationException("ORDER_CANNOT_CANCELLED",
                    "Cannot cancel order with status: "+orderRecord.getOrderStatus().name());

        orderRecord.setOrderStatus(OrderStatus.CANCELLED);
        orderRecord= orderRepository.save(orderRecord);

        return orderMapper.toOrderResponse(orderRecord);

    }

    @Override
    public List<PaymentResponse> listPayment(UUID merchantId, UUID orderId) {
        OrderRecord orderRecord=
                orderRepository.findByIdAndMerchantId(orderId,merchantId).orElseThrow(()->new ResourceNotFoundException("Order",orderId));

        List<Payment>paymentList=paymentRepository.findByOrder_Id(orderId);
        return paymentList.stream().map(payment -> paymentMapper.toPaymentResponse(payment)).collect(Collectors.toList());

    }


}
