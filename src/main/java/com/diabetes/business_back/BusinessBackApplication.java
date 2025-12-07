package com.diabetes.business_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.diabetes.business_back",
        "com.diabetes.business_back.controllers",
        "com.diabetes.business_back.services",
        "com.diabetes.business_back.config"
})
public class BusinessBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessBackApplication.class, args);
    }
}
