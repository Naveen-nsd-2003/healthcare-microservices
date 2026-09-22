package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.request.CreateEnrollmentRequest;
import com.naveen.healthcare.payersservice.dto.request.ReviewEnrollmentRequest;
import com.naveen.healthcare.payersservice.dto.response.EnrollmentResponse;
import com.naveen.healthcare.payersservice.dto.response.MemberVerificationResponse;
import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.EnrollmentStatus;
import com.naveen.healthcare.payersservice.enums.InsurancePlanStatus;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import com.naveen.healthcare.payersservice.exception.DuplicateEnrollmentException;
import com.naveen.healthcare.payersservice.exception.EnrollmentNotFoundException;
import com.naveen.healthcare.payersservice.exception.InsurancePlanNotFoundException;
import com.naveen.healthcare.payersservice.exception.InvalidEnrollmentOperationException;
import com.naveen.healthcare.payersservice.exception.InvalidPayerStatusException;
import com.naveen.healthcare.payersservice.exception.PayerNotFoundException;
import com.naveen.healthcare.payersservice.repository.InsuranceEnrollmentRepository;
import com.naveen.healthcare.payersservice.repository.InsurancePlanRepository;
import com.naveen.healthcare.payersservice.repository.PayerRepository;
import com.naveen.healthcare.payersservice.service.EnrollmentService;
import com.naveen.healthcare.payersservice.service.MemberVerificationService;
import com.naveen.healthcare.payersservice.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final List<EnrollmentStatus>
            DUPLICATE_BLOCKING_STATUSES = List.of(
            EnrollmentStatus.PENDING,
            EnrollmentStatus.APPROVED
    );

    private final InsuranceEnrollmentRepository
            insuranceEnrollmentRepository;

    private final InsurancePlanRepository
            insurancePlanRepository;

    private final PayerRepository payerRepository;

    private final MemberVerificationService
            memberVerificationService;

    private final PolicyService policyService;

    @Override
    @Transactional
    public EnrollmentResponse createEnrollment(
            String authenticatedMemberEmail,
            CreateEnrollmentRequest request
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        validateRequestedCoverageStartDate(
                request.getRequestedCoverageStartDate()
        );

        MemberVerificationResponse memberVerification =
                memberVerificationService.verifyActiveMember(
                        normalizedMemberEmail
                );

        InsurancePlan insurancePlan =
                findActiveInsurancePlan(
                        request.getPlanId()
                );

        PayerOrganization payerOrganization =
                insurancePlan.getPayerOrganization();

        validatePayerOrganizationIsActive(
                payerOrganization
        );

        validateMemberAgeEligibility(
                memberVerification.getDateOfBirth(),
                request.getRequestedCoverageStartDate(),
                insurancePlan
        );

        validateDuplicateEnrollment(
                memberVerification.getMemberId(),
                insurancePlan.getId()
        );

        InsuranceEnrollment enrollment =
                InsuranceEnrollment.builder()
                        .memberId(
                                memberVerification.getMemberId()
                        )
                        .memberEmail(
                                normalizeEmail(
                                        memberVerification.getEmail()
                                )
                        )
                        .insurancePlan(
                                insurancePlan
                        )
                        .payerOrganization(
                                payerOrganization
                        )
                        .requestedCoverageStartDate(
                                request.getRequestedCoverageStartDate()
                        )
                        .nomineeName(
                                normalizeRequiredText(
                                        request.getNomineeName()
                                )
                        )
                        .nomineeRelationship(
                                request.getNomineeRelationship()
                        )
                        .nomineeContactNumber(
                                normalizeRequiredText(
                                        request.getNomineeContactNumber()
                                )
                        )
                        .status(
                                EnrollmentStatus.PENDING
                        )
                        .build();

        InsuranceEnrollment savedEnrollment =
                insuranceEnrollmentRepository.save(enrollment);

        return mapToResponse(savedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(
            String authenticatedMemberEmail
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        return insuranceEnrollmentRepository
                .findAllByMemberEmailIgnoreCaseOrderByCreatedAtDesc(
                        normalizedMemberEmail
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getMyEnrollmentById(
            String authenticatedMemberEmail,
            UUID enrollmentId
    ) {

        InsuranceEnrollment enrollment =
                findEnrollmentOwnedByMember(
                        authenticatedMemberEmail,
                        enrollmentId
                );

        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse cancelMyEnrollment(
            String authenticatedMemberEmail,
            UUID enrollmentId
    ) {

        InsuranceEnrollment enrollment =
                findEnrollmentOwnedByMember(
                        authenticatedMemberEmail,
                        enrollmentId
                );

        validateEnrollmentIsPending(
                enrollment,
                "Only a PENDING enrollment can be cancelled"
        );

        enrollment.setStatus(
                EnrollmentStatus.CANCELLED
        );

        enrollment.setReviewRemarks(null);
        enrollment.setReviewedAt(null);

        InsuranceEnrollment updatedEnrollment =
                insuranceEnrollmentRepository.save(enrollment);

        return mapToResponse(updatedEnrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getPayerEnrollments(
            String authenticatedPayerEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerOrganizationByEmail(
                        authenticatedPayerEmail
                );

        return insuranceEnrollmentRepository
                .findAllByPayerOrganizationAdminEmailIgnoreCaseOrderByCreatedAtDesc(
                        payerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getPayerEnrollmentById(
            String authenticatedPayerEmail,
            UUID enrollmentId
    ) {

        InsuranceEnrollment enrollment =
                findEnrollmentOwnedByPayer(
                        authenticatedPayerEmail,
                        enrollmentId
                );

        return mapToResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse approveEnrollment(
            String authenticatedPayerEmail,
            UUID enrollmentId,
            ReviewEnrollmentRequest request
    ) {

        PayerOrganization payerOrganization =
                findActivePayerOrganizationByEmail(
                        authenticatedPayerEmail
                );

        InsuranceEnrollment enrollment =
                findEnrollmentOwnedByPayer(
                        payerOrganization.getAdminEmail(),
                        enrollmentId
                );

        validateEnrollmentIsPending(
                enrollment,
                "Only a PENDING enrollment can be approved"
        );

        validatePlanAndPayerStillActive(enrollment);

        enrollment.setStatus(
                EnrollmentStatus.APPROVED
        );

        enrollment.setReviewRemarks(
                normalizeRequiredText(
                        request.getReviewRemarks()
                )
        );

        enrollment.setReviewedAt(
                LocalDateTime.now()
        );

        InsuranceEnrollment approvedEnrollment =
                insuranceEnrollmentRepository.save(enrollment);

        policyService.issuePolicyForApprovedEnrollment(
                approvedEnrollment
        );

        return mapToResponse(approvedEnrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse rejectEnrollment(
            String authenticatedPayerEmail,
            UUID enrollmentId,
            ReviewEnrollmentRequest request
    ) {

        PayerOrganization payerOrganization =
                findActivePayerOrganizationByEmail(
                        authenticatedPayerEmail
                );

        InsuranceEnrollment enrollment =
                findEnrollmentOwnedByPayer(
                        payerOrganization.getAdminEmail(),
                        enrollmentId
                );

        validateEnrollmentIsPending(
                enrollment,
                "Only a PENDING enrollment can be rejected"
        );

        enrollment.setStatus(
                EnrollmentStatus.REJECTED
        );

        enrollment.setReviewRemarks(
                normalizeRequiredText(
                        request.getReviewRemarks()
                )
        );

        enrollment.setReviewedAt(
                LocalDateTime.now()
        );

        InsuranceEnrollment updatedEnrollment =
                insuranceEnrollmentRepository.save(enrollment);

        return mapToResponse(updatedEnrollment);
    }

    private InsurancePlan findActiveInsurancePlan(
            UUID planId
    ) {

        InsurancePlan insurancePlan =
                insurancePlanRepository
                        .findById(planId)
                        .orElseThrow(() ->
                                new InsurancePlanNotFoundException(
                                        "Insurance plan not found with ID: "
                                                + planId
                                )
                        );

        if (insurancePlan.getStatus()
                != InsurancePlanStatus.ACTIVE) {

            throw new InvalidEnrollmentOperationException(
                    "Only an ACTIVE insurance plan "
                            + "can accept enrollments"
            );
        }

        return insurancePlan;
    }

    private PayerOrganization findPayerOrganizationByEmail(
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

    private PayerOrganization findActivePayerOrganizationByEmail(
            String authenticatedPayerEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerOrganizationByEmail(
                        authenticatedPayerEmail
                );

        validatePayerOrganizationIsActive(
                payerOrganization
        );

        return payerOrganization;
    }

    private InsuranceEnrollment findEnrollmentOwnedByMember(
            String authenticatedMemberEmail,
            UUID enrollmentId
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        return insuranceEnrollmentRepository
                .findByIdAndMemberEmailIgnoreCase(
                        enrollmentId,
                        normalizedMemberEmail
                )
                .orElseThrow(() ->
                        new EnrollmentNotFoundException(
                                "Insurance enrollment not found for "
                                        + "the authenticated Member"
                        )
                );
    }

    private InsuranceEnrollment findEnrollmentOwnedByPayer(
            String authenticatedPayerEmail,
            UUID enrollmentId
    ) {

        String normalizedPayerEmail =
                normalizeEmail(authenticatedPayerEmail);

        return insuranceEnrollmentRepository
                .findByIdAndPayerOrganizationAdminEmailIgnoreCase(
                        enrollmentId,
                        normalizedPayerEmail
                )
                .orElseThrow(() ->
                        new EnrollmentNotFoundException(
                                "Insurance enrollment not found for "
                                        + "the authenticated Payer"
                        )
                );
    }

    private void validateRequestedCoverageStartDate(
            LocalDate requestedCoverageStartDate
    ) {

        if (requestedCoverageStartDate == null) {

            throw new InvalidEnrollmentOperationException(
                    "Requested coverage start date is required"
            );
        }

        if (requestedCoverageStartDate.isBefore(
                LocalDate.now()
        )) {

            throw new InvalidEnrollmentOperationException(
                    "Requested coverage start date "
                            + "cannot be in the past"
            );
        }
    }

    private void validatePayerOrganizationIsActive(
            PayerOrganization payerOrganization
    ) {

        if (payerOrganization.getStatus()
                != PayerStatus.ACTIVE) {

            throw new InvalidPayerStatusException(
                    "Payer organization must be ACTIVE "
                            + "to accept insurance enrollments"
            );
        }
    }

    private void validateMemberAgeEligibility(
            LocalDate dateOfBirth,
            LocalDate requestedCoverageStartDate,
            InsurancePlan insurancePlan
    ) {

        if (dateOfBirth == null) {

            throw new InvalidEnrollmentOperationException(
                    "Member date of birth is required "
                            + "for plan eligibility verification"
            );
        }

        if (dateOfBirth.isAfter(
                requestedCoverageStartDate
        )) {

            throw new InvalidEnrollmentOperationException(
                    "Member date of birth is invalid"
            );
        }

        int memberAge =
                Period.between(
                        dateOfBirth,
                        requestedCoverageStartDate
                ).getYears();

        if (memberAge < insurancePlan.getMinimumAge()
                || memberAge > insurancePlan.getMaximumAge()) {

            throw new InvalidEnrollmentOperationException(
                    "Member age "
                            + memberAge
                            + " is not eligible for this plan. "
                            + "Allowed age range is "
                            + insurancePlan.getMinimumAge()
                            + " to "
                            + insurancePlan.getMaximumAge()
            );
        }
    }

    private void validateDuplicateEnrollment(
            UUID memberId,
            UUID planId
    ) {

        boolean duplicateExists =
                insuranceEnrollmentRepository
                        .existsByMemberIdAndInsurancePlanIdAndStatusIn(
                                memberId,
                                planId,
                                DUPLICATE_BLOCKING_STATUSES
                        );

        if (duplicateExists) {

            throw new DuplicateEnrollmentException(
                    "A PENDING or APPROVED enrollment "
                            + "already exists for this insurance plan"
            );
        }
    }

    private void validateEnrollmentIsPending(
            InsuranceEnrollment enrollment,
            String errorMessage
    ) {

        if (enrollment.getStatus()
                != EnrollmentStatus.PENDING) {

            throw new InvalidEnrollmentOperationException(
                    errorMessage
            );
        }
    }

    private void validatePlanAndPayerStillActive(
            InsuranceEnrollment enrollment
    ) {

        InsurancePlan insurancePlan =
                enrollment.getInsurancePlan();

        if (insurancePlan.getStatus()
                != InsurancePlanStatus.ACTIVE) {

            throw new InvalidEnrollmentOperationException(
                    "Enrollment cannot be approved because "
                            + "the insurance plan is not ACTIVE"
            );
        }

        validatePayerOrganizationIsActive(
                enrollment.getPayerOrganization()
        );
    }

    private EnrollmentResponse mapToResponse(
            InsuranceEnrollment enrollment
    ) {

        InsurancePlan insurancePlan =
                enrollment.getInsurancePlan();

        PayerOrganization payerOrganization =
                enrollment.getPayerOrganization();

        return EnrollmentResponse.builder()
                .id(
                        enrollment.getId()
                )
                .memberId(
                        enrollment.getMemberId()
                )
                .memberEmail(
                        enrollment.getMemberEmail()
                )
                .planId(
                        insurancePlan.getId()
                )
                .planCode(
                        insurancePlan.getPlanCode()
                )
                .planName(
                        insurancePlan.getPlanName()
                )
                .coverageAmount(
                        insurancePlan.getCoverageAmount()
                )
                .monthlyPremium(
                        insurancePlan.getMonthlyPremium()
                )
                .payerId(
                        payerOrganization.getId()
                )
                .payerOrganizationName(
                        payerOrganization.getOrganizationName()
                )
                .requestedCoverageStartDate(
                        enrollment.getRequestedCoverageStartDate()
                )
                .nomineeName(
                        enrollment.getNomineeName()
                )
                .nomineeRelationship(
                        enrollment.getNomineeRelationship()
                )
                .nomineeContactNumber(
                        enrollment.getNomineeContactNumber()
                )
                .status(
                        enrollment.getStatus()
                )
                .reviewRemarks(
                        enrollment.getReviewRemarks()
                )
                .reviewedAt(
                        enrollment.getReviewedAt()
                )
                .createdAt(
                        enrollment.getCreatedAt()
                )
                .updatedAt(
                        enrollment.getUpdatedAt()
                )
                .build();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new InvalidEnrollmentOperationException(
                    "Authenticated email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {

            throw new InvalidEnrollmentOperationException(
                    "Required enrollment information cannot be empty"
            );
        }

        return value.trim();
    }
}