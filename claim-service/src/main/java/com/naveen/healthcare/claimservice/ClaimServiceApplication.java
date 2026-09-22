package com.naveen.healthcare.claimservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(
        basePackages = "com.naveen.healthcare.claimservice.client"
)
public class ClaimServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ClaimServiceApplication.class,
                args
        );
    }
}