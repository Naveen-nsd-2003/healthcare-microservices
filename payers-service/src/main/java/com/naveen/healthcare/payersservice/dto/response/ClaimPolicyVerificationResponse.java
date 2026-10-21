package com.naveen.healthcare.payersservice.dto.response;

import com.naveen.healthcare.payersservice.enums.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ClaimPolicyVerificationResponse {

    private UUID policyId;

    private String policyNumber;

    private UUID enrollmentId;

    private UUID memberId;

    private String memberEmail;

    private UUID payerId;

    private String payerOrganizationName;
    private String payerAdminEmail;

    private UUID planId;

    private String planCode;

    private String planName;

    private BigDecimal coverageAmount;

    private BigDecimal annualDeductible;

    private BigDecimal copayPercentage;

    private BigDecimal coinsurancePercentage;

    private Boolean hospitalizationCoverage;

    private Boolean outpatientCoverage;

    private Boolean prescriptionCoverage;

    private Boolean maternityCoverage;

    private LocalDate coverageStartDate;

    private LocalDate coverageEndDate;

    private PolicyStatus status;

    private LocalDate serviceDate;

    private Boolean coverageValid;
}