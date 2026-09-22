package com.naveen.healthcare.payersservice.dto.response;

import com.naveen.healthcare.payersservice.enums.InsurancePlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class InsurancePlanResponse {

    private UUID id;

    private String planName;

    private String planCode;

    private String description;

    private BigDecimal coverageAmount;

    private BigDecimal monthlyPremium;

    private BigDecimal annualDeductible;

    private BigDecimal copayPercentage;

    private BigDecimal coinsurancePercentage;

    private Integer minimumAge;

    private Integer maximumAge;

    private Integer policyDurationMonths;

    private Boolean hospitalizationCoverage;

    private Boolean outpatientCoverage;

    private Boolean prescriptionCoverage;

    private Boolean maternityCoverage;

    private Integer waitingPeriodDays;

    private InsurancePlanStatus status;

    private UUID payerId;

    private String payerOrganizationName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}