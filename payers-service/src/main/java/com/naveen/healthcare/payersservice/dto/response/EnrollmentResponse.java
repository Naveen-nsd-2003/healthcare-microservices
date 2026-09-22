package com.naveen.healthcare.payersservice.dto.response;

import com.naveen.healthcare.payersservice.enums.EnrollmentStatus;
import com.naveen.healthcare.payersservice.enums.NomineeRelationship;
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
public class EnrollmentResponse {

    private UUID id;

    private UUID memberId;

    private String memberEmail;

    private UUID planId;

    private String planCode;

    private String planName;

    private BigDecimal coverageAmount;

    private BigDecimal monthlyPremium;

    private UUID payerId;

    private String payerOrganizationName;

    private LocalDate requestedCoverageStartDate;

    private String nomineeName;

    private NomineeRelationship nomineeRelationship;

    private String nomineeContactNumber;

    private EnrollmentStatus status;

    private String reviewRemarks;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}