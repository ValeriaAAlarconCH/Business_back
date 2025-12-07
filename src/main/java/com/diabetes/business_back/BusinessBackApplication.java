package com.diabetes.business_back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class BusinessBackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BusinessBackApplication.class, args);
    }
}
