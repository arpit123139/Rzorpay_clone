package com.example.distributed_razorpay.common_lib.web;

import com.example.distributed_razorpay.common_lib.context.MerchantContext;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.RequestContextFilter;

@AutoConfiguration
public class SharedWebFilterAutoConfiguration {


    @Bean
    @ConditionalOnProperty(
            prefix = "app.merchant-context",
            name = "filter-enabled",
            havingValue = "true"
    )
    public MerchantContextFilter merchantContextFilter(
            MerchantContext merchantContext
    ) {
        return new MerchantContextFilter(merchantContext);
    }

    @Bean
    public FilterRegistrationBean<Filter> requestContextFilterBean(){
         FilterRegistrationBean<Filter> registration =
                new FilterRegistrationBean<>(new RequestContextFilter());

         registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
         registration.addUrlPatterns("/*");
         return registration;
    }
}
