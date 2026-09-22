package com.naveen.healthcare.payersservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewEnrollmentRequest {

    @NotBlank(message = "Review remarks are required")
    @Size(
            max = 500,
            message = "Review remarks must not exceed 500 characters"
    )
    private String reviewRemarks;
}