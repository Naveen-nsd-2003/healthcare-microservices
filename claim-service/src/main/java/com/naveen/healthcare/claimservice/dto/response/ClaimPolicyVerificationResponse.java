package com.naveen.healthcare.claimservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ClaimPolicyVerificationResponse {

    private UUID policyId;

    private String policyNumber;

    private UUID enrollmentId;

    private UUID memberId;

    private String memberEmail;

    private UUID payerId;

    private String payerOrganizationName;

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

    private String status;

    private LocalDate serviceDate;

    private Boolean coverageValid;
}