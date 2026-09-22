package com.naveen.healthcare.appointmentservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DoctorScheduleResponse {

    private UUID providerId;

    private String providerOrganizationName;

    private UUID departmentId;

    private String departmentName;

    private UUID doctorId;

    private String doctorName;

    private String specialization;

    private LocalDate appointmentDate;

    private DayOfWeek dayOfWeek;

    private List<AvailabilityWindowResponse>
            availabilityWindows;
}