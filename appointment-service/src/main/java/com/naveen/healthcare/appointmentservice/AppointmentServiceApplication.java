package com.naveen.healthcare.appointmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients(
        basePackages =
                "com.naveen.healthcare.appointmentservice.client"
)
public class AppointmentServiceApplication {

    public static void main(
            String[] args
    ) {

        SpringApplication.run(
                AppointmentServiceApplication.class,
                args
        );
    }
}