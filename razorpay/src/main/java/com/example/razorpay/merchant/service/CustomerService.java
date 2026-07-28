package com.example.razorpay.merchant.service;

import com.example.razorpay.merchant.entity.Merchant;

import java.util.UUID;

public interface CustomerService {

    UUID findorCreate(UUID merchantId , String email,String name,String phone);
}
