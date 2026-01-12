package com.mylog.config;

import feign.codec.Encoder;
import feign.form.FormEncoder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.mylog")
public class FeignConfig {
    @Bean
    public Encoder feignFormEncoder() {
        return new FormEncoder();
    }
}