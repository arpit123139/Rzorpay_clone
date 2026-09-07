package com.example.distributed_razorpay.merchant_service.service.Impl;


import com.example.distributed_razorpay.common_lib.enums.MerchantStatus;
import com.example.distributed_razorpay.common_lib.enums.UserRole;
import com.example.distributed_razorpay.common_lib.exceptions.BuisnessRuleViolationException;
import com.example.distributed_razorpay.common_lib.exceptions.DuplicateResourceException;
import com.example.distributed_razorpay.common_lib.exceptions.ResourceNotFoundException;
import com.example.distributed_razorpay.merchant_service.dto.Request.LoginRequest;
import com.example.distributed_razorpay.merchant_service.dto.Request.MerchantSignUpRequest;
import com.example.distributed_razorpay.merchant_service.dto.Response.LoginResponse;
import com.example.distributed_razorpay.merchant_service.dto.Response.MerchantResponse;
import com.example.distributed_razorpay.merchant_service.entity.AppUser;
import com.example.distributed_razorpay.merchant_service.entity.Merchant;
import com.example.distributed_razorpay.merchant_service.mapper.MerchantMapper;
import com.example.distributed_razorpay.merchant_service.repository.AppUserRepository;
import com.example.distributed_razorpay.merchant_service.repository.MerchantRepository;
import com.example.distributed_razorpay.merchant_service.security.JwtUtil;
import com.example.distributed_razorpay.merchant_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
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
    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.email();

        //login - verify the password

        AppUser appUser= appUserRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User",email));

        if(!passwordEncoder.matches(request.password(), appUser.getPasswordHash())){
            throw new BuisnessRuleViolationException("INVALID_CREDENTIALS","Invalid email or Password");
        }

        String accessToken = jwtUtil.generateAccessToken(email,appUser.getMerchant().getId(),appUser.getRole().toString());

        return new LoginResponse(accessToken);


    }
}
