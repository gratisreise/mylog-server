package com.mylog.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableJpaAuditing
@EnableFeignClients
@EnableCaching
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.mylog")
public class MylogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MylogApplication.class, args);
    }

}
