package com.naveen.healthcare.claimservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ApproveClaimRequest {

    @NotNull(message = "Approved amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Approved amount must be greater than zero"
    )
    @Digits(
            integer = 10,
            fraction = 2,
            message = "Approved amount must contain at most 10 integer digits and 2 decimal places"
    )
    private BigDecimal approvedAmount;

    @NotBlank(message = "Review remarks are required")
    @Size(
            max = 1500,
            message = "Review remarks must not exceed 1500 characters"
    )
    private String reviewRemarks;
}