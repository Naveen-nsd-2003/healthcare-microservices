package com.naveen.healthcare.claimservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelClaimRequest {

    @NotBlank(message = "Cancellation reason is required")
    @Size(
            max = 1000,
            message = "Cancellation reason must not exceed 1000 characters"
    )
    private String cancellationReason;
}