package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.request.UpdatePolicyStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PolicyResponse;
import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.entity.InsurancePolicy;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.EnrollmentStatus;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import com.naveen.healthcare.payersservice.enums.PolicyStatus;
import com.naveen.healthcare.payersservice.exception.DuplicatePolicyException;
import com.naveen.healthcare.payersservice.exception.InvalidPayerStatusException;
import com.naveen.healthcare.payersservice.exception.InvalidPolicyOperationException;
import com.naveen.healthcare.payersservice.exception.PayerNotFoundException;
import com.naveen.healthcare.payersservice.exception.PolicyNotFoundException;
import com.naveen.healthcare.payersservice.repository.InsurancePolicyRepository;
import com.naveen.healthcare.payersservice.repository.PayerRepository;
import com.naveen.healthcare.payersservice.service.PolicyService;
import com.naveen.healthcare.payersservice.util.PolicyNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.naveen.healthcare.payersservice.service.InsuranceEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private static final int MAX_POLICY_NUMBER_ATTEMPTS = 5;

    private final InsurancePolicyRepository insurancePolicyRepository;

    private final PayerRepository payerRepository;

    private final PolicyNumberGenerator policyNumberGenerator;

    private final InsuranceEventPublisher insuranceEventPublisher;

    @Override
    @Transactional
    public PolicyResponse issuePolicyForApprovedEnrollment(
            InsuranceEnrollment enrollment
    ) {

        validateEnrollmentForPolicyIssuance(enrollment);

        UUID enrollmentId = enrollment.getId();

        if (insurancePolicyRepository.existsByEnrollmentId(enrollmentId)) {
            throw new DuplicatePolicyException(
                    "An insurance policy already exists for enrollment ID: "
                            + enrollmentId
            );
        }

        InsurancePlan insurancePlan =
                enrollment.getInsurancePlan();

        PayerOrganization payerOrganization =
                enrollment.getPayerOrganization();

        validatePayerIsActive(payerOrganization);

        LocalDate coverageStartDate =
                enrollment.getRequestedCoverageStartDate();

        LocalDate coverageEndDate =
                calculateCoverageEndDate(
                        coverageStartDate,
                        insurancePlan.getPolicyDurationMonths()
                );

        String policyNumber =
                generateUniquePolicyNumber();

        InsurancePolicy insurancePolicy =
                InsurancePolicy.builder()
                        .policyNumber(policyNumber)
                        .enrollment(enrollment)
                        .memberId(enrollment.getMemberId())
                        .memberEmail(
                                normalizeEmail(
                                        enrollment.getMemberEmail()
                                )
                        )
                        .insurancePlan(insurancePlan)
                        .payerOrganization(payerOrganization)
                        .planName(insurancePlan.getPlanName())
                        .planCode(insurancePlan.getPlanCode())
                        .coverageAmount(
                                insurancePlan.getCoverageAmount()
                        )
                        .monthlyPremium(
                                insurancePlan.getMonthlyPremium()
                        )
                        .annualDeductible(
                                insurancePlan.getAnnualDeductible()
                        )
                        .copayPercentage(
                                insurancePlan.getCopayPercentage()
                        )
                        .coinsurancePercentage(
                                insurancePlan.getCoinsurancePercentage()
                        )
                        .policyDurationMonths(
                                insurancePlan.getPolicyDurationMonths()
                        )
                        .waitingPeriodDays(
                                insurancePlan.getWaitingPeriodDays()
                        )
                        .hospitalizationCoverage(
                                insurancePlan.getHospitalizationCoverage()
                        )
                        .outpatientCoverage(
                                insurancePlan.getOutpatientCoverage()
                        )
                        .prescriptionCoverage(
                                insurancePlan.getPrescriptionCoverage()
                        )
                        .maternityCoverage(
                                insurancePlan.getMaternityCoverage()
                        )
                        .coverageStartDate(coverageStartDate)
                        .coverageEndDate(coverageEndDate)
                        .status(PolicyStatus.ACTIVE)
                        .build();

        InsurancePolicy savedPolicy =
                insurancePolicyRepository.saveAndFlush(
                        insurancePolicy
                );

        insuranceEventPublisher.publishPolicyActivated(
                savedPolicy
        );

        return mapToResponse(savedPolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyResponse> getMyPolicies(
            String authenticatedMemberEmail
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        return insurancePolicyRepository
                .findAllByMemberEmailIgnoreCaseOrderByCreatedAtDesc(
                        normalizedMemberEmail
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyResponse getMyPolicyById(
            String authenticatedMemberEmail,
            UUID policyId
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        InsurancePolicy insurancePolicy =
                insurancePolicyRepository
                        .findByIdAndMemberEmailIgnoreCase(
                                policyId,
                                normalizedMemberEmail
                        )
                        .orElseThrow(() ->
                                new PolicyNotFoundException(
                                        "Insurance policy not found for "
                                                + "the authenticated Member"
                                )
                        );

        return mapToResponse(insurancePolicy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PolicyResponse> getPayerPolicies(
            String authenticatedPayerEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerByEmail(authenticatedPayerEmail);

        return insurancePolicyRepository
                .findAllByPayerOrganizationAdminEmailIgnoreCaseOrderByCreatedAtDesc(
                        payerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PolicyResponse getPayerPolicyById(
            String authenticatedPayerEmail,
            UUID policyId
    ) {

        InsurancePolicy insurancePolicy =
                findPolicyOwnedByPayer(
                        authenticatedPayerEmail,
                        policyId
                );

        return mapToResponse(insurancePolicy);
    }

    @Override
    @Transactional
    public PolicyResponse updatePolicyStatus(
            String authenticatedPayerEmail,
            UUID policyId,
            UpdatePolicyStatusRequest request
    ) {

        PayerOrganization payerOrganization =
                findActivePayerByEmail(
                        authenticatedPayerEmail
                );

        InsurancePolicy insurancePolicy =
                findPolicyOwnedByPayer(
                        payerOrganization.getAdminEmail(),
                        policyId
                );

        PolicyStatus requestedStatus =
                request.getStatus();

        validatePolicyStatusTransition(
                insurancePolicy,
                requestedStatus
        );

        insurancePolicy.setStatus(
                requestedStatus
        );

        InsurancePolicy updatedPolicy =
                insurancePolicyRepository.saveAndFlush(
                        insurancePolicy
                );

        if (requestedStatus == PolicyStatus.SUSPENDED) {

            insuranceEventPublisher.publishPolicySuspended(
                    updatedPolicy
            );
        }

        if (requestedStatus == PolicyStatus.ACTIVE) {

            insuranceEventPublisher.publishPolicyActivated(
                    updatedPolicy
            );
        }

        if (requestedStatus == PolicyStatus.CANCELLED) {

            insuranceEventPublisher.publishPolicyCancelled(
                    updatedPolicy
            );
        }

        return mapToResponse(updatedPolicy);
    }

    private void validateEnrollmentForPolicyIssuance(
            InsuranceEnrollment enrollment
    ) {

        if (enrollment == null) {
            throw new InvalidPolicyOperationException(
                    "Approved enrollment is required for policy issuance"
            );
        }

        if (enrollment.getId() == null) {
            throw new InvalidPolicyOperationException(
                    "Enrollment must be saved before a policy can be issued"
            );
        }

        if (enrollment.getStatus() != EnrollmentStatus.APPROVED) {
            throw new InvalidPolicyOperationException(
                    "A policy can be issued only for an APPROVED enrollment"
            );
        }

        if (enrollment.getMemberId() == null) {
            throw new InvalidPolicyOperationException(
                    "Enrollment does not contain a Member ID"
            );
        }

        if (enrollment.getMemberEmail() == null
                || enrollment.getMemberEmail().isBlank()) {
            throw new InvalidPolicyOperationException(
                    "Enrollment does not contain a Member email"
            );
        }

        if (enrollment.getInsurancePlan() == null) {
            throw new InvalidPolicyOperationException(
                    "Enrollment does not contain an insurance plan"
            );
        }

        if (enrollment.getPayerOrganization() == null) {
            throw new InvalidPolicyOperationException(
                    "Enrollment does not contain a Payer organization"
            );
        }

        if (enrollment.getRequestedCoverageStartDate() == null) {
            throw new InvalidPolicyOperationException(
                    "Enrollment does not contain a coverage start date"
            );
        }
    }

    private LocalDate calculateCoverageEndDate(
            LocalDate coverageStartDate,
            Integer policyDurationMonths
    ) {

        if (coverageStartDate == null) {
            throw new InvalidPolicyOperationException(
                    "Coverage start date is required"
            );
        }

        if (policyDurationMonths == null
                || policyDurationMonths <= 0) {
            throw new InvalidPolicyOperationException(
                    "Insurance plan contains an invalid policy duration"
            );
        }

        return coverageStartDate
                .plusMonths(policyDurationMonths)
                .minusDays(1);
    }

    private String generateUniquePolicyNumber() {

        for (
                int attempt = 1;
                attempt <= MAX_POLICY_NUMBER_ATTEMPTS;
                attempt++
        ) {

            String policyNumber =
                    policyNumberGenerator.generatePolicyNumber();

            boolean alreadyExists =
                    insurancePolicyRepository
                            .existsByPolicyNumberIgnoreCase(
                                    policyNumber
                            );

            if (!alreadyExists) {
                return policyNumber;
            }
        }

        throw new InvalidPolicyOperationException(
                "Unable to generate a unique policy number"
        );
    }

    private PayerOrganization findPayerByEmail(
            String authenticatedPayerEmail
    ) {

        String normalizedPayerEmail =
                normalizeEmail(authenticatedPayerEmail);

        return payerRepository
                .findByAdminEmailIgnoreCase(
                        normalizedPayerEmail
                )
                .orElseThrow(() ->
                        new PayerNotFoundException(
                                "Payer organization not found for "
                                        + "authenticated email: "
                                        + normalizedPayerEmail
                        )
                );
    }

    private PayerOrganization findActivePayerByEmail(
            String authenticatedPayerEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerByEmail(authenticatedPayerEmail);

        validatePayerIsActive(payerOrganization);

        return payerOrganization;
    }

    private void validatePayerIsActive(
            PayerOrganization payerOrganization
    ) {

        if (payerOrganization == null) {
            throw new InvalidPolicyOperationException(
                    "Payer organization is required"
            );
        }

        if (payerOrganization.getStatus() != PayerStatus.ACTIVE) {
            throw new InvalidPayerStatusException(
                    "Payer organization must be ACTIVE "
                            + "to issue or manage policies"
            );
        }
    }

    private InsurancePolicy findPolicyOwnedByPayer(
            String authenticatedPayerEmail,
            UUID policyId
    ) {

        String normalizedPayerEmail =
                normalizeEmail(authenticatedPayerEmail);

        return insurancePolicyRepository
                .findByIdAndPayerOrganizationAdminEmailIgnoreCase(
                        policyId,
                        normalizedPayerEmail
                )
                .orElseThrow(() ->
                        new PolicyNotFoundException(
                                "Insurance policy not found for "
                                        + "the authenticated Payer"
                        )
                );
    }

    private void validatePolicyStatusTransition(
            InsurancePolicy insurancePolicy,
            PolicyStatus requestedStatus
    ) {

        if (requestedStatus == null) {
            throw new InvalidPolicyOperationException(
                    "Policy status is required"
            );
        }

        PolicyStatus currentStatus =
                resolveEffectiveStatus(insurancePolicy);

        if (currentStatus == requestedStatus) {
            throw new InvalidPolicyOperationException(
                    "Insurance policy is already in "
                            + requestedStatus
                            + " status"
            );
        }

        if (requestedStatus == PolicyStatus.EXPIRED) {
            throw new InvalidPolicyOperationException(
                    "EXPIRED status cannot be assigned manually"
            );
        }

        boolean validTransition =
                switch (currentStatus) {
                    case ACTIVE ->
                            requestedStatus == PolicyStatus.SUSPENDED
                                    || requestedStatus
                                    == PolicyStatus.CANCELLED;

                    case SUSPENDED ->
                            requestedStatus == PolicyStatus.ACTIVE
                                    || requestedStatus
                                    == PolicyStatus.CANCELLED;

                    case EXPIRED, CANCELLED -> false;
                };

        if (!validTransition) {
            throw new InvalidPolicyOperationException(
                    "Policy status cannot change from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }

        if (requestedStatus == PolicyStatus.ACTIVE
                && insurancePolicy
                .getCoverageEndDate()
                .isBefore(LocalDate.now())) {
            throw new InvalidPolicyOperationException(
                    "An expired policy cannot be reactivated"
            );
        }
    }

    private PolicyStatus resolveEffectiveStatus(
            InsurancePolicy insurancePolicy
    ) {

        PolicyStatus currentStatus =
                insurancePolicy.getStatus();

        if (currentStatus == PolicyStatus.ACTIVE
                || currentStatus == PolicyStatus.SUSPENDED) {

            LocalDate coverageEndDate =
                    insurancePolicy.getCoverageEndDate();

            if (coverageEndDate != null
                    && coverageEndDate.isBefore(LocalDate.now())) {
                return PolicyStatus.EXPIRED;
            }
        }

        return currentStatus;
    }

    private PolicyResponse mapToResponse(
            InsurancePolicy insurancePolicy
    ) {

        InsuranceEnrollment enrollment =
                insurancePolicy.getEnrollment();

        InsurancePlan insurancePlan =
                insurancePolicy.getInsurancePlan();

        PayerOrganization payerOrganization =
                insurancePolicy.getPayerOrganization();

        PolicyStatus effectiveStatus =
                resolveEffectiveStatus(insurancePolicy);

        return PolicyResponse.builder()
                .id(insurancePolicy.getId())
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
                .planId(
                        insurancePlan.getId()
                )
                .planName(
                        insurancePolicy.getPlanName()
                )
                .planCode(
                        insurancePolicy.getPlanCode()
                )
                .payerId(
                        payerOrganization.getId()
                )
                .payerOrganizationName(
                        payerOrganization.getOrganizationName()
                )
                .coverageAmount(
                        insurancePolicy.getCoverageAmount()
                )
                .monthlyPremium(
                        insurancePolicy.getMonthlyPremium()
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
                .policyDurationMonths(
                        insurancePolicy.getPolicyDurationMonths()
                )
                .waitingPeriodDays(
                        insurancePolicy.getWaitingPeriodDays()
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
                .status(effectiveStatus)
                .createdAt(
                        insurancePolicy.getCreatedAt()
                )
                .updatedAt(
                        insurancePolicy.getUpdatedAt()
                )
                .build();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidPolicyOperationException(
                    "Authenticated email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}