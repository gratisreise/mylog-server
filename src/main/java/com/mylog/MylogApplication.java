package com.mylog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableJpaAuditing
@EnableCaching
@EnableAsync
@SpringBootApplication
public class MylogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MylogApplication.class, args);
    }

}
