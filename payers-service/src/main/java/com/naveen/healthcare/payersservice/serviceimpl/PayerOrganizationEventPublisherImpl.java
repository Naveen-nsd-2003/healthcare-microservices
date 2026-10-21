package com.naveen.healthcare.payersservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.payersservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.payersservice.entity.OutboxEvent;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.NotificationType;
import com.naveen.healthcare.payersservice.enums.OutboxEventStatus;
import com.naveen.healthcare.payersservice.enums.RecipientRole;
import com.naveen.healthcare.payersservice.enums.ReferenceType;
import com.naveen.healthcare.payersservice.repository.OutboxEventRepository;
import com.naveen.healthcare.payersservice.service.PayerOrganizationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayerOrganizationEventPublisherImpl
        implements PayerOrganizationEventPublisher {

    private static final String AGGREGATE_TYPE =
            "PAYER_ORGANIZATION";

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository
            outboxEventRepository;

    @Value(
            "${insurance.kafka.topics.organization-events}"
    )
    private String organizationEventsTopic;

    @Value(
            "${platform.admin.notification-email}"
    )
    private String platformAdminNotificationEmail;

    @Override
    public void publishOrganizationSubmitted(
            PayerOrganization payerOrganization
    ) {

        validateOrganization(
                payerOrganization
        );

        String title =
                "New Payer Organization Application";

        String message =
                payerOrganization.getOrganizationName()
                        + " submitted a Payer organization "
                        + "application and requires review.";

        saveOrganizationEvent(
                payerOrganization,
                NotificationType.PAYER_ORGANIZATION_SUBMITTED,
                RecipientRole.ADMIN,
                platformAdminNotificationEmail,
                title,
                message,
                resolveOccurredAt(
                        payerOrganization,
                        NotificationType.PAYER_ORGANIZATION_SUBMITTED
                )
        );
    }

    @Override
    public void publishOrganizationApproved(
            PayerOrganization payerOrganization
    ) {

        validateOrganization(
                payerOrganization
        );

        String title =
                "Payer Organization Approved";

        String message =
                "Your Payer organization "
                        + payerOrganization.getOrganizationName()
                        + " has been approved. "
                        + "You can now access Payer operations.";

        String reviewRemarks =
                normalizeOptionalText(
                        payerOrganization.getReviewRemarks()
                );

        if (reviewRemarks != null) {

            message += " Review remarks: "
                    + reviewRemarks;
        }

        saveOrganizationEvent(
                payerOrganization,
                NotificationType.PAYER_ORGANIZATION_APPROVED,
                RecipientRole.PAYER,
                payerOrganization.getAdminEmail(),
                title,
                message,
                resolveOccurredAt(
                        payerOrganization,
                        NotificationType.PAYER_ORGANIZATION_APPROVED
                )
        );
    }

    @Override
    public void publishOrganizationRejected(
            PayerOrganization payerOrganization
    ) {

        validateOrganization(
                payerOrganization
        );

        String rejectionReason =
                normalizeRequiredText(
                        payerOrganization.getRejectionReason(),
                        "Payer organization rejection reason"
                );

        String title =
                "Payer Organization Application Rejected";

        String message =
                "Your Payer organization application for "
                        + payerOrganization.getOrganizationName()
                        + " has been rejected. Reason: "
                        + rejectionReason;

        String reviewRemarks =
                normalizeOptionalText(
                        payerOrganization.getReviewRemarks()
                );

        if (reviewRemarks != null) {

            message += " Review remarks: "
                    + reviewRemarks;
        }

        saveOrganizationEvent(
                payerOrganization,
                NotificationType.PAYER_ORGANIZATION_REJECTED,
                RecipientRole.PAYER,
                payerOrganization.getAdminEmail(),
                title,
                message,
                resolveOccurredAt(
                        payerOrganization,
                        NotificationType.PAYER_ORGANIZATION_REJECTED
                )
        );
    }

    @Override
    public void publishOrganizationSuspended(
            PayerOrganization payerOrganization
    ) {

        validateOrganization(
                payerOrganization
        );

        saveOrganizationEvent(
                payerOrganization,
                NotificationType.PAYER_ORGANIZATION_SUSPENDED,
                RecipientRole.PAYER,
                payerOrganization.getAdminEmail(),
                "Payer Organization Suspended",
                "Your Payer organization "
                        + payerOrganization.getOrganizationName()
                        + " has been suspended. "
                        + "Business operations are temporarily unavailable.",
                resolveOccurredAt(
                        payerOrganization,
                        NotificationType.PAYER_ORGANIZATION_SUSPENDED
                )
        );
    }

    @Override
    public void publishOrganizationReactivated(
            PayerOrganization payerOrganization
    ) {

        validateOrganization(
                payerOrganization
        );

        saveOrganizationEvent(
                payerOrganization,
                NotificationType.PAYER_ORGANIZATION_REACTIVATED,
                RecipientRole.PAYER,
                payerOrganization.getAdminEmail(),
                "Payer Organization Reactivated",
                "Your Payer organization "
                        + payerOrganization.getOrganizationName()
                        + " has been reactivated. "
                        + "You can resume Payer operations.",
                resolveOccurredAt(
                        payerOrganization,
                        NotificationType.PAYER_ORGANIZATION_REACTIVATED
                )
        );
    }

    @Override
    public void publishOrganizationDeactivated(
            PayerOrganization payerOrganization
    ) {

        validateOrganization(
                payerOrganization
        );

        saveOrganizationEvent(
                payerOrganization,
                NotificationType.PAYER_ORGANIZATION_DEACTIVATED,
                RecipientRole.PAYER,
                payerOrganization.getAdminEmail(),
                "Payer Organization Deactivated",
                "Your Payer organization "
                        + payerOrganization.getOrganizationName()
                        + " has been deactivated. "
                        + "Payer business operations are unavailable.",
                resolveOccurredAt(
                        payerOrganization,
                        NotificationType.PAYER_ORGANIZATION_DEACTIVATED
                )
        );
    }

    private void saveOrganizationEvent(
            PayerOrganization payerOrganization,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        HealthcareEvent event =
                buildOrganizationEvent(
                        payerOrganization,
                        notificationType,
                        recipientRole,
                        recipientEmail,
                        title,
                        message,
                        occurredAt
                );

        OutboxEvent outboxEvent =
                buildOutboxEvent(
                        payerOrganization,
                        event
                );

        outboxEventRepository.saveAndFlush(
                outboxEvent
        );
    }

    private HealthcareEvent buildOrganizationEvent(
            PayerOrganization payerOrganization,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        if (notificationType == null) {

            throw new IllegalArgumentException(
                    "Payer organization notification type is required"
            );
        }

        if (recipientRole == null) {

            throw new IllegalArgumentException(
                    "Payer organization notification recipient role is required"
            );
        }

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        notificationType
                )
                .occurredAt(
                        occurredAt != null
                                ? occurredAt
                                : LocalDateTime.now()
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
                        ReferenceType.PAYER_ORGANIZATION
                )
                .referenceId(
                        payerOrganization.getId()
                )
                .title(
                        normalizeRequiredText(
                                title,
                                "Notification title"
                        )
                )
                .message(
                        normalizeRequiredText(
                                message,
                                "Notification message"
                        )
                )
                .metadata(
                        buildMetadata(
                                payerOrganization
                        )
                )
                .build();
    }

    private OutboxEvent buildOutboxEvent(
            PayerOrganization payerOrganization,
            HealthcareEvent event
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
                        AGGREGATE_TYPE
                )
                .aggregateId(
                        payerOrganization.getId()
                )
                .eventType(
                        event.getEventType().name()
                )
                .topicName(
                        normalizeRequiredText(
                                organizationEventsTopic,
                                "Organization Kafka topic"
                        )
                )
                .messageKey(
                        payerOrganization
                                .getId()
                                .toString()
                )
                .payload(
                        serializeEvent(
                                event
                        )
                )
                .status(
                        OutboxEventStatus.PENDING
                )
                .retryCount(
                        0
                )
                .nextAttemptAt(
                        now
                )
                .processingStartedAt(
                        null
                )
                .lastAttemptAt(
                        null
                )
                .publishedAt(
                        null
                )
                .lastError(
                        null
                )
                .createdAt(
                        now
                )
                .updatedAt(
                        now
                )
                .build();
    }

    private Map<String, Object> buildMetadata(
            PayerOrganization payerOrganization
    ) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "payerOrganizationId",
                payerOrganization
                        .getId()
                        .toString()
        );

        metadata.put(
                "organizationName",
                payerOrganization.getOrganizationName()
        );

        metadata.put(
                "registrationNumber",
                payerOrganization.getRegistrationNumber()
        );

        metadata.put(
                "adminEmail",
                payerOrganization.getAdminEmail()
        );

        metadata.put(
                "contactNumber",
                payerOrganization.getContactNumber()
        );

        metadata.put(
                "city",
                payerOrganization.getCity()
        );

        metadata.put(
                "state",
                payerOrganization.getState()
        );

        metadata.put(
                "country",
                payerOrganization.getCountry()
        );

        metadata.put(
                "status",
                payerOrganization
                        .getStatus()
                        .name()
        );

        putOptionalMetadata(
                metadata,
                "reviewedByEmail",
                payerOrganization.getReviewedByEmail()
        );

        putOptionalMetadata(
                metadata,
                "reviewRemarks",
                payerOrganization.getReviewRemarks()
        );

        putOptionalMetadata(
                metadata,
                "rejectionReason",
                payerOrganization.getRejectionReason()
        );

        if (payerOrganization.getSubmittedAt() != null) {

            metadata.put(
                    "submittedAt",
                    payerOrganization
                            .getSubmittedAt()
                            .toString()
            );
        }

        if (payerOrganization.getReviewedAt() != null) {

            metadata.put(
                    "reviewedAt",
                    payerOrganization
                            .getReviewedAt()
                            .toString()
            );
        }

        return metadata;
    }

    private void putOptionalMetadata(
            Map<String, Object> metadata,
            String key,
            String value
    ) {

        String normalizedValue =
                normalizeOptionalText(
                        value
                );

        if (normalizedValue != null) {

            metadata.put(
                    key,
                    normalizedValue
            );
        }
    }

    private LocalDateTime resolveOccurredAt(
            PayerOrganization payerOrganization,
            NotificationType notificationType
    ) {

        if (
                notificationType
                        == NotificationType.PAYER_ORGANIZATION_SUBMITTED
                        && payerOrganization.getSubmittedAt() != null
        ) {

            return payerOrganization.getSubmittedAt();
        }

        if (
                (
                        notificationType
                                == NotificationType.PAYER_ORGANIZATION_APPROVED
                                || notificationType
                                == NotificationType.PAYER_ORGANIZATION_REJECTED
                )
                        && payerOrganization.getReviewedAt() != null
        ) {

            return payerOrganization.getReviewedAt();
        }

        if (payerOrganization.getUpdatedAt() != null) {

            return payerOrganization.getUpdatedAt();
        }

        if (payerOrganization.getCreatedAt() != null) {

            return payerOrganization.getCreatedAt();
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
                    "Unable to serialize Payer organization event",
                    exception
            );
        }
    }

    private void validateOrganization(
            PayerOrganization payerOrganization
    ) {

        if (
                payerOrganization == null
                        || payerOrganization.getId() == null
                        || payerOrganization.getStatus() == null
        ) {

            throw new IllegalArgumentException(
                    "Complete Payer organization information "
                            + "is required for notification"
            );
        }

        normalizeRequiredText(
                payerOrganization.getOrganizationName(),
                "Payer organization name"
        );

        normalizeRequiredText(
                payerOrganization.getRegistrationNumber(),
                "Payer registration number"
        );

        normalizeEmail(
                payerOrganization.getAdminEmail()
        );
    }

    private void validateEvent(
            HealthcareEvent event
    ) {

        if (
                event == null
                        || event.getEventId() == null
                        || event.getEventType() == null
                        || event.getOccurredAt() == null
                        || event.getRecipientRole() == null
                        || event.getReferenceType() == null
                        || event.getReferenceId() == null
        ) {

            throw new IllegalArgumentException(
                    "Complete Payer organization event "
                            + "information is required"
            );
        }

        normalizeEmail(
                event.getRecipientEmail()
        );

        normalizeRequiredText(
                event.getTitle(),
                "Notification title"
        );

        normalizeRequiredText(
                event.getMessage(),
                "Notification message"
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
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeRequiredText(
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

    private String normalizeOptionalText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}