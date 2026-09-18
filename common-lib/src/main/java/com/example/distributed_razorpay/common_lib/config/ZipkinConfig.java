package com.example.distributed_razorpay.common_lib.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@AutoConfiguration
public class ZipkinConfig {

    @Bean
    public BytesMessageSender zipkinSender(
            @Value("${management.zipkin.tracing.endpoint}") String endPoint
    ){
        return URLConnectionSender.create(endPoint);
    }
}
