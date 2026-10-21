package com.naveen.healthcare.providerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(
        basePackages =
                "com.naveen.healthcare.providerservice.client"
)
@EnableScheduling
public class ProviderServiceApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ProviderServiceApplication.class,
                args
        );
    }
}