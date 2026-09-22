package com.naveen.healthcare.claimservice.serviceimpl;

import com.naveen.healthcare.claimservice.client.PayersServiceClient;
import com.naveen.healthcare.claimservice.client.ProviderServiceClient;
import com.naveen.healthcare.claimservice.dto.request.ApproveClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.CancelClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.ClaimVerificationTestRequest;
import com.naveen.healthcare.claimservice.dto.request.RejectClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.SubmitClaimRequest;
import com.naveen.healthcare.claimservice.dto.response.ClaimAppointmentVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.ClaimMedicalRecordVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.ClaimResponse;
import com.naveen.healthcare.claimservice.dto.response.CombinedClaimVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.PayerIdentityResponse;
import com.naveen.healthcare.claimservice.dto.response.ProviderIdentityResponse;
import com.naveen.healthcare.claimservice.entity.Claim;
import com.naveen.healthcare.claimservice.enums.ClaimStatus;
import com.naveen.healthcare.claimservice.exception.ClaimNotFoundException;
import com.naveen.healthcare.claimservice.exception.DuplicateClaimException;
import com.naveen.healthcare.claimservice.exception.InvalidClaimOperationException;
import com.naveen.healthcare.claimservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.claimservice.repository.ClaimRepository;
import com.naveen.healthcare.claimservice.service.ClaimService;
import com.naveen.healthcare.claimservice.service.ClaimVerificationService;
import com.naveen.healthcare.claimservice.util.ClaimNumberGenerator;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private static final int CLAIM_NUMBER_GENERATION_ATTEMPTS = 5;

    private final ClaimRepository claimRepository;

    private final ClaimVerificationService claimVerificationService;

    private final ClaimNumberGenerator claimNumberGenerator;

    private final ProviderServiceClient providerServiceClient;

    private final PayersServiceClient payersServiceClient;

    @Override
    @Transactional
    public ClaimResponse submitClaim(
            SubmitClaimRequest request
    ) {

        validateSubmitRequest(request);

        validateClaimDoesNotAlreadyExist(
                request.getAppointmentId(),
                request.getMedicalRecordId()
        );

        ClaimVerificationTestRequest verificationRequest =
                new ClaimVerificationTestRequest();

        verificationRequest.setAppointmentId(
                request.getAppointmentId()
        );

        verificationRequest.setMedicalRecordId(
                request.getMedicalRecordId()
        );

        verificationRequest.setPolicyId(
                request.getPolicyId()
        );

        CombinedClaimVerificationResponse verification =
                claimVerificationService
                        .verifyClaimRequirements(
                                verificationRequest
                        );

        validateCombinedVerification(verification);

        ClaimAppointmentVerificationResponse appointment =
                verification.getAppointment();

        ClaimMedicalRecordVerificationResponse medicalRecord =
                verification.getMedicalRecord();

        ClaimPolicyVerificationResponse policy =
                verification.getPolicy();

        validateClaimedAmount(
                request.getClaimedAmount(),
                appointment.getConsultationFee(),
                policy.getCoverageAmount()
        );

        Claim claim =
                buildClaim(
                        request,
                        appointment,
                        medicalRecord,
                        policy
                );

        try {

            Claim savedClaim =
                    claimRepository.saveAndFlush(claim);

            return mapToResponse(savedClaim);

        } catch (DataIntegrityViolationException exception) {

            throw new DuplicateClaimException(
                    "A Claim already exists for the selected "
                            + "appointment or Medical Record"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getProviderClaims(
            String authenticatedProviderEmail
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveProviderIdentity(
                        authenticatedProviderEmail
                );

        return claimRepository
                .findAllByProviderIdOrderBySubmittedAtDesc(
                        providerIdentity.getProviderId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getProviderClaimById(
            String authenticatedProviderEmail,
            UUID claimId
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveProviderIdentity(
                        authenticatedProviderEmail
                );

        Claim claim =
                findProviderOwnedClaim(
                        providerIdentity.getProviderId(),
                        claimId
                );

        return mapToResponse(claim);
    }

    @Override
    @Transactional
    public ClaimResponse cancelProviderClaim(
            String authenticatedProviderEmail,
            UUID claimId,
            CancelClaimRequest request
    ) {

        validateCancelClaimRequest(request);

        ProviderIdentityResponse providerIdentity =
                retrieveProviderIdentity(
                        authenticatedProviderEmail
                );

        Claim claim =
                findProviderOwnedClaim(
                        providerIdentity.getProviderId(),
                        claimId
                );

        if (claim.getStatus() != ClaimStatus.SUBMITTED) {

            throw new InvalidClaimOperationException(
                    "Only a SUBMITTED claim can be cancelled "
                            + "by the Provider"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        claim.setStatus(ClaimStatus.CANCELLED);

        claim.setCancellationReason(
                normalizeRequiredText(
                        request.getCancellationReason(),
                        "Cancellation reason"
                )
        );

        claim.setApprovedAmount(null);
        claim.setCancelledAt(now);
        claim.setResolvedAt(now);

        Claim updatedClaim =
                claimRepository.save(claim);

        return mapToResponse(updatedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getPayerClaims(
            String authenticatedPayerEmail
    ) {

        PayerIdentityResponse payerIdentity =
                retrievePayerIdentity(
                        authenticatedPayerEmail
                );

        return claimRepository
                .findAllByPayerIdOrderBySubmittedAtDesc(
                        payerIdentity.getPayerId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getPayerClaimById(
            String authenticatedPayerEmail,
            UUID claimId
    ) {

        PayerIdentityResponse payerIdentity =
                retrievePayerIdentity(
                        authenticatedPayerEmail
                );

        Claim claim =
                findPayerOwnedClaim(
                        payerIdentity.getPayerId(),
                        claimId
                );

        return mapToResponse(claim);
    }

    @Override
    @Transactional
    public ClaimResponse startClaimReview(
            String authenticatedPayerEmail,
            UUID claimId
    ) {

        PayerIdentityResponse payerIdentity =
                retrievePayerIdentity(
                        authenticatedPayerEmail
                );

        Claim claim =
                findPayerOwnedClaim(
                        payerIdentity.getPayerId(),
                        claimId
                );

        if (claim.getStatus() != ClaimStatus.SUBMITTED) {

            throw new InvalidClaimOperationException(
                    "Only a SUBMITTED claim can move "
                            + "to UNDER_REVIEW"
            );
        }

        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setReviewedAt(LocalDateTime.now());

        Claim updatedClaim =
                claimRepository.save(claim);

        return mapToResponse(updatedClaim);
    }

    @Override
    @Transactional
    public ClaimResponse approveClaim(
            String authenticatedPayerEmail,
            UUID claimId,
            ApproveClaimRequest request
    ) {

        validateApproveClaimRequest(request);

        PayerIdentityResponse payerIdentity =
                retrievePayerIdentity(
                        authenticatedPayerEmail
                );

        Claim claim =
                findPayerOwnedClaim(
                        payerIdentity.getPayerId(),
                        claimId
                );

        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {

            throw new InvalidClaimOperationException(
                    "Only an UNDER_REVIEW claim can be approved"
            );
        }

        validateApprovedAmount(
                request.getApprovedAmount(),
                claim
        );

        LocalDateTime now = LocalDateTime.now();

        claim.setApprovedAmount(
                request.getApprovedAmount()
        );

        claim.setReviewRemarks(
                normalizeRequiredText(
                        request.getReviewRemarks(),
                        "Review remarks"
                )
        );

        claim.setRejectionReason(null);
        claim.setCancellationReason(null);
        claim.setStatus(ClaimStatus.APPROVED);

        if (claim.getReviewedAt() == null) {
            claim.setReviewedAt(now);
        }

        claim.setResolvedAt(now);

        Claim updatedClaim =
                claimRepository.save(claim);

        return mapToResponse(updatedClaim);
    }

    @Override
    @Transactional
    public ClaimResponse rejectClaim(
            String authenticatedPayerEmail,
            UUID claimId,
            RejectClaimRequest request
    ) {

        validateRejectClaimRequest(request);

        PayerIdentityResponse payerIdentity =
                retrievePayerIdentity(
                        authenticatedPayerEmail
                );

        Claim claim =
                findPayerOwnedClaim(
                        payerIdentity.getPayerId(),
                        claimId
                );

        if (claim.getStatus() != ClaimStatus.UNDER_REVIEW) {

            throw new InvalidClaimOperationException(
                    "Only an UNDER_REVIEW claim can be rejected"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        claim.setApprovedAmount(null);

        claim.setRejectionReason(
                normalizeRequiredText(
                        request.getRejectionReason(),
                        "Rejection reason"
                )
        );

        claim.setReviewRemarks(
                normalizeRequiredText(
                        request.getReviewRemarks(),
                        "Review remarks"
                )
        );

        claim.setCancellationReason(null);
        claim.setStatus(ClaimStatus.REJECTED);

        if (claim.getReviewedAt() == null) {
            claim.setReviewedAt(now);
        }

        claim.setResolvedAt(now);

        Claim updatedClaim =
                claimRepository.save(claim);

        return mapToResponse(updatedClaim);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaimResponse> getMyClaims(
            String authenticatedMemberEmail
    ) {

        String normalizedMemberEmail =
                normalizeEmail(
                        authenticatedMemberEmail,
                        "Authenticated Member email"
                );

        return claimRepository
                .findAllByMemberEmailIgnoreCaseOrderBySubmittedAtDesc(
                        normalizedMemberEmail
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaimResponse getMyClaimById(
            String authenticatedMemberEmail,
            UUID claimId
    ) {

        validateClaimId(claimId);

        String normalizedMemberEmail =
                normalizeEmail(
                        authenticatedMemberEmail,
                        "Authenticated Member email"
                );

        Claim claim =
                claimRepository
                        .findByIdAndMemberEmailIgnoreCase(
                                claimId,
                                normalizedMemberEmail
                        )
                        .orElseThrow(() ->
                                new ClaimNotFoundException(
                                        "Claim not found for "
                                                + "the authenticated Member"
                                )
                        );

        return mapToResponse(claim);
    }

    private Claim buildClaim(
            SubmitClaimRequest request,
            ClaimAppointmentVerificationResponse appointment,
            ClaimMedicalRecordVerificationResponse medicalRecord,
            ClaimPolicyVerificationResponse policy
    ) {

        return Claim.builder()
                .claimNumber(
                        generateUniqueClaimNumber()
                )
                .appointmentId(
                        appointment.getAppointmentId()
                )
                .medicalRecordId(
                        medicalRecord.getMedicalRecordId()
                )
                .policyId(
                        policy.getPolicyId()
                )
                .policyNumber(
                        normalizeRequiredText(
                                policy.getPolicyNumber(),
                                "Policy number"
                        )
                )
                .memberId(
                        appointment.getMemberId()
                )
                .memberEmail(
                        normalizeEmail(
                                appointment.getMemberEmail(),
                                "Member email"
                        )
                )
                .providerId(
                        appointment.getProviderId()
                )
                .providerOrganizationName(
                        normalizeRequiredText(
                                appointment
                                        .getProviderOrganizationName(),
                                "Provider organization name"
                        )
                )
                .departmentId(
                        appointment.getDepartmentId()
                )
                .departmentName(
                        normalizeRequiredText(
                                appointment.getDepartmentName(),
                                "Department name"
                        )
                )
                .doctorId(
                        appointment.getDoctorId()
                )
                .doctorName(
                        normalizeRequiredText(
                                appointment.getDoctorName(),
                                "Doctor name"
                        )
                )
                .doctorSpecialization(
                        normalizeRequiredText(
                                appointment
                                        .getDoctorSpecialization(),
                                "Doctor specialization"
                        )
                )
                .payerId(
                        policy.getPayerId()
                )
                .payerOrganizationName(
                        normalizeRequiredText(
                                policy.getPayerOrganizationName(),
                                "Payer organization name"
                        )
                )
                .planId(
                        policy.getPlanId()
                )
                .planCode(
                        normalizeRequiredText(
                                policy.getPlanCode(),
                                "Plan code"
                        )
                )
                .planName(
                        normalizeRequiredText(
                                policy.getPlanName(),
                                "Plan name"
                        )
                )
                .serviceDate(
                        appointment.getAppointmentDate()
                )
                .consultationFee(
                        appointment.getConsultationFee()
                )
                .claimedAmount(
                        request.getClaimedAmount()
                )
                .approvedAmount(null)
                .coverageAmount(
                        policy.getCoverageAmount()
                )
                .annualDeductible(
                        requireNonNegativeAmount(
                                policy.getAnnualDeductible(),
                                "Annual deductible"
                        )
                )
                .copayPercentage(
                        requirePercentage(
                                policy.getCopayPercentage(),
                                "Copay percentage"
                        )
                )
                .coinsurancePercentage(
                        requirePercentage(
                                policy.getCoinsurancePercentage(),
                                "Coinsurance percentage"
                        )
                )
                .policyCoverageStartDate(
                        policy.getCoverageStartDate()
                )
                .policyCoverageEndDate(
                        policy.getCoverageEndDate()
                )
                .diagnosisSnapshot(
                        normalizeRequiredText(
                                medicalRecord.getDiagnosis(),
                                "Diagnosis"
                        )
                )
                .treatmentPlanSnapshot(
                        normalizeRequiredText(
                                medicalRecord.getTreatmentPlan(),
                                "Treatment plan"
                        )
                )
                .claimDescription(
                        normalizeRequiredText(
                                request.getClaimDescription(),
                                "Claim description"
                        )
                )
                .status(ClaimStatus.SUBMITTED)
                .build();
    }

    private ProviderIdentityResponse retrieveProviderIdentity(
            String authenticatedProviderEmail
    ) {

        String normalizedProviderEmail =
                normalizeEmail(
                        authenticatedProviderEmail,
                        "Authenticated Provider email"
                );

        ProviderIdentityResponse response;

        try {

            response =
                    providerServiceClient
                            .getAuthenticatedProviderIdentity();

        } catch (RetryableException exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider identity verification is "
                            + "temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            handleProviderIdentityFeignFailure(exception);

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected response",
                    exception
            );
        }

        validateProviderIdentityResponse(
                response,
                normalizedProviderEmail
        );

        return response;
    }

    private PayerIdentityResponse retrievePayerIdentity(
            String authenticatedPayerEmail
    ) {

        String normalizedPayerEmail =
                normalizeEmail(
                        authenticatedPayerEmail,
                        "Authenticated Payer email"
                );

        PayerIdentityResponse response;

        try {

            response =
                    payersServiceClient
                            .getAuthenticatedPayerIdentity();

        } catch (RetryableException exception) {

            throw new RemoteServiceCommunicationException(
                    "Payer identity verification is "
                            + "temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            handlePayerIdentityFeignFailure(exception);

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned an unexpected response",
                    exception
            );
        }

        validatePayerIdentityResponse(
                response,
                normalizedPayerEmail
        );

        return response;
    }

    private void handleProviderIdentityFeignFailure(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400 || status == 404) {

            throw new InvalidClaimOperationException(
                    "An ACTIVE Provider organization is required "
                            + "to manage claims"
            );
        }

        if (status == 401) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service rejected the forwarded "
                            + "authentication token",
                    exception
            );
        }

        if (status == 403) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service rejected the forwarded "
                            + "Provider authorization",
                    exception
            );
        }

        if (status == 503) {

            throw new RemoteServiceCommunicationException(
                    "Provider identity verification is "
                            + "temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Provider Service returned an unexpected response",
                exception
        );
    }

    private void handlePayerIdentityFeignFailure(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400 || status == 404) {

            throw new InvalidClaimOperationException(
                    "An ACTIVE Payer organization is required "
                            + "to manage claims"
            );
        }

        if (status == 401) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service rejected the forwarded "
                            + "authentication token",
                    exception
            );
        }

        if (status == 403) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service rejected the forwarded "
                            + "Payer authorization",
                    exception
            );
        }

        if (status == 503) {

            throw new RemoteServiceCommunicationException(
                    "Payer identity verification is "
                            + "temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Payers Service returned an unexpected response",
                exception
        );
    }

    private void validateProviderIdentityResponse(
            ProviderIdentityResponse response,
            String authenticatedProviderEmail
    ) {

        if (response == null) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an empty identity response"
            );
        }

        if (response.getProviderId() == null) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Provider ID"
            );
        }

        String returnedEmail =
                normalizeEmail(
                        response.getAdminEmail(),
                        "Provider administrator email"
                );

        if (!authenticatedProviderEmail.equals(returnedEmail)) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned identity information "
                            + "for an unexpected Provider"
            );
        }

        normalizeRequiredText(
                response.getProviderOrganizationName(),
                "Provider organization name"
        );

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                response.getStatus()
        )) {

            throw new InvalidClaimOperationException(
                    "Only an ACTIVE Provider organization "
                            + "can manage claims"
            );
        }
    }

    private void validatePayerIdentityResponse(
            PayerIdentityResponse response,
            String authenticatedPayerEmail
    ) {

        if (response == null) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned an empty identity response"
            );
        }

        if (response.getPayerId() == null) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service response does not contain "
                            + "a Payer ID"
            );
        }

        String returnedEmail =
                normalizeEmail(
                        response.getAdminEmail(),
                        "Payer administrator email"
                );

        if (!authenticatedPayerEmail.equals(returnedEmail)) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned identity information "
                            + "for an unexpected Payer"
            );
        }

        normalizeRequiredText(
                response.getPayerOrganizationName(),
                "Payer organization name"
        );

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                response.getStatus()
        )) {

            throw new InvalidClaimOperationException(
                    "Only an ACTIVE Payer organization "
                            + "can manage claims"
            );
        }
    }

    private Claim findProviderOwnedClaim(
            UUID providerId,
            UUID claimId
    ) {

        validateClaimId(claimId);

        if (providerId == null) {

            throw new InvalidClaimOperationException(
                    "Provider ID is required"
            );
        }

        return claimRepository
                .findByIdAndProviderId(
                        claimId,
                        providerId
                )
                .orElseThrow(() ->
                        new ClaimNotFoundException(
                                "Claim not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private Claim findPayerOwnedClaim(
            UUID payerId,
            UUID claimId
    ) {

        validateClaimId(claimId);

        if (payerId == null) {

            throw new InvalidClaimOperationException(
                    "Payer ID is required"
            );
        }

        return claimRepository
                .findByIdAndPayerId(
                        claimId,
                        payerId
                )
                .orElseThrow(() ->
                        new ClaimNotFoundException(
                                "Claim not found for "
                                        + "the authenticated Payer"
                        )
                );
    }

    private void validateSubmitRequest(
            SubmitClaimRequest request
    ) {

        if (request == null) {

            throw new InvalidClaimOperationException(
                    "Claim submission request is required"
            );
        }

        if (request.getAppointmentId() == null) {

            throw new InvalidClaimOperationException(
                    "Appointment ID is required"
            );
        }

        if (request.getMedicalRecordId() == null) {

            throw new InvalidClaimOperationException(
                    "Medical Record ID is required"
            );
        }

        if (request.getPolicyId() == null) {

            throw new InvalidClaimOperationException(
                    "Policy ID is required"
            );
        }

        if (request.getClaimedAmount() == null
                || request.getClaimedAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new InvalidClaimOperationException(
                    "Claimed amount must be greater than zero"
            );
        }

        normalizeRequiredText(
                request.getClaimDescription(),
                "Claim description"
        );
    }

    private void validateCancelClaimRequest(
            CancelClaimRequest request
    ) {

        if (request == null) {

            throw new InvalidClaimOperationException(
                    "Claim cancellation request is required"
            );
        }

        normalizeRequiredText(
                request.getCancellationReason(),
                "Cancellation reason"
        );
    }

    private void validateApproveClaimRequest(
            ApproveClaimRequest request
    ) {

        if (request == null) {

            throw new InvalidClaimOperationException(
                    "Claim approval request is required"
            );
        }

        if (request.getApprovedAmount() == null) {

            throw new InvalidClaimOperationException(
                    "Approved amount is required"
            );
        }

        normalizeRequiredText(
                request.getReviewRemarks(),
                "Review remarks"
        );
    }

    private void validateRejectClaimRequest(
            RejectClaimRequest request
    ) {

        if (request == null) {

            throw new InvalidClaimOperationException(
                    "Claim rejection request is required"
            );
        }

        normalizeRequiredText(
                request.getRejectionReason(),
                "Rejection reason"
        );

        normalizeRequiredText(
                request.getReviewRemarks(),
                "Review remarks"
        );
    }

    private void validateClaimDoesNotAlreadyExist(
            UUID appointmentId,
            UUID medicalRecordId
    ) {

        if (claimRepository.existsByAppointmentId(
                appointmentId
        )) {

            throw new DuplicateClaimException(
                    "A Claim already exists for "
                            + "the selected appointment"
            );
        }

        if (claimRepository.existsByMedicalRecordId(
                medicalRecordId
        )) {

            throw new DuplicateClaimException(
                    "A Claim already exists for "
                            + "the selected Medical Record"
            );
        }
    }

    private void validateCombinedVerification(
            CombinedClaimVerificationResponse verification
    ) {

        if (verification == null) {

            throw new RemoteServiceCommunicationException(
                    "Claim verification returned an empty response"
            );
        }

        ClaimAppointmentVerificationResponse appointment =
                verification.getAppointment();

        ClaimMedicalRecordVerificationResponse medicalRecord =
                verification.getMedicalRecord();

        ClaimPolicyVerificationResponse policy =
                verification.getPolicy();

        if (appointment == null
                || medicalRecord == null
                || policy == null) {

            throw new RemoteServiceCommunicationException(
                    "Claim verification returned incomplete information"
            );
        }

        compareUuid(
                appointment.getAppointmentId(),
                medicalRecord.getAppointmentId(),
                "Appointment and Medical Record appointment IDs do not match"
        );

        compareUuid(
                appointment.getMemberId(),
                medicalRecord.getMemberId(),
                "Appointment and Medical Record Member IDs do not match"
        );

        compareUuid(
                appointment.getMemberId(),
                policy.getMemberId(),
                "Appointment and Policy Member IDs do not match"
        );

        compareUuid(
                appointment.getProviderId(),
                medicalRecord.getProviderId(),
                "Appointment and Medical Record Provider IDs do not match"
        );

        compareUuid(
                appointment.getDepartmentId(),
                medicalRecord.getDepartmentId(),
                "Appointment and Medical Record Department IDs do not match"
        );

        compareUuid(
                appointment.getDoctorId(),
                medicalRecord.getDoctorId(),
                "Appointment and Medical Record Doctor IDs do not match"
        );

        compareDate(
                appointment.getAppointmentDate(),
                medicalRecord.getAppointmentDate(),
                "Appointment and Medical Record dates do not match"
        );

        compareDate(
                appointment.getAppointmentDate(),
                policy.getServiceDate(),
                "Appointment date and Policy service date do not match"
        );

        compareEmail(
                appointment.getMemberEmail(),
                medicalRecord.getMemberEmail(),
                "Appointment and Medical Record Member emails do not match"
        );

        compareEmail(
                appointment.getMemberEmail(),
                policy.getMemberEmail(),
                "Appointment and Policy Member emails do not match"
        );

        if (!Boolean.TRUE.equals(
                policy.getCoverageValid()
        )) {

            throw new InvalidClaimOperationException(
                    "The selected policy does not cover "
                            + "the appointment service date"
            );
        }
    }

    private void validateClaimedAmount(
            BigDecimal claimedAmount,
            BigDecimal consultationFee,
            BigDecimal coverageAmount
    ) {

        if (claimedAmount == null
                || claimedAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new InvalidClaimOperationException(
                    "Claimed amount must be greater than zero"
            );
        }

        if (consultationFee == null
                || consultationFee.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new InvalidClaimOperationException(
                    "The verified appointment contains "
                            + "an invalid consultation fee"
            );
        }

        if (coverageAmount == null
                || coverageAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new InvalidClaimOperationException(
                    "The verified policy contains "
                            + "an invalid coverage amount"
            );
        }

        if (claimedAmount.compareTo(
                consultationFee
        ) > 0) {

            throw new InvalidClaimOperationException(
                    "Claimed amount cannot exceed "
                            + "the verified consultation fee"
            );
        }

        if (claimedAmount.compareTo(
                coverageAmount
        ) > 0) {

            throw new InvalidClaimOperationException(
                    "Claimed amount cannot exceed "
                            + "the policy coverage amount"
            );
        }
    }

    private void validateApprovedAmount(
            BigDecimal approvedAmount,
            Claim claim
    ) {

        if (approvedAmount == null
                || approvedAmount.compareTo(
                BigDecimal.ZERO
        ) <= 0) {

            throw new InvalidClaimOperationException(
                    "Approved amount must be greater than zero"
            );
        }

        if (claim.getClaimedAmount() == null) {

            throw new InvalidClaimOperationException(
                    "The Claim does not contain a valid claimed amount"
            );
        }

        if (approvedAmount.compareTo(
                claim.getClaimedAmount()
        ) > 0) {

            throw new InvalidClaimOperationException(
                    "Approved amount cannot exceed claimed amount"
            );
        }

        if (claim.getCoverageAmount() == null) {

            throw new InvalidClaimOperationException(
                    "The Claim does not contain a valid coverage amount"
            );
        }

        if (approvedAmount.compareTo(
                claim.getCoverageAmount()
        ) > 0) {

            throw new InvalidClaimOperationException(
                    "Approved amount cannot exceed "
                            + "the policy coverage amount"
            );
        }
    }

    private String generateUniqueClaimNumber() {

        for (
                int attempt = 0;
                attempt < CLAIM_NUMBER_GENERATION_ATTEMPTS;
                attempt++
        ) {

            String claimNumber =
                    claimNumberGenerator.generateClaimNumber();

            if (!claimRepository
                    .existsByClaimNumberIgnoreCase(
                            claimNumber
                    )) {

                return claimNumber;
            }
        }

        throw new InvalidClaimOperationException(
                "Unable to generate a unique Claim number"
        );
    }

    private void validateClaimId(
            UUID claimId
    ) {

        if (claimId == null) {

            throw new InvalidClaimOperationException(
                    "Claim ID is required"
            );
        }
    }

    private ClaimResponse mapToResponse(
            Claim claim
    ) {

        return ClaimResponse.builder()
                .id(claim.getId())
                .claimNumber(claim.getClaimNumber())
                .appointmentId(claim.getAppointmentId())
                .medicalRecordId(claim.getMedicalRecordId())
                .policyId(claim.getPolicyId())
                .policyNumber(claim.getPolicyNumber())
                .memberId(claim.getMemberId())
                .memberEmail(claim.getMemberEmail())
                .providerId(claim.getProviderId())
                .providerOrganizationName(
                        claim.getProviderOrganizationName()
                )
                .departmentId(claim.getDepartmentId())
                .departmentName(claim.getDepartmentName())
                .doctorId(claim.getDoctorId())
                .doctorName(claim.getDoctorName())
                .doctorSpecialization(
                        claim.getDoctorSpecialization()
                )
                .payerId(claim.getPayerId())
                .payerOrganizationName(
                        claim.getPayerOrganizationName()
                )
                .planId(claim.getPlanId())
                .planCode(claim.getPlanCode())
                .planName(claim.getPlanName())
                .serviceDate(claim.getServiceDate())
                .consultationFee(claim.getConsultationFee())
                .claimedAmount(claim.getClaimedAmount())
                .approvedAmount(claim.getApprovedAmount())
                .coverageAmount(claim.getCoverageAmount())
                .annualDeductible(
                        claim.getAnnualDeductible()
                )
                .copayPercentage(
                        claim.getCopayPercentage()
                )
                .coinsurancePercentage(
                        claim.getCoinsurancePercentage()
                )
                .policyCoverageStartDate(
                        claim.getPolicyCoverageStartDate()
                )
                .policyCoverageEndDate(
                        claim.getPolicyCoverageEndDate()
                )
                .diagnosisSnapshot(
                        claim.getDiagnosisSnapshot()
                )
                .treatmentPlanSnapshot(
                        claim.getTreatmentPlanSnapshot()
                )
                .claimDescription(
                        claim.getClaimDescription()
                )
                .status(claim.getStatus())
                .reviewRemarks(claim.getReviewRemarks())
                .rejectionReason(claim.getRejectionReason())
                .cancellationReason(
                        claim.getCancellationReason()
                )
                .submittedAt(claim.getSubmittedAt())
                .reviewedAt(claim.getReviewedAt())
                .resolvedAt(claim.getResolvedAt())
                .cancelledAt(claim.getCancelledAt())
                .createdAt(claim.getCreatedAt())
                .updatedAt(claim.getUpdatedAt())
                .build();
    }

    private BigDecimal requireNonNegativeAmount(
            BigDecimal value,
            String fieldName
    ) {

        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new InvalidClaimOperationException(
                    fieldName + " must be zero or greater"
            );
        }

        return value;
    }

    private BigDecimal requirePercentage(
            BigDecimal value,
            String fieldName
    ) {

        if (value == null
                || value.compareTo(
                BigDecimal.ZERO
        ) < 0
                || value.compareTo(
                BigDecimal.valueOf(100)
        ) > 0) {

            throw new InvalidClaimOperationException(
                    fieldName + " must be between 0 and 100"
            );
        }

        return value;
    }

    private void compareUuid(
            UUID first,
            UUID second,
            String message
    ) {

        if (first == null
                || second == null
                || !first.equals(second)) {

            throw new InvalidClaimOperationException(message);
        }
    }

    private void compareDate(
            LocalDate first,
            LocalDate second,
            String message
    ) {

        if (first == null
                || second == null
                || !first.equals(second)) {

            throw new InvalidClaimOperationException(message);
        }
    }

    private void compareEmail(
            String first,
            String second,
            String message
    ) {

        if (first == null
                || second == null
                || !first
                .trim()
                .equalsIgnoreCase(second.trim())) {

            throw new InvalidClaimOperationException(message);
        }
    }

    private String normalizeEmail(
            String email,
            String fieldName
    ) {

        if (email == null || email.isBlank()) {

            throw new InvalidClaimOperationException(
                    fieldName + " is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new InvalidClaimOperationException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }
}