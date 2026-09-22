package com.naveen.healthcare.payersservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(
        basePackages = "com.naveen.healthcare.payersservice.client"
)
public class PayersServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                PayersServiceApplication.class,
                args
        );
    }
}