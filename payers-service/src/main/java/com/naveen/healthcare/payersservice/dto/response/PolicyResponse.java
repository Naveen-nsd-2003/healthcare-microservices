package com.naveen.healthcare.payersservice.dto.response;

import com.naveen.healthcare.payersservice.enums.PolicyStatus;
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
public class PolicyResponse {

    private UUID id;

    private String policyNumber;

    private UUID enrollmentId;

    private UUID memberId;

    private String memberEmail;

    private UUID planId;

    private String planName;

    private String planCode;

    private UUID payerId;

    private String payerOrganizationName;

    private BigDecimal coverageAmount;

    private BigDecimal monthlyPremium;

    private BigDecimal annualDeductible;

    private BigDecimal copayPercentage;

    private BigDecimal coinsurancePercentage;

    private Integer policyDurationMonths;

    private Integer waitingPeriodDays;

    private Boolean hospitalizationCoverage;

    private Boolean outpatientCoverage;

    private Boolean prescriptionCoverage;

    private Boolean maternityCoverage;

    private LocalDate coverageStartDate;

    private LocalDate coverageEndDate;

    private PolicyStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}