package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DoctorAvailabilityResponse {

    private UUID id;

    private UUID doctorId;

    private String doctorName;

    private String specialization;

    private UUID departmentId;

    private String departmentName;

    private UUID providerId;

    private String providerOrganizationName;

    private DayOfWeek dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDurationMinutes;

    private AvailabilityStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}