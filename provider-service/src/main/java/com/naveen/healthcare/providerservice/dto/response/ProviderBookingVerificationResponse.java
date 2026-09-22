package com.naveen.healthcare.providerservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
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