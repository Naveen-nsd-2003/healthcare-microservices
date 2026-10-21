
package com.naveen.healthcare.payersservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.payersservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.entity.InsurancePolicy;
import com.naveen.healthcare.payersservice.entity.OutboxEvent;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.NotificationType;
import com.naveen.healthcare.payersservice.enums.OutboxEventStatus;
import com.naveen.healthcare.payersservice.enums.RecipientRole;
import com.naveen.healthcare.payersservice.enums.ReferenceType;
import com.naveen.healthcare.payersservice.repository.OutboxEventRepository;
import com.naveen.healthcare.payersservice.service.InsuranceEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceEventPublisherImpl
        implements InsuranceEventPublisher {

    private static final String ENROLLMENT_AGGREGATE =
            "ENROLLMENT";

    private static final String POLICY_AGGREGATE =
            "POLICY";

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository
            outboxEventRepository;

    @Value(
            "${insurance.kafka.topics.insurance-events}"
    )
    private String insuranceEventsTopic;

    @Override
    public void publishEnrollmentSubmitted(
            InsuranceEnrollment enrollment
    ) {

        validateEnrollment(
                enrollment
        );

        PayerOrganization payer =
                enrollment.getPayerOrganization();

        LocalDateTime occurredAt =
                resolveEnrollmentOccurredAt(
                        enrollment,
                        NotificationType.ENROLLMENT_SUBMITTED
                );

        HealthcareEvent memberEvent =
                buildEnrollmentEvent(
                        enrollment,
                        NotificationType.ENROLLMENT_SUBMITTED,
                        RecipientRole.MEMBER,
                        enrollment.getMemberEmail(),
                        "Enrollment Submitted",
                        "Your enrollment request for "
                                + getPlanName(enrollment)
                                + " has been submitted successfully.",
                        occurredAt
                );

        HealthcareEvent payerEvent =
                buildEnrollmentEvent(
                        enrollment,
                        NotificationType.ENROLLMENT_SUBMITTED,
                        RecipientRole.PAYER,
                        payer.getAdminEmail(),
                        "New Enrollment Request",
                        "A new enrollment request for "
                                + getPlanName(enrollment)
                                + " was submitted by "
                                + enrollment.getMemberEmail()
                                + " and requires review.",
                        occurredAt
                );

        outboxEventRepository.saveAllAndFlush(
                List.of(
                        buildEnrollmentOutbox(
                                enrollment,
                                memberEvent
                        ),
                        buildEnrollmentOutbox(
                                enrollment,
                                payerEvent
                        )
                )
        );
    }

    @Override
    public void publishEnrollmentApproved(
            InsuranceEnrollment enrollment
    ) {

        saveEnrollmentEvent(
                enrollment,
                NotificationType.ENROLLMENT_APPROVED,
                RecipientRole.MEMBER,
                enrollment.getMemberEmail(),
                "Enrollment Approved",
                "Your enrollment request for "
                        + getPlanName(enrollment)
                        + " has been approved."
        );
    }

    @Override
    public void publishEnrollmentRejected(
            InsuranceEnrollment enrollment
    ) {

        String message =
                "Your enrollment request for "
                        + getPlanName(enrollment)
                        + " has been rejected.";

        if (enrollment.getReviewRemarks() != null
                && !enrollment.getReviewRemarks().isBlank()) {

            message += " Remarks: "
                    + enrollment.getReviewRemarks().trim();
        }

        saveEnrollmentEvent(
                enrollment,
                NotificationType.ENROLLMENT_REJECTED,
                RecipientRole.MEMBER,
                enrollment.getMemberEmail(),
                "Enrollment Rejected",
                message
        );
    }

    @Override
    public void publishEnrollmentCancelled(
            InsuranceEnrollment enrollment
    ) {

        validateEnrollment(
                enrollment
        );

        PayerOrganization payer =
                enrollment.getPayerOrganization();

        LocalDateTime occurredAt =
                resolveEnrollmentOccurredAt(
                        enrollment,
                        NotificationType.ENROLLMENT_CANCELLED
                );

        HealthcareEvent memberEvent =
                buildEnrollmentEvent(
                        enrollment,
                        NotificationType.ENROLLMENT_CANCELLED,
                        RecipientRole.MEMBER,
                        enrollment.getMemberEmail(),
                        "Enrollment Cancelled",
                        "Your enrollment request for "
                                + getPlanName(enrollment)
                                + " has been cancelled.",
                        occurredAt
                );

        HealthcareEvent payerEvent =
                buildEnrollmentEvent(
                        enrollment,
                        NotificationType.ENROLLMENT_CANCELLED,
                        RecipientRole.PAYER,
                        payer.getAdminEmail(),
                        "Enrollment Cancelled by Member",
                        "The enrollment request for "
                                + getPlanName(enrollment)
                                + " submitted by "
                                + enrollment.getMemberEmail()
                                + " was cancelled by the Member.",
                        occurredAt
                );

        outboxEventRepository.saveAllAndFlush(
                List.of(
                        buildEnrollmentOutbox(
                                enrollment,
                                memberEvent
                        ),
                        buildEnrollmentOutbox(
                                enrollment,
                                payerEvent
                        )
                )
        );
    }

    @Override
    public void publishPolicyActivated(
            InsurancePolicy policy
    ) {

        savePolicyEvent(
                policy,
                NotificationType.POLICY_ACTIVATED,
                "Policy Activated",
                "Your insurance policy "
                        + getPolicyNumber(policy)
                        + " is now active."
        );
    }

    @Override
    public void publishPolicySuspended(
            InsurancePolicy policy
    ) {

        savePolicyEvent(
                policy,
                NotificationType.POLICY_SUSPENDED,
                "Policy Suspended",
                "Your insurance policy "
                        + getPolicyNumber(policy)
                        + " has been suspended."
        );
    }

    @Override
    public void publishPolicyCancelled(
            InsurancePolicy policy
    ) {

        savePolicyEvent(
                policy,
                NotificationType.POLICY_CANCELLED,
                "Policy Cancelled",
                "Your insurance policy "
                        + getPolicyNumber(policy)
                        + " has been cancelled."
        );
    }

    @Override
    public void publishPolicyExpired(
            InsurancePolicy policy
    ) {

        savePolicyEvent(
                policy,
                NotificationType.POLICY_EXPIRED,
                "Policy Expired",
                "Your insurance policy "
                        + getPolicyNumber(policy)
                        + " has expired."
        );
    }

    private void saveEnrollmentEvent(
            InsuranceEnrollment enrollment,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message
    ) {

        validateEnrollment(
                enrollment
        );

        HealthcareEvent event =
                buildEnrollmentEvent(
                        enrollment,
                        notificationType,
                        recipientRole,
                        recipientEmail,
                        title,
                        message,
                        resolveEnrollmentOccurredAt(
                                enrollment,
                                notificationType
                        )
                );

        outboxEventRepository.saveAndFlush(
                buildEnrollmentOutbox(
                        enrollment,
                        event
                )
        );
    }

    private void savePolicyEvent(
            InsurancePolicy policy,
            NotificationType notificationType,
            String title,
            String message
    ) {

        validatePolicy(
                policy
        );

        HealthcareEvent event =
                buildPolicyEvent(
                        policy,
                        notificationType,
                        title,
                        message
                );

        outboxEventRepository.saveAndFlush(
                buildPolicyOutbox(
                        policy,
                        event
                )
        );
    }

    private HealthcareEvent buildEnrollmentEvent(
            InsuranceEnrollment enrollment,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        notificationType
                )
                .occurredAt(
                        occurredAt
                )
                .recipientEmail(
                        normalizeEmail(
                                recipientEmail
                        )
                )
                .recipientRole(
                        recipientRole
                )
                .referenceType(
                        ReferenceType.ENROLLMENT
                )
                .referenceId(
                        enrollment.getId()
                )
                .title(
                        normalizeText(
                                title,
                                "Notification title"
                        )
                )
                .message(
                        normalizeText(
                                message,
                                "Notification message"
                        )
                )
                .metadata(
                        buildEnrollmentMetadata(
                                enrollment
                        )
                )
                .build();
    }

    private HealthcareEvent buildPolicyEvent(
            InsurancePolicy policy,
            NotificationType notificationType,
            String title,
            String message
    ) {

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        notificationType
                )
                .occurredAt(
                        resolvePolicyOccurredAt(
                                policy
                        )
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
                        normalizeText(
                                title,
                                "Notification title"
                        )
                )
                .message(
                        normalizeText(
                                message,
                                "Notification message"
                        )
                )
                .metadata(
                        buildPolicyMetadata(
                                policy
                        )
                )
                .build();
    }

    private OutboxEvent buildEnrollmentOutbox(
            InsuranceEnrollment enrollment,
            HealthcareEvent event
    ) {

        return buildOutboxEvent(
                event,
                ENROLLMENT_AGGREGATE,
                enrollment.getId()
        );
    }

    private OutboxEvent buildPolicyOutbox(
            InsurancePolicy policy,
            HealthcareEvent event
    ) {

        return buildOutboxEvent(
                event,
                POLICY_AGGREGATE,
                policy.getId()
        );
    }

    private OutboxEvent buildOutboxEvent(
            HealthcareEvent event,
            String aggregateType,
            UUID aggregateId
    ) {

        validateEvent(
                event
        );

        LocalDateTime now =
                LocalDateTime.now();

        return OutboxEvent.builder()
                .id(
                        UUID.randomUUID()
                )
                .eventId(
                        event.getEventId()
                )
                .aggregateType(
                        aggregateType
                )
                .aggregateId(
                        aggregateId
                )
                .eventType(
                        event.getEventType().name()
                )
                .topicName(
                        normalizeText(
                                insuranceEventsTopic,
                                "Insurance Kafka topic"
                        )
                )
                .messageKey(
                        aggregateId.toString()
                )
                .payload(
                        serializeEvent(event)
                )
                .status(
                        OutboxEventStatus.PENDING
                )
                .retryCount(0)
                .nextAttemptAt(now)
                .processingStartedAt(null)
                .lastAttemptAt(null)
                .publishedAt(null)
                .lastError(null)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private Map<String, Object> buildEnrollmentMetadata(
            InsuranceEnrollment enrollment
    ) {

        InsurancePlan plan =
                enrollment.getInsurancePlan();

        PayerOrganization payer =
                enrollment.getPayerOrganization();

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "enrollmentId",
                enrollment.getId().toString()
        );

        metadata.put(
                "memberId",
                enrollment.getMemberId().toString()
        );

        metadata.put(
                "planId",
                plan.getId().toString()
        );

        metadata.put(
                "planCode",
                plan.getPlanCode()
        );

        metadata.put(
                "planName",
                plan.getPlanName()
        );

        metadata.put(
                "coverageAmount",
                plan.getCoverageAmount()
        );

        metadata.put(
                "monthlyPremium",
                plan.getMonthlyPremium()
        );

        metadata.put(
                "payerId",
                payer.getId().toString()
        );

        metadata.put(
                "payerOrganizationName",
                payer.getOrganizationName()
        );

        metadata.put(
                "requestedCoverageStartDate",
                enrollment
                        .getRequestedCoverageStartDate()
                        .toString()
        );

        metadata.put(
                "status",
                enrollment.getStatus().name()
        );

        if (enrollment.getReviewRemarks() != null) {

            metadata.put(
                    "reviewRemarks",
                    enrollment.getReviewRemarks()
            );
        }

        return metadata;
    }

    private Map<String, Object> buildPolicyMetadata(
            InsurancePolicy policy
    ) {

        InsurancePlan plan =
                policy.getInsurancePlan();

        PayerOrganization payer =
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
                policy.getMemberId().toString()
        );

        metadata.put(
                "planId",
                plan.getId().toString()
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
                payer.getId().toString()
        );

        metadata.put(
                "payerOrganizationName",
                payer.getOrganizationName()
        );

        metadata.put(
                "coverageStartDate",
                policy.getCoverageStartDate().toString()
        );

        metadata.put(
                "coverageEndDate",
                policy.getCoverageEndDate().toString()
        );

        metadata.put(
                "status",
                policy.getStatus().name()
        );

        return metadata;
    }

    private LocalDateTime resolveEnrollmentOccurredAt(
            InsuranceEnrollment enrollment,
            NotificationType notificationType
    ) {

        if (notificationType
                == NotificationType.ENROLLMENT_SUBMITTED
                && enrollment.getCreatedAt() != null) {

            return enrollment.getCreatedAt();
        }

        if ((notificationType
                == NotificationType.ENROLLMENT_APPROVED
                || notificationType
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

    private String serializeEvent(
            HealthcareEvent event
    ) {

        try {

            return objectMapper.writeValueAsString(
                    event
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to serialize insurance event",
                    exception
            );
        }
    }

    private void validateEvent(
            HealthcareEvent event
    ) {

        if (event == null
                || event.getEventId() == null
                || event.getEventType() == null
                || event.getRecipientRole() == null
                || event.getReferenceType() == null
                || event.getReferenceId() == null) {

            throw new IllegalArgumentException(
                    "Complete healthcare event information "
                            + "is required"
            );
        }

        normalizeEmail(
                event.getRecipientEmail()
        );

        normalizeText(
                event.getTitle(),
                "Healthcare event title"
        );

        normalizeText(
                event.getMessage(),
                "Healthcare event message"
        );
    }

    private void validateEnrollment(
            InsuranceEnrollment enrollment
    ) {

        if (enrollment == null
                || enrollment.getId() == null
                || enrollment.getMemberId() == null
                || enrollment.getStatus() == null
                || enrollment.getInsurancePlan() == null
                || enrollment.getInsurancePlan().getId() == null
                || enrollment.getPayerOrganization() == null
                || enrollment.getPayerOrganization().getId() == null) {

            throw new IllegalArgumentException(
                    "Complete enrollment information "
                            + "is required for notification"
            );
        }

        normalizeEmail(
                enrollment.getMemberEmail()
        );

        normalizeEmail(
                enrollment.getPayerOrganization()
                        .getAdminEmail()
        );
    }

    private void validatePolicy(
            InsurancePolicy policy
    ) {

        if (policy == null
                || policy.getId() == null
                || policy.getMemberId() == null
                || policy.getStatus() == null
                || policy.getInsurancePlan() == null
                || policy.getInsurancePlan().getId() == null
                || policy.getPayerOrganization() == null
                || policy.getPayerOrganization().getId() == null) {

            throw new IllegalArgumentException(
                    "Complete policy information "
                            + "is required for notification"
            );
        }

        normalizeEmail(
                policy.getMemberEmail()
        );

        normalizeText(
                policy.getPolicyNumber(),
                "Policy number"
        );
    }

    private String getPlanName(
            InsuranceEnrollment enrollment
    ) {

        return normalizeText(
                enrollment.getInsurancePlan()
                        .getPlanName(),
                "Insurance plan name"
        );
    }

    private String getPolicyNumber(
            InsurancePolicy policy
    ) {

        return normalizeText(
                policy.getPolicyNumber(),
                "Policy number"
        );
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Notification recipient email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeText(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }
}
