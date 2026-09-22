package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.entity.InsurancePolicy;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.PolicyStatus;
import com.naveen.healthcare.payersservice.exception.InvalidPolicyClaimException;
import com.naveen.healthcare.payersservice.exception.PolicyNotFoundException;
import com.naveen.healthcare.payersservice.repository.InsurancePolicyRepository;
import com.naveen.healthcare.payersservice.service.PolicyClaimVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyClaimVerificationServiceImpl
        implements PolicyClaimVerificationService {

    private final InsurancePolicyRepository insurancePolicyRepository;

    @Override
    @Transactional(readOnly = true)
    public ClaimPolicyVerificationResponse verifyPolicyForClaim(
            UUID policyId,
            UUID memberId,
            LocalDate serviceDate
    ) {

        validateRequest(
                policyId,
                memberId,
                serviceDate
        );

        InsurancePolicy insurancePolicy =
                insurancePolicyRepository
                        .findById(policyId)
                        .orElseThrow(() ->
                                new PolicyNotFoundException(
                                        "Insurance policy not found with ID: "
                                                + policyId
                                )
                        );

        validatePolicyMemberOwnership(
                insurancePolicy,
                memberId
        );

        validateCoverageDates(
                insurancePolicy,
                serviceDate
        );

        PolicyStatus effectiveStatus =
                resolveEffectivePolicyStatus(
                        insurancePolicy,
                        serviceDate
                );

        validatePolicyStatus(effectiveStatus);

        validatePolicyRelationships(insurancePolicy);

        validatePolicyFinancialInformation(
                insurancePolicy
        );

        return mapToResponse(
                insurancePolicy,
                effectiveStatus,
                serviceDate
        );
    }

    private void validateRequest(
            UUID policyId,
            UUID memberId,
            LocalDate serviceDate
    ) {

        if (policyId == null) {
            throw new InvalidPolicyClaimException(
                    "Policy ID is required"
            );
        }

        if (memberId == null) {
            throw new InvalidPolicyClaimException(
                    "Member ID is required"
            );
        }

        if (serviceDate == null) {
            throw new InvalidPolicyClaimException(
                    "Service date is required"
            );
        }

        if (serviceDate.isAfter(LocalDate.now())) {
            throw new InvalidPolicyClaimException(
                    "A claim cannot be submitted for a future service date"
            );
        }
    }

    private void validatePolicyMemberOwnership(
            InsurancePolicy insurancePolicy,
            UUID requestedMemberId
    ) {

        UUID policyMemberId =
                insurancePolicy.getMemberId();

        if (policyMemberId == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid Member ID"
            );
        }

        if (!requestedMemberId.equals(policyMemberId)) {
            throw new PolicyNotFoundException(
                    "Insurance policy not found for the supplied Member"
            );
        }

        String memberEmail =
                insurancePolicy.getMemberEmail();

        if (memberEmail == null || memberEmail.isBlank()) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid Member email"
            );
        }
    }

    private void validateCoverageDates(
            InsurancePolicy insurancePolicy,
            LocalDate serviceDate
    ) {

        LocalDate coverageStartDate =
                insurancePolicy.getCoverageStartDate();

        LocalDate coverageEndDate =
                insurancePolicy.getCoverageEndDate();

        if (coverageStartDate == null
                || coverageEndDate == null) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain valid coverage dates"
            );
        }

        if (coverageStartDate.isAfter(coverageEndDate)) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy contains an invalid coverage date range"
            );
        }

        if (serviceDate.isBefore(coverageStartDate)) {
            throw new InvalidPolicyClaimException(
                    "The service date is before the insurance policy coverage start date"
            );
        }

        if (serviceDate.isAfter(coverageEndDate)) {
            throw new InvalidPolicyClaimException(
                    "The service date is after the insurance policy coverage end date"
            );
        }
    }

    private PolicyStatus resolveEffectivePolicyStatus(
            InsurancePolicy insurancePolicy,
            LocalDate serviceDate
    ) {

        PolicyStatus storedStatus =
                insurancePolicy.getStatus();

        if (storedStatus == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid status"
            );
        }

        LocalDate coverageEndDate =
                insurancePolicy.getCoverageEndDate();

        if (coverageEndDate != null
                && serviceDate.isAfter(coverageEndDate)) {

            return PolicyStatus.EXPIRED;
        }

        return storedStatus;
    }

    private void validatePolicyStatus(
            PolicyStatus effectiveStatus
    ) {

        if (effectiveStatus != PolicyStatus.ACTIVE) {
            throw new InvalidPolicyClaimException(
                    "Only an ACTIVE insurance policy can be used "
                            + "for claim submission. Current policy status: "
                            + effectiveStatus
            );
        }
    }

    private void validatePolicyRelationships(
            InsurancePolicy insurancePolicy
    ) {

        InsuranceEnrollment enrollment =
                insurancePolicy.getEnrollment();

        if (enrollment == null || enrollment.getId() == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain valid enrollment information"
            );
        }

        InsurancePlan insurancePlan =
                insurancePolicy.getInsurancePlan();

        if (insurancePlan == null
                || insurancePlan.getId() == null) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain valid insurance plan information"
            );
        }

        PayerOrganization payerOrganization =
                insurancePolicy.getPayerOrganization();

        if (payerOrganization == null
                || payerOrganization.getId() == null) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain valid Payer organization information"
            );
        }

        if (payerOrganization.getOrganizationName() == null
                || payerOrganization
                .getOrganizationName()
                .isBlank()) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid Payer organization name"
            );
        }

        if (insurancePolicy.getPolicyNumber() == null
                || insurancePolicy
                .getPolicyNumber()
                .isBlank()) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid policy number"
            );
        }

        if (insurancePolicy.getPlanCode() == null
                || insurancePolicy.getPlanCode().isBlank()) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid plan code"
            );
        }

        if (insurancePolicy.getPlanName() == null
                || insurancePolicy.getPlanName().isBlank()) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain a valid plan name"
            );
        }
    }

    private void validatePolicyFinancialInformation(
            InsurancePolicy insurancePolicy
    ) {

        validatePositiveAmount(
                insurancePolicy.getCoverageAmount(),
                "coverage amount"
        );

        validateNonNegativeAmount(
                insurancePolicy.getAnnualDeductible(),
                "annual deductible"
        );

        validatePercentage(
                insurancePolicy.getCopayPercentage(),
                "copay percentage"
        );

        validatePercentage(
                insurancePolicy.getCoinsurancePercentage(),
                "coinsurance percentage"
        );

        if (insurancePolicy.getHospitalizationCoverage() == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain hospitalization coverage information"
            );
        }

        if (insurancePolicy.getOutpatientCoverage() == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain outpatient coverage information"
            );
        }

        if (insurancePolicy.getPrescriptionCoverage() == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain prescription coverage information"
            );
        }

        if (insurancePolicy.getMaternityCoverage() == null) {
            throw new InvalidPolicyClaimException(
                    "The insurance policy does not contain maternity coverage information"
            );
        }
    }

    private void validatePositiveAmount(
            BigDecimal value,
            String fieldName
    ) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy contains an invalid "
                            + fieldName
            );
        }
    }

    private void validateNonNegativeAmount(
            BigDecimal value,
            String fieldName
    ) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) < 0) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy contains an invalid "
                            + fieldName
            );
        }
    }

    private void validatePercentage(
            BigDecimal value,
            String fieldName
    ) {

        if (value == null
                || value.compareTo(BigDecimal.ZERO) < 0
                || value.compareTo(BigDecimal.valueOf(100)) > 0) {

            throw new InvalidPolicyClaimException(
                    "The insurance policy contains an invalid "
                            + fieldName
            );
        }
    }

    private ClaimPolicyVerificationResponse mapToResponse(
            InsurancePolicy insurancePolicy,
            PolicyStatus effectiveStatus,
            LocalDate serviceDate
    ) {

        InsuranceEnrollment enrollment =
                insurancePolicy.getEnrollment();

        InsurancePlan insurancePlan =
                insurancePolicy.getInsurancePlan();

        PayerOrganization payerOrganization =
                insurancePolicy.getPayerOrganization();

        return ClaimPolicyVerificationResponse.builder()
                .policyId(
                        insurancePolicy.getId()
                )
                .policyNumber(
                        insurancePolicy.getPolicyNumber()
                )
                .enrollmentId(
                        enrollment.getId()
                )
                .memberId(
                        insurancePolicy.getMemberId()
                )
                .memberEmail(
                        insurancePolicy.getMemberEmail()
                )
                .payerId(
                        payerOrganization.getId()
                )
                .payerOrganizationName(
                        payerOrganization.getOrganizationName()
                )
                .planId(
                        insurancePlan.getId()
                )
                .planCode(
                        insurancePolicy.getPlanCode()
                )
                .planName(
                        insurancePolicy.getPlanName()
                )
                .coverageAmount(
                        insurancePolicy.getCoverageAmount()
                )
                .annualDeductible(
                        insurancePolicy.getAnnualDeductible()
                )
                .copayPercentage(
                        insurancePolicy.getCopayPercentage()
                )
                .coinsurancePercentage(
                        insurancePolicy.getCoinsurancePercentage()
                )
                .hospitalizationCoverage(
                        insurancePolicy.getHospitalizationCoverage()
                )
                .outpatientCoverage(
                        insurancePolicy.getOutpatientCoverage()
                )
                .prescriptionCoverage(
                        insurancePolicy.getPrescriptionCoverage()
                )
                .maternityCoverage(
                        insurancePolicy.getMaternityCoverage()
                )
                .coverageStartDate(
                        insurancePolicy.getCoverageStartDate()
                )
                .coverageEndDate(
                        insurancePolicy.getCoverageEndDate()
                )
                .status(
                        effectiveStatus
                )
                .serviceDate(
                        serviceDate
                )
                .coverageValid(true)
                .build();
    }
}