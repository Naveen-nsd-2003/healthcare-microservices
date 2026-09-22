package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class MedicalRecordResponse {

    private UUID id;

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

    private LocalTime appointmentStartTime;

    private LocalTime appointmentEndTime;

    private String chiefComplaint;

    private String diagnosis;

    private String clinicalObservations;

    private String treatmentPlan;

    private String prescriptionNotes;

    private LocalDate followUpDate;

    private MedicalRecordStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}