package com.naveen.healthcare.providerservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectProviderOrganizationRequest {

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