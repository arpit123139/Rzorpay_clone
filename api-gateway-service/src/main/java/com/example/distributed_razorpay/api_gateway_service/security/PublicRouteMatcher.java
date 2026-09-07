package com.example.distributed_razorpay.api_gateway_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
@RequiredArgsConstructor
public class PublicRouteMatcher {


    private final SecurityRoutesProperty securityRoutesProperty;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    public boolean isPublic(String path){
        return securityRoutesProperty.getPublicRoutes().stream()
                .anyMatch(pattern->antPathMatcher.match(pattern,path));
    }
}
