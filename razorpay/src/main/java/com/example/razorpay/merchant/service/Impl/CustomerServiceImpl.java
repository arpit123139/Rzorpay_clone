package com.example.razorpay.merchant.service.Impl;

import com.example.razorpay.merchant.entity.Customer;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.repository.CustomerRepository;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final MerchantRepository merchantRepository;

    @Override
    public UUID findorCreate(UUID merchantId, String email, String name, String phone) {

        if(email == null || email.isBlank()){
            return  null;
        }
        return customerRepository.findByMerchant_IdAndEmail(merchantId,email)
                .map(customer -> customer.getId())
                .orElseGet(()->createNew(merchantId,email,name,phone));
    }

    private UUID createNew(UUID merchantId, String email, String name, String phone) {

        Merchant merchant = merchantRepository.getReferenceById(merchantId);

        Customer customer = Customer.builder()
                .contactNumber(phone)
                .email(email)
                .name(name)
                .contactNumber(phone)
                .merchant(merchant)
                .build();

        customer = customerRepository.save(customer);
        return customer.getId();
    }
}
