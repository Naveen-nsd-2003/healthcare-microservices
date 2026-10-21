package com.naveen.healthcare.providerservice.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveProviderOrganizationRequest {

    @Size(
            max = 1000,
            message = "Review remarks must not exceed 1000 characters"
    )
    private String reviewRemarks;
}