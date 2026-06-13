package com.example.razorpay.merchant.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false , length = 200)
    private String name;

    @Column(nullable = false ,length = 20)
    private String contactNumber;


    @ManyToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(name = "merchant_id" , nullable = false)
    private Merchant merchant;

    @Column(length = 200)
    private String email;


    private LocalDateTime deletedAt;

}
