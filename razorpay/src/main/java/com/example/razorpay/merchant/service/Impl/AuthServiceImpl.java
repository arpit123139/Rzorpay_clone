package com.example.razorpay.merchant.service.Impl;

import com.example.razorpay.common.enums.MerchantStatus;
import com.example.razorpay.common.enums.UserRole;
import com.example.razorpay.common.exceptions.DuplicateResourceException;
import com.example.razorpay.common.exceptions.ResourceNotFoundException;
import com.example.razorpay.merchant.dto.Request.LoginRequest;
import com.example.razorpay.merchant.dto.Request.MerchantSignUpRequest;
import com.example.razorpay.merchant.dto.Response.LoginResponse;
import com.example.razorpay.merchant.dto.Response.MerchantResponse;
import com.example.razorpay.merchant.entity.AppUser;
import com.example.razorpay.merchant.entity.Merchant;
import com.example.razorpay.merchant.mapper.MerchantMapper;
import com.example.razorpay.merchant.repository.AppUserRepository;
import com.example.razorpay.merchant.repository.MerchantRepository;
import com.example.razorpay.merchant.security.JwtUtil;
import com.example.razorpay.merchant.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthServiceImpl implements AuthService {


    private final AppUserRepository appUserRepository;
    private final MerchantRepository merchantRepository;
    private final MerchantMapper merchantMapper;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public MerchantResponse signUp(MerchantSignUpRequest request) {
        if(merchantRepository.existsByEmail(request.email())){
            throw  new DuplicateResourceException("DUPLICATE_MERCHANT_EMAIL","Merchant already exsist with email "+request.email());
        }

        Merchant merchant=Merchant.builder()
                .name(request.name())
                .email(request.email())
                .buisnessName(request.buisnessName())
                .buisnessType(request.buisnessType())
                .status(MerchantStatus.PENDING_KYC)
                .build();
        merchant=merchantRepository.save(merchant);

        AppUser appUser=AppUser.builder()
                .email(request.email())
                .merchant(merchant)
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.OWNER)
                .build();
        appUser=appUserRepository.save(appUser);

        return merchantMapper.toMerchantResponse(merchant);

    }

    @Override
    public LoginResponse login(LoginRequest request) {

        String email = request.email();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,request.password())
        );

        AppUser appUser= appUserRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User",email));

        String accessToken = jwtUtil.generateAccessToken(email,appUser.getMerchant().getId(),appUser.getRole().toString());

        return new LoginResponse(accessToken);


    }
}
