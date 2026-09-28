package com.naveen.healthcare.payersservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.payersservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.entity.InsurancePolicy;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.NotificationType;
import com.naveen.healthcare.payersservice.enums.RecipientRole;
import com.naveen.healthcare.payersservice.enums.ReferenceType;
import com.naveen.healthcare.payersservice.service.InsuranceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceEventPublisherImpl
        implements InsuranceEventPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    InsuranceEventPublisherImpl.class
            );

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${insurance.kafka.topics.insurance-events}")
    private String insuranceEventsTopic;

    @Override
    public void publishEnrollmentSubmitted(
            InsuranceEnrollment enrollment
    ) {

        publishEnrollmentEvent(
                enrollment,
                NotificationType.ENROLLMENT_SUBMITTED,
                "Enrollment Submitted",
                "Your enrollment request for "
                        + getEnrollmentPlanNameSafely(enrollment)
                        + " has been submitted successfully."
        );
    }

    @Override
    public void publishEnrollmentApproved(
            InsuranceEnrollment enrollment
    ) {

        publishEnrollmentEvent(
                enrollment,
                NotificationType.ENROLLMENT_APPROVED,
                "Enrollment Approved",
                "Your enrollment request for "
                        + getEnrollmentPlanNameSafely(enrollment)
                        + " has been approved."
        );
    }

    @Override
    public void publishEnrollmentRejected(
            InsuranceEnrollment enrollment
    ) {

        publishEnrollmentEvent(
                enrollment,
                NotificationType.ENROLLMENT_REJECTED,
                "Enrollment Rejected",
                "Your enrollment request for "
                        + getEnrollmentPlanNameSafely(enrollment)
                        + " has been rejected."
        );
    }

    @Override
    public void publishPolicyActivated(
            InsurancePolicy policy
    ) {

        publishPolicyEvent(
                policy,
                NotificationType.POLICY_ACTIVATED,
                "Policy Activated",
                "Your insurance policy "
                        + getPolicyNumberSafely(policy)
                        + " is now active."
        );
    }

    @Override
    public void publishPolicySuspended(
            InsurancePolicy policy
    ) {

        publishPolicyEvent(
                policy,
                NotificationType.POLICY_SUSPENDED,
                "Policy Suspended",
                "Your insurance policy "
                        + getPolicyNumberSafely(policy)
                        + " has been suspended."
        );
    }

    @Override
    public void publishPolicyExpired(
            InsurancePolicy policy
    ) {

        publishPolicyEvent(
                policy,
                NotificationType.POLICY_EXPIRED,
                "Policy Expired",
                "Your insurance policy "
                        + getPolicyNumberSafely(policy)
                        + " has expired."
        );
    }

    private void publishEnrollmentEvent(
            InsuranceEnrollment enrollment,
            NotificationType eventType,
            String title,
            String message
    ) {

        try {

            validateEnrollmentForPublishing(
                    enrollment
            );

            HealthcareEvent event =
                    buildEnrollmentEvent(
                            enrollment,
                            eventType,
                            title,
                            message
                    );

            String messageKey =
                    enrollment.getId().toString();

            publishEvent(
                    messageKey,
                    enrollment.getId(),
                    eventType,
                    event
            );

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to publish {} event to Kafka. "
                            + "Enrollment ID: {}. "
                            + "The enrollment operation remains successful.",
                    eventType,
                    enrollment == null
                            ? null
                            : enrollment.getId(),
                    exception
            );
        }
    }

    private void publishPolicyEvent(
            InsurancePolicy policy,
            NotificationType eventType,
            String title,
            String message
    ) {

        try {

            validatePolicyForPublishing(
                    policy
            );

            HealthcareEvent event =
                    buildPolicyEvent(
                            policy,
                            eventType,
                            title,
                            message
                    );

            String messageKey =
                    policy.getId().toString();

            publishEvent(
                    messageKey,
                    policy.getId(),
                    eventType,
                    event
            );

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to publish {} event to Kafka. "
                            + "Policy ID: {}. "
                            + "The policy operation remains successful.",
                    eventType,
                    policy == null
                            ? null
                            : policy.getId(),
                    exception
            );
        }
    }

    private void publishEvent(
            String messageKey,
            UUID referenceId,
            NotificationType eventType,
            HealthcareEvent event
    ) {

        String eventJson =
                convertEventToJson(
                        event
                );

        kafkaTemplate
                .send(
                        insuranceEventsTopic,
                        messageKey,
                        eventJson
                )
                .whenComplete(
                        (sendResult, exception) -> {

                            if (exception != null) {

                                LOGGER.error(
                                        "Failed to publish {} event. "
                                                + "Reference ID: {}, "
                                                + "event ID: {}",
                                        eventType,
                                        referenceId,
                                        event.getEventId(),
                                        exception
                                );

                                return;
                            }

                            LOGGER.info(
                                    "Published {} event. "
                                            + "Reference ID: {}, "
                                            + "event ID: {}, "
                                            + "topic: {}, "
                                            + "partition: {}, "
                                            + "offset: {}",
                                    eventType,
                                    referenceId,
                                    event.getEventId(),
                                    sendResult
                                            .getRecordMetadata()
                                            .topic(),
                                    sendResult
                                            .getRecordMetadata()
                                            .partition(),
                                    sendResult
                                            .getRecordMetadata()
                                            .offset()
                            );
                        }
                );
    }

    private HealthcareEvent buildEnrollmentEvent(
            InsuranceEnrollment enrollment,
            NotificationType eventType,
            String title,
            String message
    ) {

        InsurancePlan insurancePlan =
                enrollment.getInsurancePlan();

        PayerOrganization payerOrganization =
                enrollment.getPayerOrganization();

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "enrollmentId",
                enrollment.getId().toString()
        );

        metadata.put(
                "memberId",
                enrollment.getMemberId()
        );

        metadata.put(
                "planId",
                insurancePlan.getId()
        );

        metadata.put(
                "planCode",
                insurancePlan.getPlanCode()
        );

        metadata.put(
                "planName",
                insurancePlan.getPlanName()
        );

        metadata.put(
                "coverageAmount",
                insurancePlan.getCoverageAmount()
        );

        metadata.put(
                "monthlyPremium",
                insurancePlan.getMonthlyPremium()
        );

        metadata.put(
                "payerId",
                payerOrganization.getId()
        );

        metadata.put(
                "payerOrganizationName",
                payerOrganization.getOrganizationName()
        );

        metadata.put(
                "requestedCoverageStartDate",
                enrollment.getRequestedCoverageStartDate()
        );

        metadata.put(
                "status",
                enrollment.getStatus().name()
        );

        metadata.put(
                "reviewRemarks",
                enrollment.getReviewRemarks()
        );

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        eventType
                )
                .occurredAt(
                        resolveEnrollmentOccurredAt(
                                enrollment,
                                eventType
                        )
                )
                .recipientEmail(
                        normalizeEmail(
                                enrollment.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.ENROLLMENT
                )
                .referenceId(
                        enrollment.getId()
                )
                .title(
                        title
                )
                .message(
                        message
                )
                .metadata(
                        metadata
                )
                .build();
    }

    private HealthcareEvent buildPolicyEvent(
            InsurancePolicy policy,
            NotificationType eventType,
            String title,
            String message
    ) {

        InsurancePlan insurancePlan =
                policy.getInsurancePlan();

        PayerOrganization payerOrganization =
                policy.getPayerOrganization();

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "policyId",
                policy.getId().toString()
        );

        metadata.put(
                "policyNumber",
                policy.getPolicyNumber()
        );

        if (policy.getEnrollment() != null
                && policy.getEnrollment().getId() != null) {

            metadata.put(
                    "enrollmentId",
                    policy.getEnrollment()
                            .getId()
                            .toString()
            );
        }

        metadata.put(
                "memberId",
                policy.getMemberId()
        );

        metadata.put(
                "planId",
                insurancePlan.getId()
        );

        metadata.put(
                "planCode",
                policy.getPlanCode()
        );

        metadata.put(
                "planName",
                policy.getPlanName()
        );

        metadata.put(
                "coverageAmount",
                policy.getCoverageAmount()
        );

        metadata.put(
                "monthlyPremium",
                policy.getMonthlyPremium()
        );

        metadata.put(
                "payerId",
                payerOrganization.getId()
        );

        metadata.put(
                "payerOrganizationName",
                payerOrganization.getOrganizationName()
        );

        metadata.put(
                "coverageStartDate",
                policy.getCoverageStartDate()
        );

        metadata.put(
                "coverageEndDate",
                policy.getCoverageEndDate()
        );

        metadata.put(
                "status",
                policy.getStatus().name()
        );

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        eventType
                )
                .occurredAt(
                        resolvePolicyOccurredAt(policy)
                )
                .recipientEmail(
                        normalizeEmail(
                                policy.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.POLICY
                )
                .referenceId(
                        policy.getId()
                )
                .title(
                        title
                )
                .message(
                        message
                )
                .metadata(
                        metadata
                )
                .build();
    }

    private LocalDateTime resolveEnrollmentOccurredAt(
            InsuranceEnrollment enrollment,
            NotificationType eventType
    ) {

        if (eventType
                == NotificationType.ENROLLMENT_SUBMITTED
                && enrollment.getCreatedAt() != null) {

            return enrollment.getCreatedAt();
        }

        if ((eventType
                == NotificationType.ENROLLMENT_APPROVED
                || eventType
                == NotificationType.ENROLLMENT_REJECTED)
                && enrollment.getReviewedAt() != null) {

            return enrollment.getReviewedAt();
        }

        if (enrollment.getUpdatedAt() != null) {
            return enrollment.getUpdatedAt();
        }

        return LocalDateTime.now();
    }

    private LocalDateTime resolvePolicyOccurredAt(
            InsurancePolicy policy
    ) {

        if (policy.getUpdatedAt() != null) {
            return policy.getUpdatedAt();
        }

        if (policy.getCreatedAt() != null) {
            return policy.getCreatedAt();
        }

        return LocalDateTime.now();
    }

    private String convertEventToJson(
            HealthcareEvent event
    ) {

        try {

            return objectMapper.writeValueAsString(
                    event
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to convert Insurance event to JSON",
                    exception
            );
        }
    }

    private void validateEnrollmentForPublishing(
            InsuranceEnrollment enrollment
    ) {

        if (enrollment == null) {

            throw new IllegalArgumentException(
                    "Insurance enrollment is required"
            );
        }

        if (enrollment.getId() == null) {

            throw new IllegalArgumentException(
                    "Enrollment ID is required"
            );
        }

        if (enrollment.getMemberEmail() == null
                || enrollment.getMemberEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Enrollment Member email is required"
            );
        }

        if (enrollment.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Enrollment status is required"
            );
        }

        if (enrollment.getInsurancePlan() == null) {

            throw new IllegalArgumentException(
                    "Enrollment insurance plan is required"
            );
        }

        if (enrollment.getInsurancePlan().getId() == null) {

            throw new IllegalArgumentException(
                    "Enrollment insurance plan ID is required"
            );
        }

        if (enrollment.getPayerOrganization() == null) {

            throw new IllegalArgumentException(
                    "Enrollment Payer organization is required"
            );
        }

        if (enrollment.getPayerOrganization().getId() == null) {

            throw new IllegalArgumentException(
                    "Enrollment Payer organization ID is required"
            );
        }
    }

    private void validatePolicyForPublishing(
            InsurancePolicy policy
    ) {

        if (policy == null) {

            throw new IllegalArgumentException(
                    "Insurance policy is required"
            );
        }

        if (policy.getId() == null) {

            throw new IllegalArgumentException(
                    "Policy ID is required"
            );
        }

        if (policy.getPolicyNumber() == null
                || policy.getPolicyNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Policy number is required"
            );
        }

        if (policy.getMemberEmail() == null
                || policy.getMemberEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Policy Member email is required"
            );
        }

        if (policy.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Policy status is required"
            );
        }

        if (policy.getInsurancePlan() == null) {

            throw new IllegalArgumentException(
                    "Policy insurance plan is required"
            );
        }

        if (policy.getInsurancePlan().getId() == null) {

            throw new IllegalArgumentException(
                    "Policy insurance plan ID is required"
            );
        }

        if (policy.getPayerOrganization() == null) {

            throw new IllegalArgumentException(
                    "Policy Payer organization is required"
            );
        }

        if (policy.getPayerOrganization().getId() == null) {

            throw new IllegalArgumentException(
                    "Policy Payer organization ID is required"
            );
        }
    }

    private String getEnrollmentPlanNameSafely(
            InsuranceEnrollment enrollment
    ) {

        if (enrollment == null
                || enrollment.getInsurancePlan() == null
                || enrollment.getInsurancePlan()
                .getPlanName() == null
                || enrollment.getInsurancePlan()
                .getPlanName()
                .isBlank()) {

            return "the selected insurance plan";
        }

        return enrollment
                .getInsurancePlan()
                .getPlanName()
                .trim();
    }

    private String getPolicyNumberSafely(
            InsurancePolicy policy
    ) {

        if (policy == null
                || policy.getPolicyNumber() == null
                || policy.getPolicyNumber().isBlank()) {

            return "";
        }

        return policy
                .getPolicyNumber()
                .trim();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Member email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    @Override
    public void publishEnrollmentCancelled(
            InsuranceEnrollment enrollment
    ) {

        publishEnrollmentEvent(
                enrollment,
                NotificationType.ENROLLMENT_CANCELLED,
                "Enrollment Cancelled",
                "Your enrollment request for "
                        + getEnrollmentPlanNameSafely(enrollment)
                        + " has been cancelled."
        );
    }
    @Override
    public void publishPolicyCancelled(
            InsurancePolicy policy
    ) {

        publishPolicyEvent(
                policy,
                NotificationType.POLICY_CANCELLED,
                "Policy Cancelled",
                "Your insurance policy "
                        + getPolicyNumberSafely(policy)
                        + " has been cancelled."
        );
    }
}