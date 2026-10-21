package com.naveen.healthcare.claimservice.serviceimpl;

import com.naveen.healthcare.claimservice.client.AppointmentServiceClient;
import com.naveen.healthcare.claimservice.client.PayersServiceClient;
import com.naveen.healthcare.claimservice.client.ProviderServiceClient;
import com.naveen.healthcare.claimservice.dto.request.ClaimVerificationTestRequest;
import com.naveen.healthcare.claimservice.dto.response.ClaimAppointmentVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.ClaimMedicalRecordVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.CombinedClaimVerificationResponse;
import com.naveen.healthcare.claimservice.exception.ClaimAppointmentVerificationException;
import com.naveen.healthcare.claimservice.exception.ClaimMedicalRecordVerificationException;
import com.naveen.healthcare.claimservice.exception.ClaimPolicyVerificationException;
import com.naveen.healthcare.claimservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.claimservice.service.ClaimVerificationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.UUID;

import com.naveen.healthcare.claimservice.dto.response.ClaimSubmissionContextResponse;
import com.naveen.healthcare.claimservice.dto.response.EligibleClaimPolicyResponse;

@Service
@RequiredArgsConstructor
public class ClaimVerificationServiceImpl
        implements ClaimVerificationService {

    private static final String COMPLETED_STATUS = "COMPLETED";

    private static final String ACTIVE_STATUS = "ACTIVE";

    private static final String AMENDED_STATUS = "AMENDED";

    private final AppointmentServiceClient appointmentServiceClient;

    private final ProviderServiceClient providerServiceClient;

    private final PayersServiceClient payersServiceClient;

    @Override
    public CombinedClaimVerificationResponse verifyClaimRequirements(
            ClaimVerificationTestRequest request
    ) {

        validateRequest(request);

        ClaimAppointmentVerificationResponse appointment =
                retrieveAppointmentVerification(
                        request.getAppointmentId()
                );

        validateAppointmentResponse(
                appointment,
                request.getAppointmentId()
        );

        ClaimMedicalRecordVerificationResponse medicalRecord =
                retrieveMedicalRecordVerification(
                        request.getMedicalRecordId(),
                        request.getAppointmentId()
                );

        validateMedicalRecordResponse(
                medicalRecord,
                request.getMedicalRecordId(),
                request.getAppointmentId()
        );

        crossCheckAppointmentAndMedicalRecord(
                appointment,
                medicalRecord
        );

        ClaimPolicyVerificationResponse policy =
                retrievePolicyVerification(
                        request.getPolicyId(),
                        appointment.getMemberId(),
                        appointment.getAppointmentDate()
                );

        validatePolicyResponse(
                policy,
                request.getPolicyId(),
                appointment.getMemberId(),
                appointment.getAppointmentDate()
        );

        crossCheckPolicyInformation(
                appointment,
                medicalRecord,
                policy
        );

        return CombinedClaimVerificationResponse.builder()
                .message(
                        "Appointment, Medical Record and Policy "
                                + "verification succeeded"
                )
                .appointment(appointment)
                .medicalRecord(medicalRecord)
                .policy(policy)
                .build();
    }

    @Override
    public ClaimSubmissionContextResponse getClaimSubmissionContext(
            UUID appointmentId,
            UUID medicalRecordId
    ) {

        validateSubmissionContextRequest(
                appointmentId,
                medicalRecordId
        );

        ClaimAppointmentVerificationResponse appointment =
                retrieveAppointmentVerification(
                        appointmentId
                );

        validateAppointmentResponse(
                appointment,
                appointmentId
        );

        ClaimMedicalRecordVerificationResponse medicalRecord =
                retrieveMedicalRecordVerification(
                        medicalRecordId,
                        appointmentId
                );

        validateMedicalRecordResponse(
                medicalRecord,
                medicalRecordId,
                appointmentId
        );

        crossCheckAppointmentAndMedicalRecord(
                appointment,
                medicalRecord
        );

        List<EligibleClaimPolicyResponse> eligiblePolicies =
                retrieveEligiblePolicies(
                        appointment.getMemberId(),
                        appointment.getAppointmentDate()
                );

        return ClaimSubmissionContextResponse
                .builder()
                .message(
                        eligiblePolicies.isEmpty()
                                ? "No eligible insurance policy was found "
                                + "for the appointment service date"
                                : "Claim submission context loaded successfully"
                )
                .appointment(
                        appointment
                )
                .medicalRecord(
                        medicalRecord
                )
                .eligiblePolicies(
                        eligiblePolicies
                )
                .build();
    }

    private ClaimAppointmentVerificationResponse
    retrieveAppointmentVerification(
            UUID appointmentId
    ) {

        try {
            return appointmentServiceClient
                    .verifyCompletedAppointmentForClaim(
                            appointmentId
                    );

        } catch (RetryableException exception) {
            throw new RemoteServiceCommunicationException(
                    "Appointment verification is temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {
            handleAppointmentFeignException(exception);

            throw new RemoteServiceCommunicationException(
                    "Appointment Service returned an unexpected response",
                    exception
            );
        }
    }

    private ClaimMedicalRecordVerificationResponse
    retrieveMedicalRecordVerification(
            UUID medicalRecordId,
            UUID appointmentId
    ) {

        try {
            return providerServiceClient
                    .verifyMedicalRecordForClaim(
                            medicalRecordId,
                            appointmentId
                    );

        } catch (RetryableException exception) {
            throw new RemoteServiceCommunicationException(
                    "Medical Record verification is temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {
            handleMedicalRecordFeignException(exception);

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected response",
                    exception
            );
        }
    }

    private ClaimPolicyVerificationResponse
    retrievePolicyVerification(
            UUID policyId,
            UUID memberId,
            LocalDate serviceDate
    ) {

        try {
            return payersServiceClient
                    .verifyPolicyForClaim(
                            policyId,
                            memberId,
                            serviceDate
                    );

        } catch (RetryableException exception) {
            throw new RemoteServiceCommunicationException(
                    "Policy verification is temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {
            handlePolicyFeignException(exception);

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned an unexpected response",
                    exception
            );
        }
    }

    private List<EligibleClaimPolicyResponse>
    retrieveEligiblePolicies(
            UUID memberId,
            LocalDate serviceDate
    ) {

        if (memberId == null) {
            throw new RemoteServiceCommunicationException(
                    "Verified appointment does not contain "
                            + "a Member ID"
            );
        }

        if (serviceDate == null) {
            throw new RemoteServiceCommunicationException(
                    "Verified appointment does not contain "
                            + "a service date"
            );
        }

        try {
            List<EligibleClaimPolicyResponse> eligiblePolicies =
                    payersServiceClient
                            .getEligiblePoliciesForClaim(
                                    memberId,
                                    serviceDate
                            );

            if (eligiblePolicies == null) {
                throw new RemoteServiceCommunicationException(
                        "Payers Service returned an empty "
                                + "eligible-policy response"
                );
            }

            return eligiblePolicies;

        } catch (RetryableException exception) {
            throw new RemoteServiceCommunicationException(
                    "Eligible policy lookup is temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {
            handleEligiblePolicyFeignException(
                    exception
            );

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned an unexpected "
                            + "eligible-policy response",
                    exception
            );
        }
    }

    private void handleAppointmentFeignException(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400) {
            throw new ClaimAppointmentVerificationException(
                    "A claim can be submitted only for "
                            + "a COMPLETED appointment"
            );
        }

        if (status == 404) {
            throw new ClaimAppointmentVerificationException(
                    "Completed appointment not found for "
                            + "the authenticated Provider"
            );
        }

        if (status == 401) {
            throw new RemoteServiceCommunicationException(
                    "Appointment Service rejected the forwarded "
                            + "authentication token",
                    exception
            );
        }

        if (status == 403) {
            throw new RemoteServiceCommunicationException(
                    "Appointment Service rejected the forwarded "
                            + "Provider authorization",
                    exception
            );
        }

        if (status == 503) {
            throw new RemoteServiceCommunicationException(
                    "Appointment verification is temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Appointment Service returned an unexpected response",
                exception
        );
    }

    private void handleMedicalRecordFeignException(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400) {
            throw new ClaimMedicalRecordVerificationException(
                    "The selected Medical Record is not valid "
                            + "for claim submission"
            );
        }

        if (status == 404) {
            throw new ClaimMedicalRecordVerificationException(
                    "Medical Record not found for "
                            + "the authenticated Provider"
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
                    "Medical Record verification is "
                            + "temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Provider Service returned an unexpected response",
                exception
        );
    }

    private void handlePolicyFeignException(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400) {
            throw new ClaimPolicyVerificationException(
                    "The selected insurance policy is not valid "
                            + "for the appointment service date"
            );
        }

        if (status == 404) {
            throw new ClaimPolicyVerificationException(
                    "Insurance policy not found for "
                            + "the appointment Member"
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
                            + "Provider authorization",
                    exception
            );
        }

        if (status == 503) {
            throw new RemoteServiceCommunicationException(
                    "Policy verification is temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Payers Service returned an unexpected response",
                exception
        );
    }

    private void handleEligiblePolicyFeignException(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400) {
            throw new ClaimPolicyVerificationException(
                    "Eligible policies could not be loaded "
                            + "for the appointment service date"
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
                            + "Provider authorization",
                    exception
            );
        }

        if (status == 503) {
            throw new RemoteServiceCommunicationException(
                    "Eligible policy lookup is temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Payers Service returned an unexpected "
                        + "eligible-policy response",
                exception
        );
    }

    private void validateRequest(
            ClaimVerificationTestRequest request
    ) {

        if (request == null) {
            throw new ClaimAppointmentVerificationException(
                    "Claim verification request is required"
            );
        }

        if (request.getAppointmentId() == null) {
            throw new ClaimAppointmentVerificationException(
                    "Appointment ID is required"
            );
        }

        if (request.getMedicalRecordId() == null) {
            throw new ClaimMedicalRecordVerificationException(
                    "Medical Record ID is required"
            );
        }

        if (request.getPolicyId() == null) {
            throw new ClaimPolicyVerificationException(
                    "Policy ID is required"
            );
        }
    }
    private void validateSubmissionContextRequest(
            UUID appointmentId,
            UUID medicalRecordId
    ) {

        if (appointmentId == null) {
            throw new ClaimAppointmentVerificationException(
                    "Appointment ID is required"
            );
        }

        if (medicalRecordId == null) {
            throw new ClaimMedicalRecordVerificationException(
                    "Medical Record ID is required"
            );
        }
    }


    private void validateAppointmentResponse(
            ClaimAppointmentVerificationResponse appointment,
            UUID requestedAppointmentId
    ) {

        if (appointment == null) {
            throw new RemoteServiceCommunicationException(
                    "Appointment Service returned an empty response"
            );
        }

        if (appointment.getAppointmentId() == null
                || !requestedAppointmentId.equals(
                appointment.getAppointmentId()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Appointment Service returned details for "
                            + "an unexpected appointment"
            );
        }

        if (!COMPLETED_STATUS.equalsIgnoreCase(
                appointment.getStatus()
        )) {

            throw new ClaimAppointmentVerificationException(
                    "A claim can be submitted only for "
                            + "a COMPLETED appointment"
            );
        }

        requireUuid(
                appointment.getMemberId(),
                "Appointment Member ID"
        );

        requireText(
                appointment.getMemberEmail(),
                "Appointment Member email"
        );

        requireUuid(
                appointment.getProviderId(),
                "Appointment Provider ID"
        );

        requireText(
                appointment.getProviderOrganizationName(),
                "Appointment Provider organization name"
        );

        requireUuid(
                appointment.getDepartmentId(),
                "Appointment Department ID"
        );

        requireText(
                appointment.getDepartmentName(),
                "Appointment Department name"
        );

        requireUuid(
                appointment.getDoctorId(),
                "Appointment Doctor ID"
        );

        requireText(
                appointment.getDoctorName(),
                "Appointment Doctor name"
        );

        requireText(
                appointment.getDoctorSpecialization(),
                "Appointment Doctor specialization"
        );

        requireDate(
                appointment.getAppointmentDate(),
                "Appointment date"
        );

        validateAppointmentTimes(
                appointment.getStartTime(),
                appointment.getEndTime()
        );

        BigDecimal consultationFee =
                appointment.getConsultationFee();

        if (consultationFee == null
                || consultationFee.compareTo(
                BigDecimal.ZERO
        ) < 0) {

            throw new RemoteServiceCommunicationException(
                    "Appointment Service returned an invalid "
                            + "consultation fee"
            );
        }
    }

    private void validateMedicalRecordResponse(
            ClaimMedicalRecordVerificationResponse medicalRecord,
            UUID requestedMedicalRecordId,
            UUID requestedAppointmentId
    ) {

        if (medicalRecord == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an empty "
                            + "Medical Record response"
            );
        }

        if (medicalRecord.getMedicalRecordId() == null
                || !requestedMedicalRecordId.equals(
                medicalRecord.getMedicalRecordId()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned details for "
                            + "an unexpected Medical Record"
            );
        }

        if (medicalRecord.getAppointmentId() == null
                || !requestedAppointmentId.equals(
                medicalRecord.getAppointmentId()
        )) {

            throw new ClaimMedicalRecordVerificationException(
                    "The Medical Record does not belong "
                            + "to the selected appointment"
            );
        }

        String status = medicalRecord.getStatus();

        if (!ACTIVE_STATUS.equalsIgnoreCase(status)
                && !AMENDED_STATUS.equalsIgnoreCase(status)) {

            throw new ClaimMedicalRecordVerificationException(
                    "Only an ACTIVE or AMENDED Medical Record "
                            + "can be used for claim submission"
            );
        }

        requireUuid(
                medicalRecord.getMemberId(),
                "Medical Record Member ID"
        );

        requireText(
                medicalRecord.getMemberEmail(),
                "Medical Record Member email"
        );

        requireUuid(
                medicalRecord.getProviderId(),
                "Medical Record Provider ID"
        );

        requireText(
                medicalRecord.getProviderOrganizationName(),
                "Medical Record Provider organization name"
        );

        requireUuid(
                medicalRecord.getDepartmentId(),
                "Medical Record Department ID"
        );

        requireText(
                medicalRecord.getDepartmentName(),
                "Medical Record Department name"
        );

        requireUuid(
                medicalRecord.getDoctorId(),
                "Medical Record Doctor ID"
        );

        requireText(
                medicalRecord.getDoctorName(),
                "Medical Record Doctor name"
        );

        requireText(
                medicalRecord.getDoctorSpecialization(),
                "Medical Record Doctor specialization"
        );

        requireDate(
                medicalRecord.getAppointmentDate(),
                "Medical Record appointment date"
        );

        requireText(
                medicalRecord.getDiagnosis(),
                "Medical Record diagnosis"
        );

        requireText(
                medicalRecord.getTreatmentPlan(),
                "Medical Record treatment plan"
        );
    }

    private void validatePolicyResponse(
            ClaimPolicyVerificationResponse policy,
            UUID requestedPolicyId,
            UUID expectedMemberId,
            LocalDate expectedServiceDate
    ) {

        if (policy == null) {
            throw new RemoteServiceCommunicationException(
                    "Payers Service returned an empty policy response"
            );
        }

        if (policy.getPolicyId() == null
                || !requestedPolicyId.equals(
                policy.getPolicyId()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned details for "
                            + "an unexpected policy"
            );
        }

        if (policy.getMemberId() == null
                || !expectedMemberId.equals(
                policy.getMemberId()
        )) {

            throw new ClaimPolicyVerificationException(
                    "The insurance policy does not belong "
                            + "to the appointment Member"
            );
        }

        if (policy.getServiceDate() == null
                || !expectedServiceDate.equals(
                policy.getServiceDate()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service verified an unexpected service date"
            );
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                policy.getStatus()
        )) {

            throw new ClaimPolicyVerificationException(
                    "Only an ACTIVE insurance policy can be "
                            + "used for claim submission"
            );
        }

        if (!Boolean.TRUE.equals(
                policy.getCoverageValid()
        )) {

            throw new ClaimPolicyVerificationException(
                    "The insurance policy does not cover "
                            + "the appointment service date"
            );
        }

        requireText(
                policy.getPolicyNumber(),
                "Policy number"
        );

        requireUuid(
                policy.getPayerId(),
                "Policy Payer ID"
        );

        requireText(
                policy.getPayerOrganizationName(),
                "Policy Payer organization name"
        );

        requireUuid(
                policy.getPlanId(),
                "Policy plan ID"
        );

        requireText(
                policy.getPlanCode(),
                "Policy plan code"
        );

        requireText(
                policy.getPlanName(),
                "Policy plan name"
        );

        requireDate(
                policy.getCoverageStartDate(),
                "Policy coverage start date"
        );

        requireDate(
                policy.getCoverageEndDate(),
                "Policy coverage end date"
        );

        if (policy.getCoverageStartDate().isAfter(
                expectedServiceDate
        )) {

            throw new ClaimPolicyVerificationException(
                    "The appointment occurred before "
                            + "the policy coverage start date"
            );
        }

        if (policy.getCoverageEndDate().isBefore(
                expectedServiceDate
        )) {

            throw new ClaimPolicyVerificationException(
                    "The appointment occurred after "
                            + "the policy coverage end date"
            );
        }

        if (policy.getCoverageAmount() == null
                || policy.getCoverageAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new RemoteServiceCommunicationException(
                    "Payers Service returned an invalid coverage amount"
            );
        }
    }

    private void crossCheckAppointmentAndMedicalRecord(
            ClaimAppointmentVerificationResponse appointment,
            ClaimMedicalRecordVerificationResponse medicalRecord
    ) {

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

        compareEmail(
                appointment.getMemberEmail(),
                medicalRecord.getMemberEmail(),
                "Appointment and Medical Record Member emails do not match"
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
    }

    private void crossCheckPolicyInformation(
            ClaimAppointmentVerificationResponse appointment,
            ClaimMedicalRecordVerificationResponse medicalRecord,
            ClaimPolicyVerificationResponse policy
    ) {

        compareUuid(
                appointment.getMemberId(),
                policy.getMemberId(),
                "Appointment and Policy Member IDs do not match"
        );

        compareEmail(
                appointment.getMemberEmail(),
                policy.getMemberEmail(),
                "Appointment and Policy Member emails do not match"
        );

        compareUuid(
                medicalRecord.getMemberId(),
                policy.getMemberId(),
                "Medical Record and Policy Member IDs do not match"
        );

        compareEmail(
                medicalRecord.getMemberEmail(),
                policy.getMemberEmail(),
                "Medical Record and Policy Member emails do not match"
        );

        compareDate(
                appointment.getAppointmentDate(),
                policy.getServiceDate(),
                "Appointment date and Policy service date do not match"
        );

        compareDate(
                medicalRecord.getAppointmentDate(),
                policy.getServiceDate(),
                "Medical Record date and Policy service date do not match"
        );
    }

    private void validateAppointmentTimes(
            LocalTime startTime,
            LocalTime endTime
    ) {

        if (startTime == null
                || endTime == null
                || !startTime.isBefore(endTime)) {

            throw new RemoteServiceCommunicationException(
                    "Appointment Service returned an invalid "
                            + "appointment time range"
            );
        }
    }

    private void requireUuid(
            UUID value,
            String fieldName
    ) {

        if (value == null) {
            throw new RemoteServiceCommunicationException(
                    fieldName + " is missing"
            );
        }
    }

    private void requireText(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {
            throw new RemoteServiceCommunicationException(
                    fieldName + " is missing"
            );
        }
    }

    private void requireDate(
            LocalDate value,
            String fieldName
    ) {

        if (value == null) {
            throw new RemoteServiceCommunicationException(
                    fieldName + " is missing"
            );
        }
    }

    private void compareUuid(
            UUID first,
            UUID second,
            String message
    ) {

        if (first == null
                || second == null
                || !first.equals(second)) {

            throw new ClaimMedicalRecordVerificationException(
                    message
            );
        }
    }

    private void compareEmail(
            String first,
            String second,
            String message
    ) {

        if (first == null
                || second == null
                || !normalizeEmail(first).equals(
                normalizeEmail(second)
        )) {

            throw new ClaimMedicalRecordVerificationException(
                    message
            );
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

            throw new ClaimMedicalRecordVerificationException(
                    message
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}