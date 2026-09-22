package com.naveen.healthcare.claimservice.client;

import com.naveen.healthcare.claimservice.config.FeignClientConfig;
import com.naveen.healthcare.claimservice.dto.response.ClaimAppointmentVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "appointment-service",
        configuration = FeignClientConfig.class
)
public interface AppointmentServiceClient {

    @GetMapping(
            "/internal/appointments/"
                    + "{appointmentId}/claim-verification"
    )
    ClaimAppointmentVerificationResponse
    verifyCompletedAppointmentForClaim(
            @PathVariable("appointmentId")
            UUID appointmentId
    );
}