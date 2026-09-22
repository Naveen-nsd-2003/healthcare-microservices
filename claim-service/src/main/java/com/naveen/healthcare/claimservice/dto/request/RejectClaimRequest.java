package com.naveen.healthcare.claimservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectClaimRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(
            max = 1000,
            message = "Rejection reason must not exceed 1000 characters"
    )
    private String rejectionReason;

    @NotBlank(message = "Review remarks are required")
    @Size(
            max = 1500,
            message = "Review remarks must not exceed 1500 characters"
    )
    private  String reviewRemarks;
}
