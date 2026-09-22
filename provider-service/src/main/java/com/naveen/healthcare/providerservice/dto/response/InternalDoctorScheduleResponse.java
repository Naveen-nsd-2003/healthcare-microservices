package com.naveen.healthcare.providerservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class InternalDoctorScheduleResponse {

    private UUID providerId;

    private String providerOrganizationName;

    private UUID departmentId;

    private String departmentName;

    private UUID doctorId;

    private String doctorName;

    private String specialization;

    private LocalDate appointmentDate;

    private DayOfWeek dayOfWeek;

    private List<InternalAvailabilityWindowResponse>
            availabilityWindows;
}