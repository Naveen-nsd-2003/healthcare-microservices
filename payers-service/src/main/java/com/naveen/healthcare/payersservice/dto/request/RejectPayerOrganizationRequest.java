package com.naveen.healthcare.payersservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectPayerOrganizationRequest {

    @NotBlank(
            message = "Rejection reason is required"
    )
    @Size(
            max = 1000,
            message = "Rejection reason must not exceed 1000 characters"
    )
    private String rejectionReason;

    @Size(
            max = 1000,
            message = "Review remarks must not exceed 1000 characters"
    )
    private String reviewRemarks;
}