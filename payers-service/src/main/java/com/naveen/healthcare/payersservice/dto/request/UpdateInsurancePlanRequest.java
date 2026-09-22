package com.naveen.healthcare.payersservice.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateInsurancePlanRequest {

    @NotBlank(
            message = "Plan name is required"
    )
    @Size(
            max = 150,
            message = "Plan name must not exceed 150 characters"
    )
    private String planName;

    @NotBlank(
            message = "Plan description is required"
    )
    @Size(
            max = 1000,
            message = "Description must not exceed 1000 characters"
    )
    private String description;

    @NotNull(
            message = "Coverage amount is required"
    )
    @DecimalMin(
            value = "1.00",
            message = "Coverage amount must be greater than zero"
    )
    private BigDecimal coverageAmount;

    @NotNull(
            message = "Monthly premium is required"
    )
    @DecimalMin(
            value = "0.01",
            message = "Monthly premium must be greater than zero"
    )
    private BigDecimal monthlyPremium;

    @NotNull(
            message = "Annual deductible is required"
    )
    @DecimalMin(
            value = "0.00",
            message = "Annual deductible cannot be negative"
    )
    private BigDecimal annualDeductible;

    @NotNull(
            message = "Copay percentage is required"
    )
    @DecimalMin(
            value = "0.00",
            message = "Copay percentage cannot be negative"
    )
    @DecimalMax(
            value = "100.00",
            message = "Copay percentage cannot exceed 100"
    )
    private BigDecimal copayPercentage;

    @NotNull(
            message = "Coinsurance percentage is required"
    )
    @DecimalMin(
            value = "0.00",
            message = "Coinsurance percentage cannot be negative"
    )
    @DecimalMax(
            value = "100.00",
            message = "Coinsurance percentage cannot exceed 100"
    )
    private BigDecimal coinsurancePercentage;

    @NotNull(
            message = "Minimum age is required"
    )
    @Min(
            value = 0,
            message = "Minimum age cannot be negative"
    )
    @Max(
            value = 120,
            message = "Minimum age cannot exceed 120"
    )
    private Integer minimumAge;

    @NotNull(
            message = "Maximum age is required"
    )
    @Min(
            value = 1,
            message = "Maximum age must be at least 1"
    )
    @Max(
            value = 120,
            message = "Maximum age cannot exceed 120"
    )
    private Integer maximumAge;

    @NotNull(
            message = "Policy duration is required"
    )
    @Min(
            value = 1,
            message = "Policy duration must be at least one month"
    )
    @Max(
            value = 120,
            message = "Policy duration cannot exceed 120 months"
    )
    private Integer policyDurationMonths;

    @NotNull(
            message = "Hospitalization coverage value is required"
    )
    private Boolean hospitalizationCoverage;

    @NotNull(
            message = "Outpatient coverage value is required"
    )
    private Boolean outpatientCoverage;

    @NotNull(
            message = "Prescription coverage value is required"
    )
    private Boolean prescriptionCoverage;

    @NotNull(
            message = "Maternity coverage value is required"
    )
    private Boolean maternityCoverage;

    @NotNull(
            message = "Waiting period is required"
    )
    @Min(
            value = 0,
            message = "Waiting period cannot be negative"
    )
    @Max(
            value = 1460,
            message = "Waiting period cannot exceed 1460 days"
    )
    private Integer waitingPeriodDays;
}