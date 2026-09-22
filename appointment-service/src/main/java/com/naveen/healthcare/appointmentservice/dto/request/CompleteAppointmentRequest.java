package com.naveen.healthcare.appointmentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompleteAppointmentRequest {

    @NotBlank(message = "Provider notes are required")
    @Size(
            max = 1000,
            message = "Provider notes must not exceed 1000 characters"
    )
    private String providerNotes;
}