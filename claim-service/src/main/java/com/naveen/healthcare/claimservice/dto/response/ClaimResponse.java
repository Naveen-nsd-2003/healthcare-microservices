package com.naveen.healthcare.claimservice.dto.response;

import com.naveen.healthcare.claimservice.enums.ClaimStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ClaimResponse {

    private UUID id;

    private String claimNumber;

    private UUID appointmentId;

    private UUID medicalRecordId;

    private UUID policyId;

    private String policyNumber;

    private UUID memberId;

    private String memberEmail;

    private UUID providerId;

    private String providerOrganizationName;

    private UUID departmentId;

    private String departmentName;

    private UUID doctorId;

    private String doctorName;

    private String doctorSpecialization;

    private UUID payerId;

    private String payerOrganizationName;

    private UUID planId;

    private String planCode;

    private String planName;

    private LocalDate serviceDate;

    private BigDecimal consultationFee;

    private BigDecimal claimedAmount;

    private BigDecimal approvedAmount;

    private BigDecimal coverageAmount;

    private BigDecimal annualDeductible;

    private BigDecimal copayPercentage;

    private BigDecimal coinsurancePercentage;

    private LocalDate policyCoverageStartDate;

    private LocalDate policyCoverageEndDate;

    private String diagnosisSnapshot;

    private String treatmentPlanSnapshot;

    private String claimDescription;

    private ClaimStatus status;

    private String reviewRemarks;

    private String rejectionReason;

    private String cancellationReason;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime cancelledAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}