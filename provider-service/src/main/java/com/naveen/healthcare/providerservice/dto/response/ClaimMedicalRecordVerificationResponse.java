package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ClaimMedicalRecordVerificationResponse {

    private UUID medicalRecordId;

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

    private String diagnosis;

    private String treatmentPlan;

    private MedicalRecordStatus status;
}