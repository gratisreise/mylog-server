package com.mylog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.mylog")
public class MylogApplication {
    public static void main(String[] args) {
        SpringApplication.run(MylogApplication.class, args);
    }
}
