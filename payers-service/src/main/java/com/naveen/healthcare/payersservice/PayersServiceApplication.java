package com.naveen.healthcare.payersservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
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