package com.naveen.healthcare.claimservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ClaimVerificationTestRequest {

    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;

    @NotNull(message = "Medical Record ID is required")
    private UUID medicalRecordId;

    @NotNull(message = "Policy ID is required")
    private UUID policyId;
}