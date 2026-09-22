package com.naveen.healthcare.claimservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class SubmitClaimRequest {

    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;

    @NotNull(message = "Medical Record ID is required")
    private UUID medicalRecordId;

    @NotNull(message = "Policy ID is required")
    private UUID policyId;

    @NotNull(message = "Claimed amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Claimed amount must be greater than zero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Claimed amount must contain at most 10 integer digits and 2 decimal places"
    )
    private BigDecimal claimedAmount;

    @NotBlank(message = "Claim description is required")
    @Size(
            max = 1500,
            message = "Claim description must not exceed 1500 characters"
    )
    private String claimDescription;
}