package com.naveen.healthcare.claimservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.claimservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.claimservice.entity.Claim;
import com.naveen.healthcare.claimservice.entity.OutboxEvent;
import com.naveen.healthcare.claimservice.enums.ClaimStatus;
import com.naveen.healthcare.claimservice.enums.NotificationType;
import com.naveen.healthcare.claimservice.enums.OutboxEventStatus;
import com.naveen.healthcare.claimservice.enums.RecipientRole;
import com.naveen.healthcare.claimservice.enums.ReferenceType;
import com.naveen.healthcare.claimservice.repository.OutboxEventRepository;
import com.naveen.healthcare.claimservice.service.ClaimEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimEventPublisherImpl
        implements ClaimEventPublisher {

    private static final String AGGREGATE_TYPE =
            "CLAIM";

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository
            outboxEventRepository;

    @Value(
            "${claim.kafka.topics.claim-events}"
    )
    private String claimEventsTopic;

    @Override
    public void publishClaimSubmitted(
            Claim claim
    ) {

        validateClaim(
                claim
        );

        validateMemberRecipient(
                claim
        );

        validatePayerRecipient(
                claim
        );

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        claim,
                        NotificationType.CLAIM_SUBMITTED
                );

        HealthcareEvent memberEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_SUBMITTED,
                        RecipientRole.MEMBER,
                        claim.getMemberEmail(),
                        "Claim Submitted",
                        "Insurance claim "
                                + getClaimNumber(claim)
                                + " was submitted by "
                                + claim.getProviderOrganizationName()
                                + ".",
                        occurredAt
                );

        HealthcareEvent payerEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_SUBMITTED,
                        RecipientRole.PAYER,
                        claim.getPayerAdminEmail(),
                        "New Claim Submitted",
                        "A new insurance claim "
                                + getClaimNumber(claim)
                                + " was submitted by "
                                + claim.getProviderOrganizationName()
                                + " for Member "
                                + claim.getMemberEmail()
                                + " and requires review.",
                        occurredAt
                );

        saveEvents(
                claim,
                List.of(
                        memberEvent,
                        payerEvent
                )
        );
    }

    @Override
    public void publishClaimUnderReview(
            Claim claim
    ) {

        validateClaim(
                claim
        );

        validateMemberRecipient(
                claim
        );

        validateProviderRecipient(
                claim
        );

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        claim,
                        NotificationType.CLAIM_UNDER_REVIEW
                );

        HealthcareEvent memberEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_UNDER_REVIEW,
                        RecipientRole.MEMBER,
                        claim.getMemberEmail(),
                        "Claim Under Review",
                        "Your insurance claim "
                                + getClaimNumber(claim)
                                + " is currently under review by "
                                + claim.getPayerOrganizationName()
                                + ".",
                        occurredAt
                );

        HealthcareEvent providerEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_UNDER_REVIEW,
                        RecipientRole.PROVIDER,
                        claim.getProviderAdminEmail(),
                        "Claim Under Review",
                        "Insurance claim "
                                + getClaimNumber(claim)
                                + " is currently under review by "
                                + claim.getPayerOrganizationName()
                                + ".",
                        occurredAt
                );

        saveEvents(
                claim,
                List.of(
                        memberEvent,
                        providerEvent
                )
        );
    }

    @Override
    public void publishClaimApproved(
            Claim claim
    ) {

        validateClaim(
                claim
        );

        if (claim.getApprovedAmount() == null) {

            throw new IllegalArgumentException(
                    "Approved amount is required for "
                            + "an APPROVED Claim notification"
            );
        }

        validateMemberRecipient(
                claim
        );

        validateProviderRecipient(
                claim
        );

        String approvedAmount =
                claim.getApprovedAmount()
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        .toPlainString();

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        claim,
                        NotificationType.CLAIM_APPROVED
                );

        HealthcareEvent memberEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_APPROVED,
                        RecipientRole.MEMBER,
                        claim.getMemberEmail(),
                        "Claim Approved",
                        "Your insurance claim "
                                + getClaimNumber(claim)
                                + " has been approved for INR "
                                + approvedAmount
                                + ".",
                        occurredAt
                );

        HealthcareEvent providerEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_APPROVED,
                        RecipientRole.PROVIDER,
                        claim.getProviderAdminEmail(),
                        "Claim Approved",
                        "Insurance claim "
                                + getClaimNumber(claim)
                                + " has been approved for INR "
                                + approvedAmount
                                + " by "
                                + claim.getPayerOrganizationName()
                                + ".",
                        occurredAt
                );

        saveEvents(
                claim,
                List.of(
                        memberEvent,
                        providerEvent
                )
        );
    }

    @Override
    public void publishClaimRejected(
            Claim claim
    ) {

        validateClaim(
                claim
        );

        validateMemberRecipient(
                claim
        );

        validateProviderRecipient(
                claim
        );

        String memberMessage =
                "Your insurance claim "
                        + getClaimNumber(claim)
                        + " has been rejected.";

        String providerMessage =
                "Insurance claim "
                        + getClaimNumber(claim)
                        + " has been rejected by "
                        + claim.getPayerOrganizationName()
                        + ".";

        if (claim.getRejectionReason() != null
                && !claim.getRejectionReason().isBlank()) {

            String reason =
                    claim.getRejectionReason().trim();

            memberMessage +=
                    " Reason: " + reason;

            providerMessage +=
                    " Reason: " + reason;
        }

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        claim,
                        NotificationType.CLAIM_REJECTED
                );

        HealthcareEvent memberEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_REJECTED,
                        RecipientRole.MEMBER,
                        claim.getMemberEmail(),
                        "Claim Rejected",
                        memberMessage,
                        occurredAt
                );

        HealthcareEvent providerEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_REJECTED,
                        RecipientRole.PROVIDER,
                        claim.getProviderAdminEmail(),
                        "Claim Rejected",
                        providerMessage,
                        occurredAt
                );

        saveEvents(
                claim,
                List.of(
                        memberEvent,
                        providerEvent
                )
        );
    }

    @Override
    public void publishClaimCancelled(
            Claim claim
    ) {

        validateClaim(
                claim
        );

        validateMemberRecipient(
                claim
        );

        validatePayerRecipient(
                claim
        );

        String memberMessage =
                "Your insurance claim "
                        + getClaimNumber(claim)
                        + " has been cancelled by "
                        + claim.getProviderOrganizationName()
                        + ".";

        String payerMessage =
                "Insurance claim "
                        + getClaimNumber(claim)
                        + " was cancelled by "
                        + claim.getProviderOrganizationName()
                        + ".";

        if (claim.getCancellationReason() != null
                && !claim.getCancellationReason().isBlank()) {

            String reason =
                    claim.getCancellationReason().trim();

            memberMessage +=
                    " Reason: " + reason;

            payerMessage +=
                    " Reason: " + reason;
        }

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        claim,
                        NotificationType.CLAIM_CANCELLED
                );

        HealthcareEvent memberEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_CANCELLED,
                        RecipientRole.MEMBER,
                        claim.getMemberEmail(),
                        "Claim Cancelled",
                        memberMessage,
                        occurredAt
                );

        HealthcareEvent payerEvent =
                buildEvent(
                        claim,
                        NotificationType.CLAIM_CANCELLED,
                        RecipientRole.PAYER,
                        claim.getPayerAdminEmail(),
                        "Claim Cancelled",
                        payerMessage,
                        occurredAt
                );

        saveEvents(
                claim,
                List.of(
                        memberEvent,
                        payerEvent
                )
        );
    }

    private void saveEvents(
            Claim claim,
            List<HealthcareEvent> events
    ) {

        List<OutboxEvent> outboxEvents =
                events.stream()
                        .map(event ->
                                buildOutboxEvent(
                                        claim,
                                        event
                                )
                        )
                        .toList();

        outboxEventRepository.saveAllAndFlush(
                outboxEvents
        );
    }

    private HealthcareEvent buildEvent(
            Claim claim,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        if (notificationType == null) {

            throw new IllegalArgumentException(
                    "Claim notification type is required"
            );
        }

        if (recipientRole == null) {

            throw new IllegalArgumentException(
                    "Claim notification recipient role is required"
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
                        occurredAt == null
                                ? LocalDateTime.now()
                                : occurredAt
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
                        ReferenceType.CLAIM
                )
                .referenceId(
                        claim.getId()
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
                        buildMetadata(
                                claim
                        )
                )
                .build();
    }

    private OutboxEvent buildOutboxEvent(
            Claim claim,
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
                        claim.getId()
                )
                .eventType(
                        event.getEventType().name()
                )
                .topicName(
                        normalizeText(
                                claimEventsTopic,
                                "Claim Kafka topic"
                        )
                )
                .messageKey(
                        claim.getId().toString()
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
            Claim claim
    ) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "claimId",
                claim.getId().toString()
        );

        metadata.put(
                "claimNumber",
                claim.getClaimNumber()
        );

        metadata.put(
                "appointmentId",
                claim.getAppointmentId().toString()
        );

        metadata.put(
                "medicalRecordId",
                claim.getMedicalRecordId().toString()
        );

        metadata.put(
                "policyId",
                claim.getPolicyId().toString()
        );

        metadata.put(
                "policyNumber",
                claim.getPolicyNumber()
        );

        metadata.put(
                "memberId",
                claim.getMemberId().toString()
        );

        metadata.put(
                "providerId",
                claim.getProviderId().toString()
        );

        metadata.put(
                "providerOrganizationName",
                claim.getProviderOrganizationName()
        );

        metadata.put(
                "payerId",
                claim.getPayerId().toString()
        );

        metadata.put(
                "payerOrganizationName",
                claim.getPayerOrganizationName()
        );

        metadata.put(
                "planId",
                claim.getPlanId().toString()
        );

        metadata.put(
                "planCode",
                claim.getPlanCode()
        );

        metadata.put(
                "planName",
                claim.getPlanName()
        );

        metadata.put(
                "claimedAmount",
                claim.getClaimedAmount()
        );

        metadata.put(
                "approvedAmount",
                claim.getApprovedAmount()
        );

        metadata.put(
                "status",
                claim.getStatus().name()
        );

        metadata.put(
                "serviceDate",
                claim.getServiceDate().toString()
        );

        if (claim.getReviewRemarks() != null
                && !claim.getReviewRemarks().isBlank()) {

            metadata.put(
                    "reviewRemarks",
                    claim.getReviewRemarks().trim()
            );
        }

        if (claim.getRejectionReason() != null
                && !claim.getRejectionReason().isBlank()) {

            metadata.put(
                    "rejectionReason",
                    claim.getRejectionReason().trim()
            );
        }

        if (claim.getCancellationReason() != null
                && !claim.getCancellationReason().isBlank()) {

            metadata.put(
                    "cancellationReason",
                    claim.getCancellationReason().trim()
            );
        }

        return metadata;
    }

    private LocalDateTime resolveOccurredAt(
            Claim claim,
            NotificationType notificationType
    ) {

        if (notificationType
                == NotificationType.CLAIM_SUBMITTED
                && claim.getSubmittedAt() != null) {

            return claim.getSubmittedAt();
        }

        if (notificationType
                == NotificationType.CLAIM_UNDER_REVIEW
                && claim.getReviewedAt() != null) {

            return claim.getReviewedAt();
        }

        if ((notificationType
                == NotificationType.CLAIM_APPROVED
                || notificationType
                == NotificationType.CLAIM_REJECTED)
                && claim.getResolvedAt() != null) {

            return claim.getResolvedAt();
        }

        if (notificationType
                == NotificationType.CLAIM_CANCELLED) {

            if (claim.getCancelledAt() != null) {
                return claim.getCancelledAt();
            }

            if (claim.getResolvedAt() != null) {
                return claim.getResolvedAt();
            }
        }

        if (claim.getUpdatedAt() != null) {
            return claim.getUpdatedAt();
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
                    "Unable to serialize Claim event",
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
                || event.getOccurredAt() == null
                || event.getRecipientRole() == null
                || event.getReferenceType() == null
                || event.getReferenceId() == null) {

            throw new IllegalArgumentException(
                    "Complete Claim event information is required"
            );
        }

        normalizeEmail(
                event.getRecipientEmail()
        );

        normalizeText(
                event.getTitle(),
                "Claim event title"
        );

        normalizeText(
                event.getMessage(),
                "Claim event message"
        );
    }

    private void validateClaim(
            Claim claim
    ) {

        if (claim == null) {

            throw new IllegalArgumentException(
                    "Claim is required"
            );
        }

        if (claim.getId() == null) {

            throw new IllegalArgumentException(
                    "Claim ID is required"
            );
        }

        if (claim.getMemberId() == null) {

            throw new IllegalArgumentException(
                    "Claim Member ID is required"
            );
        }

        if (claim.getProviderId() == null) {

            throw new IllegalArgumentException(
                    "Claim Provider ID is required"
            );
        }

        if (claim.getPayerId() == null) {

            throw new IllegalArgumentException(
                    "Claim Payer ID is required"
            );
        }

        if (claim.getAppointmentId() == null) {

            throw new IllegalArgumentException(
                    "Claim Appointment ID is required"
            );
        }

        if (claim.getMedicalRecordId() == null) {

            throw new IllegalArgumentException(
                    "Claim Medical Record ID is required"
            );
        }

        if (claim.getPolicyId() == null) {

            throw new IllegalArgumentException(
                    "Claim Policy ID is required"
            );
        }

        if (claim.getPlanId() == null) {

            throw new IllegalArgumentException(
                    "Claim Plan ID is required"
            );
        }

        if (claim.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Claim status is required"
            );
        }

        if (claim.getServiceDate() == null) {

            throw new IllegalArgumentException(
                    "Claim service date is required"
            );
        }

        if (claim.getClaimedAmount() == null) {

            throw new IllegalArgumentException(
                    "Claimed amount is required"
            );
        }

        if (claim.getStatus()
                == ClaimStatus.APPROVED
                && claim.getApprovedAmount() == null) {

            throw new IllegalArgumentException(
                    "Approved amount is required "
                            + "for an APPROVED Claim"
            );
        }

        normalizeText(
                claim.getClaimNumber(),
                "Claim number"
        );

        normalizeText(
                claim.getProviderOrganizationName(),
                "Provider organization name"
        );

        normalizeText(
                claim.getPayerOrganizationName(),
                "Payer organization name"
        );

        normalizeText(
                claim.getPlanName(),
                "Plan name"
        );
    }

    private void validateMemberRecipient(
            Claim claim
    ) {

        normalizeEmail(
                claim.getMemberEmail()
        );
    }

    private void validateProviderRecipient(
            Claim claim
    ) {

        normalizeEmail(
                claim.getProviderAdminEmail()
        );
    }

    private void validatePayerRecipient(
            Claim claim
    ) {

        normalizeEmail(
                claim.getPayerAdminEmail()
        );
    }

    private String getClaimNumber(
            Claim claim
    ) {

        return normalizeText(
                claim.getClaimNumber(),
                "Claim number"
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