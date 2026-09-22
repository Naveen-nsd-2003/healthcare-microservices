package com.naveen.healthcare.payersservice.dto.request;

import com.naveen.healthcare.payersservice.enums.NomineeRelationship;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateEnrollmentRequest {

    @NotNull(message = "Insurance plan ID is required")
    private UUID planId;

    @NotNull(
            message = "Requested coverage start date is required"
    )
    @FutureOrPresent(
            message = "Requested coverage start date cannot be in the past"
    )
    private LocalDate requestedCoverageStartDate;

    @NotBlank(message = "Nominee name is required")
    @Size(
            max = 150,
            message = "Nominee name must not exceed 150 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z .'-]*$",
            message = "Nominee name contains invalid characters"
    )
    private String nomineeName;

    @NotNull(message = "Nominee relationship is required")
    private NomineeRelationship nomineeRelationship;

    @NotBlank(message = "Nominee contact number is required")
    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Nominee contact number must contain between 10 and 20 valid characters"
    )
    private String nomineeContactNumber;
}