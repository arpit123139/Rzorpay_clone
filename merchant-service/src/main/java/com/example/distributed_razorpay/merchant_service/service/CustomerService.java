package com.example.distributed_razorpay.merchant_service.service;

import java.util.UUID;

public interface CustomerService {

    UUID findorCreate(UUID merchantId , String email,String name,String phone);
}
