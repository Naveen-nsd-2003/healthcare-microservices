package com.naveen.healthcare.providerservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateDoctorRequest {

    @NotBlank(message = "Doctor first name is required")
    @Size(
            max = 100,
            message = "Doctor first name must not exceed 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z .'-]*$",
            message = "Doctor first name contains invalid characters"
    )
    private String firstName;

    @NotBlank(message = "Doctor last name is required")
    @Size(
            max = 100,
            message = "Doctor last name must not exceed 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z .'-]*$",
            message = "Doctor last name contains invalid characters"
    )
    private String lastName;

    @NotBlank(message = "Medical registration number is required")
    @Size(
            min = 3,
            max = 100,
            message = "Medical registration number must contain between 3 and 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9./_-]+$",
            message = "Medical registration number contains invalid characters"
    )
    private String medicalRegistrationNumber;

    @NotBlank(message = "Doctor qualification is required")
    @Size(
            max = 200,
            message = "Doctor qualification must not exceed 200 characters"
    )
    private String qualification;

    @NotBlank(message = "Doctor specialization is required")
    @Size(
            max = 150,
            message = "Doctor specialization must not exceed 150 characters"
    )
    private String specialization;

    @NotNull(message = "Years of experience is required")
    @Min(
            value = 0,
            message = "Years of experience cannot be negative"
    )
    @Max(
            value = 70,
            message = "Years of experience cannot exceed 70"
    )
    private Integer yearsOfExperience;

    @NotNull(message = "Consultation fee is required")
    @DecimalMin(
            value = "0.00",
            message = "Consultation fee cannot be negative"
    )
    private BigDecimal consultationFee;

    @Size(
            max = 1500,
            message = "Doctor biography must not exceed 1500 characters"
    )
    private String biography;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
}