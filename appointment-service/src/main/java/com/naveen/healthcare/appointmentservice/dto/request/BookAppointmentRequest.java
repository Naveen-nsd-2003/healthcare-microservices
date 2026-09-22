package com.naveen.healthcare.appointmentservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class BookAppointmentRequest {

    @NotNull(message = "Doctor ID is required")
    private UUID doctorId;

    @NotNull(message = "Appointment date is required")
    @FutureOrPresent(
            message = "Appointment date cannot be in the past"
    )
    private LocalDate appointmentDate;

    @NotNull(message = "Appointment start time is required")
    private LocalTime startTime;

    @NotBlank(message = "Reason for visit is required")
    @Size(
            max = 1000,
            message = "Reason for visit must not exceed 1000 characters"
    )
    private String reasonForVisit;
}