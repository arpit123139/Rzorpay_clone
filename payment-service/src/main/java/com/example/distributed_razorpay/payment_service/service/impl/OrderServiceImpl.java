package com.example.distributed_razorpay.payment_service.service.impl;


import com.example.distributed_razorpay.common_lib.dto.FindOrCreateCustomerRequest;
import com.example.distributed_razorpay.common_lib.enums.EventAggregateType;
import com.example.distributed_razorpay.common_lib.enums.OrderStatus;
import com.example.distributed_razorpay.common_lib.exceptions.BuisnessRuleViolationException;
import com.example.distributed_razorpay.common_lib.exceptions.DuplicateResourceException;
import com.example.distributed_razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.example.distributed_razorpay.payment_service.client.CustomerServiceClient;
import com.example.distributed_razorpay.payment_service.dto.Request.CreateOrderRequest;
import com.example.distributed_razorpay.payment_service.dto.Response.OrderResponse;
import com.example.distributed_razorpay.payment_service.dto.Response.PaymentResponse;
import com.example.distributed_razorpay.payment_service.entity.OrderRecord;
import com.example.distributed_razorpay.payment_service.entity.Payment;
import com.example.distributed_razorpay.payment_service.mapper.OrderMapper;
import com.example.distributed_razorpay.payment_service.mapper.PaymentMapper;
import com.example.distributed_razorpay.payment_service.outbox.OutboxEventPublisher;
import com.example.distributed_razorpay.payment_service.repository.OrderRepository;
import com.example.distributed_razorpay.payment_service.repository.PaymentRepository;
import com.example.distributed_razorpay.payment_service.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final CustomerServiceClient customerServiceClient;
    private final OutboxEventPublisher eventPublisher;


    @Value("${payment.order.default-order-expiry-minutes:30}")
    private int defaultOrderExpiryMinutes;


    @Override
    @Transactional
    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")
    public OrderResponse create(UUID merchantId, CreateOrderRequest request) {

        if( request.receipt()!=null && orderRepository.existsByMerchantIdAndReceipt(merchantId,request.receipt())){
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE","Order with receipt already exsist: "+request.receipt());
        }

        UUID customerId = null;
        if(request.customer()!=null){
            customerId = customerServiceClient.findOrCreate(
                    new FindOrCreateCustomerRequest(
                    merchantId,
                    request.customer().email(),
                    request.customer().name(),
                    request.customer().phone())
            );
        }

        OrderRecord order=OrderRecord.builder()
                .receipt(request.receipt())
                .amount(request.amount())
                .notes(request.notes())
                .merchantId(merchantId)
                .orderStatus(OrderStatus.CREATED)
                .customerId(customerId)
                .expiresAt(request.expiresAt()!=null ? request.expiresAt() : LocalDateTime.now().plusMinutes(defaultOrderExpiryMinutes))
                .build();

        order=orderRepository.save(order);


        eventPublisher.publish(EventAggregateType.ORDER,order.getId(),"ORDER_CREATED",
                Map.of("orderId",order.getId().toString(),
                        "merchantId",merchantId.toString(),
                        "OrderStatus",order.getOrderStatus().name(),
                        "amountUnits",order.getAmount().getAmountUnits(),
                        "amountCurrency",order.getAmount().getCurrency())
                );


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

        eventPublisher.publish(EventAggregateType.ORDER,orderRecord.getId(),"ORDER_CANCELLED",
                Map.of("orderId",orderRecord.getId().toString(),
                        "merchantId",merchantId.toString(),
                        "OrderStatus",orderRecord.getOrderStatus().name(),
                        "amountUnits",orderRecord.getAmount().getAmountUnits(),
                        "amountCurrency",orderRecord.getAmount().getCurrency())
        );

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
