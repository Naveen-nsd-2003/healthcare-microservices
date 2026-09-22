package com.naveen.healthcare.providerservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class MedicalRecordAppointmentVerificationResponse {

    private UUID appointmentId;

    private UUID memberId;

    private String memberEmail;

    private UUID providerId;

    private String providerOrganizationName;

    private UUID departmentId;

    private String departmentName;

    private UUID doctorId;

    private String doctorName;

    private String doctorSpecialization;

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private String status;
}