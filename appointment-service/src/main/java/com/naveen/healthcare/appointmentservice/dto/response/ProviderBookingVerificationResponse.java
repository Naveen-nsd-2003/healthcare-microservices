package com.naveen.healthcare.appointmentservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProviderBookingVerificationResponse {

    private UUID providerId;

    private String providerOrganizationName;

    private UUID departmentId;

    private String departmentName;

    private UUID doctorId;

    private String doctorName;

    private String specialization;

    private BigDecimal consultationFee;

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDurationMinutes;

    private Boolean available;
}