package com.naveen.healthcare.providerservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.providerservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.providerservice.entity.OutboxEvent;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.NotificationType;
import com.naveen.healthcare.providerservice.enums.OutboxEventStatus;
import com.naveen.healthcare.providerservice.enums.RecipientRole;
import com.naveen.healthcare.providerservice.enums.ReferenceType;
import com.naveen.healthcare.providerservice.repository.OutboxEventRepository;
import com.naveen.healthcare.providerservice.service.ProviderOrganizationEventPublisher;
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
public class ProviderOrganizationEventPublisherImpl
        implements ProviderOrganizationEventPublisher {

    private static final String AGGREGATE_TYPE =
            "PROVIDER_ORGANIZATION";

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository
            outboxEventRepository;

    @Value(
            "${provider.kafka.topics.organization-events}"
    )
    private String organizationEventsTopic;

    @Value(
            "${platform.admin.notification-email}"
    )
    private String platformAdminNotificationEmail;

    @Override
    public void publishOrganizationSubmitted(
            ProviderOrganization providerOrganization
    ) {

        validateOrganization(
                providerOrganization
        );

        String title =
                "New Provider Organization Application";

        String message =
                providerOrganization.getOrganizationName()
                        + " submitted a Provider organization "
                        + "application and requires review.";

        saveOrganizationEvent(
                providerOrganization,
                NotificationType.PROVIDER_ORGANIZATION_SUBMITTED,
                RecipientRole.ADMIN,
                platformAdminNotificationEmail,
                title,
                message,
                resolveOccurredAt(
                        providerOrganization,
                        NotificationType.PROVIDER_ORGANIZATION_SUBMITTED
                )
        );
    }

    @Override
    public void publishOrganizationApproved(
            ProviderOrganization providerOrganization
    ) {

        validateOrganization(
                providerOrganization
        );

        String title =
                "Provider Organization Approved";

        String message =
                "Your Provider organization "
                        + providerOrganization.getOrganizationName()
                        + " has been approved. "
                        + "You can now access Provider operations.";

        String reviewRemarks =
                normalizeOptionalText(
                        providerOrganization.getReviewRemarks()
                );

        if (reviewRemarks != null) {

            message += " Review remarks: "
                    + reviewRemarks;
        }

        saveOrganizationEvent(
                providerOrganization,
                NotificationType.PROVIDER_ORGANIZATION_APPROVED,
                RecipientRole.PROVIDER,
                providerOrganization.getAdminEmail(),
                title,
                message,
                resolveOccurredAt(
                        providerOrganization,
                        NotificationType.PROVIDER_ORGANIZATION_APPROVED
                )
        );
    }

    @Override
    public void publishOrganizationRejected(
            ProviderOrganization providerOrganization
    ) {

        validateOrganization(
                providerOrganization
        );

        String rejectionReason =
                normalizeRequiredText(
                        providerOrganization.getRejectionReason(),
                        "Provider organization rejection reason"
                );

        String title =
                "Provider Organization Application Rejected";

        String message =
                "Your Provider organization application for "
                        + providerOrganization.getOrganizationName()
                        + " has been rejected. Reason: "
                        + rejectionReason;

        String reviewRemarks =
                normalizeOptionalText(
                        providerOrganization.getReviewRemarks()
                );

        if (reviewRemarks != null) {

            message += " Review remarks: "
                    + reviewRemarks;
        }

        saveOrganizationEvent(
                providerOrganization,
                NotificationType.PROVIDER_ORGANIZATION_REJECTED,
                RecipientRole.PROVIDER,
                providerOrganization.getAdminEmail(),
                title,
                message,
                resolveOccurredAt(
                        providerOrganization,
                        NotificationType.PROVIDER_ORGANIZATION_REJECTED
                )
        );
    }

    @Override
    public void publishOrganizationSuspended(
            ProviderOrganization providerOrganization
    ) {

        validateOrganization(
                providerOrganization
        );

        saveOrganizationEvent(
                providerOrganization,
                NotificationType.PROVIDER_ORGANIZATION_SUSPENDED,
                RecipientRole.PROVIDER,
                providerOrganization.getAdminEmail(),
                "Provider Organization Suspended",
                "Your Provider organization "
                        + providerOrganization.getOrganizationName()
                        + " has been suspended. "
                        + "Provider business operations are "
                        + "temporarily unavailable.",
                resolveOccurredAt(
                        providerOrganization,
                        NotificationType.PROVIDER_ORGANIZATION_SUSPENDED
                )
        );
    }

    @Override
    public void publishOrganizationReactivated(
            ProviderOrganization providerOrganization
    ) {

        validateOrganization(
                providerOrganization
        );

        saveOrganizationEvent(
                providerOrganization,
                NotificationType.PROVIDER_ORGANIZATION_REACTIVATED,
                RecipientRole.PROVIDER,
                providerOrganization.getAdminEmail(),
                "Provider Organization Reactivated",
                "Your Provider organization "
                        + providerOrganization.getOrganizationName()
                        + " has been reactivated. "
                        + "You can resume Provider operations.",
                resolveOccurredAt(
                        providerOrganization,
                        NotificationType.PROVIDER_ORGANIZATION_REACTIVATED
                )
        );
    }

    @Override
    public void publishOrganizationDeactivated(
            ProviderOrganization providerOrganization
    ) {

        validateOrganization(
                providerOrganization
        );

        saveOrganizationEvent(
                providerOrganization,
                NotificationType.PROVIDER_ORGANIZATION_DEACTIVATED,
                RecipientRole.PROVIDER,
                providerOrganization.getAdminEmail(),
                "Provider Organization Deactivated",
                "Your Provider organization "
                        + providerOrganization.getOrganizationName()
                        + " has been deactivated. "
                        + "Provider business operations are unavailable.",
                resolveOccurredAt(
                        providerOrganization,
                        NotificationType.PROVIDER_ORGANIZATION_DEACTIVATED
                )
        );
    }

    private void saveOrganizationEvent(
            ProviderOrganization providerOrganization,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        HealthcareEvent event =
                buildOrganizationEvent(
                        providerOrganization,
                        notificationType,
                        recipientRole,
                        recipientEmail,
                        title,
                        message,
                        occurredAt
                );

        OutboxEvent outboxEvent =
                buildOutboxEvent(
                        providerOrganization,
                        event
                );

        outboxEventRepository.saveAndFlush(
                outboxEvent
        );
    }

    private HealthcareEvent buildOrganizationEvent(
            ProviderOrganization providerOrganization,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        if (notificationType == null) {

            throw new IllegalArgumentException(
                    "Provider organization notification "
                            + "type is required"
            );
        }

        if (recipientRole == null) {

            throw new IllegalArgumentException(
                    "Provider organization notification "
                            + "recipient role is required"
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
                        ReferenceType.PROVIDER_ORGANIZATION
                )
                .referenceId(
                        providerOrganization.getId()
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
                                providerOrganization
                        )
                )
                .build();
    }

    private OutboxEvent buildOutboxEvent(
            ProviderOrganization providerOrganization,
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
                        providerOrganization.getId()
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
                        providerOrganization
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
            ProviderOrganization providerOrganization
    ) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "providerOrganizationId",
                providerOrganization
                        .getId()
                        .toString()
        );

        metadata.put(
                "organizationName",
                providerOrganization.getOrganizationName()
        );

        metadata.put(
                "providerType",
                providerOrganization
                        .getProviderType()
                        .name()
        );

        metadata.put(
                "registrationNumber",
                providerOrganization.getRegistrationNumber()
        );

        metadata.put(
                "licenseNumber",
                providerOrganization.getLicenseNumber()
        );

        metadata.put(
                "adminEmail",
                providerOrganization.getAdminEmail()
        );

        metadata.put(
                "contactNumber",
                providerOrganization.getContactNumber()
        );

        metadata.put(
                "city",
                providerOrganization.getCity()
        );

        metadata.put(
                "state",
                providerOrganization.getState()
        );

        metadata.put(
                "country",
                providerOrganization.getCountry()
        );

        metadata.put(
                "emergencyServicesAvailable",
                providerOrganization
                        .getEmergencyServicesAvailable()
        );

        metadata.put(
                "status",
                providerOrganization
                        .getStatus()
                        .name()
        );

        putOptionalMetadata(
                metadata,
                "reviewedByEmail",
                providerOrganization.getReviewedByEmail()
        );

        putOptionalMetadata(
                metadata,
                "reviewRemarks",
                providerOrganization.getReviewRemarks()
        );

        putOptionalMetadata(
                metadata,
                "rejectionReason",
                providerOrganization.getRejectionReason()
        );

        if (
                providerOrganization.getSubmittedAt()
                        != null
        ) {

            metadata.put(
                    "submittedAt",
                    providerOrganization
                            .getSubmittedAt()
                            .toString()
            );
        }

        if (
                providerOrganization.getReviewedAt()
                        != null
        ) {

            metadata.put(
                    "reviewedAt",
                    providerOrganization
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
            ProviderOrganization providerOrganization,
            NotificationType notificationType
    ) {

        if (
                notificationType
                        == NotificationType
                        .PROVIDER_ORGANIZATION_SUBMITTED
                        && providerOrganization.getSubmittedAt()
                        != null
        ) {

            return providerOrganization.getSubmittedAt();
        }

        if (
                (
                        notificationType
                                == NotificationType
                                .PROVIDER_ORGANIZATION_APPROVED
                                || notificationType
                                == NotificationType
                                .PROVIDER_ORGANIZATION_REJECTED
                )
                        && providerOrganization.getReviewedAt()
                        != null
        ) {

            return providerOrganization.getReviewedAt();
        }

        if (
                providerOrganization.getUpdatedAt()
                        != null
        ) {

            return providerOrganization.getUpdatedAt();
        }

        if (
                providerOrganization.getCreatedAt()
                        != null
        ) {

            return providerOrganization.getCreatedAt();
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

        } catch (
                JsonProcessingException exception
        ) {

            throw new IllegalStateException(
                    "Unable to serialize Provider "
                            + "organization event",
                    exception
            );
        }
    }

    private void validateOrganization(
            ProviderOrganization providerOrganization
    ) {

        if (
                providerOrganization == null
                        || providerOrganization.getId() == null
                        || providerOrganization.getProviderType()
                        == null
                        || providerOrganization.getStatus()
                        == null
                        || providerOrganization
                        .getEmergencyServicesAvailable()
                        == null
        ) {

            throw new IllegalArgumentException(
                    "Complete Provider organization "
                            + "information is required "
                            + "for notification"
            );
        }

        normalizeRequiredText(
                providerOrganization.getOrganizationName(),
                "Provider organization name"
        );

        normalizeRequiredText(
                providerOrganization.getRegistrationNumber(),
                "Provider registration number"
        );

        normalizeRequiredText(
                providerOrganization.getLicenseNumber(),
                "Provider license number"
        );

        normalizeEmail(
                providerOrganization.getAdminEmail()
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
                    "Complete Provider organization "
                            + "event information is required"
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

        if (
                email == null
                        || email.isBlank()
        ) {

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

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            return null;
        }

        return value.trim();
    }
}