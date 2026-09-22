package com.naveen.healthcare.providerservice.client;

import com.naveen.healthcare.providerservice.config.FeignClientConfig;
import com.naveen.healthcare.providerservice.dto.response.MedicalRecordAppointmentVerificationResponse;
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
                    + "{appointmentId}/medical-record-verification"
    )
    MedicalRecordAppointmentVerificationResponse
    verifyCompletedAppointmentForMedicalRecord(
            @PathVariable("appointmentId")
            UUID appointmentId
    );
}