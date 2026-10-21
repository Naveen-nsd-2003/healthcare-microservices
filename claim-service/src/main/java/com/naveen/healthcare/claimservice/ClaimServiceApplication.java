package com.naveen.healthcare.claimservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(
        basePackages =
                "com.naveen.healthcare.claimservice.client"
)
public class ClaimServiceApplication {

    public static void main(
            String[] args
    ) {

        SpringApplication.run(
                ClaimServiceApplication.class,
                args
        );
    }
}