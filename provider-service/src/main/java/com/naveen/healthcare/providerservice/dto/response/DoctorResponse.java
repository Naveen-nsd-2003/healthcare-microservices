package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DoctorResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String fullName;

    private String medicalRegistrationNumber;

    private String qualification;

    private String specialization;

    private Integer yearsOfExperience;

    private BigDecimal consultationFee;

    private String biography;

    private DoctorStatus status;

    private UUID departmentId;

    private String departmentName;

    private String departmentCode;

    private UUID providerId;

    private String providerOrganizationName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}