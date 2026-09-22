package com.naveen.healthcare.appointmentservice.client;

import com.naveen.healthcare.appointmentservice.config.FeignClientConfig;
import com.naveen.healthcare.appointmentservice.dto.response.DoctorScheduleResponse;
import com.naveen.healthcare.appointmentservice.dto.response.ProviderBookingVerificationResponse;
import com.naveen.healthcare.appointmentservice.dto.response.ProviderIdentityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@FeignClient(
        name = "provider-service",
        configuration = FeignClientConfig.class
)
public interface ProviderServiceClient {

    @GetMapping(
            "/internal/providers/doctors/"
                    + "{doctorId}/booking-verification"
    )
    ProviderBookingVerificationResponse verifyBookingSlot(
            @PathVariable("doctorId")
            UUID doctorId,

            @RequestParam("appointmentDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate appointmentDate,

            @RequestParam("startTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime
    );

    @GetMapping("/internal/providers/me/identity")
    ProviderIdentityResponse getAuthenticatedProviderIdentity();

    @GetMapping(
            "/internal/providers/doctors/{doctorId}/schedule"
    )
    DoctorScheduleResponse getDoctorScheduleForDate(
            @PathVariable("doctorId")
            UUID doctorId,

            @RequestParam("appointmentDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate appointmentDate
    );

}