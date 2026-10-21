package com.naveen.healthcare.claimservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleClaimPolicyResponse {

    private UUID policyId;

    private String policyNumber;

    private UUID payerId;

    private String payerOrganizationName;

    private UUID planId;

    private String planCode;

    private String planName;

    private BigDecimal coverageAmount;

    private BigDecimal annualDeductible;

    private BigDecimal copayPercentage;

    private BigDecimal coinsurancePercentage;

    private Boolean outpatientCoverage;

    private LocalDate coverageStartDate;

    private LocalDate coverageEndDate;
}