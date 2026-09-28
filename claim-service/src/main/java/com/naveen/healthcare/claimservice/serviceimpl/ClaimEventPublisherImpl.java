/*
package com.naveen.healthcare.claimservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.claimservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.claimservice.entity.Claim;
import com.naveen.healthcare.claimservice.enums.NotificationType;
import com.naveen.healthcare.claimservice.enums.RecipientRole;
import com.naveen.healthcare.claimservice.enums.ReferenceType;
import com.naveen.healthcare.claimservice.service.ClaimEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.naveen.healthcare.claimservice.enums.ClaimStatus;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimEventPublisherImpl
        implements ClaimEventPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ClaimEventPublisherImpl.class
            );

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${claim.kafka.topics.claim-events}")
    private String claimEventsTopic;

    */
/*@Override
    public void publishClaimApproved(
            Claim claim
    ) {

        validateClaimForPublishing(claim);

        HealthcareEvent event =
                buildClaimApprovedEvent(claim);

        String eventJson =
                convertEventToJson(event);

        String messageKey =
                claim.getId().toString();

        try {

            kafkaTemplate
                    .send(
                            claimEventsTopic,
                            messageKey,
                            eventJson
                    )
                    .whenComplete(
                            (sendResult, exception) -> {

                                if (exception != null) {

                                    LOGGER.error(
                                            "Failed to publish "
                                                    + "CLAIM_APPROVED event. "
                                                    + "Claim ID: {}, "
                                                    + "event ID: {}",
                                            claim.getId(),
                                            event.getEventId(),
                                            exception
                                    );

                                    return;
                                }

                                LOGGER.info(
                                        "Published CLAIM_APPROVED event. "
                                                + "Claim ID: {}, "
                                                + "event ID: {}, "
                                                + "topic: {}, "
                                                + "partition: {}, "
                                                + "offset: {}",
                                        claim.getId(),
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

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to submit CLAIM_APPROVED event "
                            + "to Kafka. Claim ID: {}, event ID: {}",
                    claim.getId(),
                    event.getEventId(),
                    exception
            );
        }
    }*//*


    @Override
    public void publishClaimApproved(
            Claim claim
    ) {

        String approvedAmount =
                claim != null
                        && claim.getApprovedAmount() != null
                        ? claim.getApprovedAmount()
                        .setScale(2)
                        .toPlainString()
                        : "0.00";

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_APPROVED,
                "Claim Approved",
                "Your insurance claim "
                        + getClaimNumberSafely(claim)
                        + " has been approved for INR "
                        + approvedAmount
                        + "."
        );
    }
    @Override
    public void publishClaimSubmitted(
            Claim claim
    ) {
        publishClaimEvent(
                claim,
                NotificationType.CLAIM_SUBMITTED,
                "Claim Submitted",
                "Your insurance claim "
                        + claim.getClaimNumber()
                        + " has been submitted successfully."
        );
    }

    @Override
    public void publishClaimUnderReview(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_UNDER_REVIEW,
                "Claim Under Review",
                "Your insurance claim "
                        + claim.getClaimNumber()
                        + " is currently under review."
        );
    }

    @Override
    public void publishClaimRejected(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_REJECTED,
                "Claim Rejected",
                "Your insurance claim "
                        + claim.getClaimNumber()
                        + " has been rejected."
        );
    }

    @Override
    public void publishClaimCancelled(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_CANCELLED,
                "Claim Cancelled",
                "Your insurance claim "
                        + claim.getClaimNumber()
                        + " has been cancelled."
        );
    }

    private HealthcareEvent buildClaimEvent(
            Claim claim,
            NotificationType eventType,
            String title,
            String message
    ) {

        LocalDateTime occurredAt =
                claim.getResolvedAt() != null
                        ? claim.getResolvedAt()
                        : LocalDateTime.now();

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "claimId",
                claim.getId()
        );

        metadata.put(
                "claimNumber",
                claim.getClaimNumber()
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
                "providerOrganizationName",
                claim.getProviderOrganizationName()
        );

        metadata.put(
                "payerOrganizationName",
                claim.getPayerOrganizationName()
        );

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        eventType
                )
                .occurredAt(
                        occurredAt
                )
                .recipientEmail(
                        normalizeEmail(
                                claim.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.CLAIM
                )
                .referenceId(
                        claim.getId()
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

    private HealthcareEvent buildClaimApprovedEvent(
            Claim claim
    ) {

        UUID eventId =
                UUID.randomUUID();

        LocalDateTime occurredAt =
                claim.getResolvedAt() != null
                        ? claim.getResolvedAt()
                        : LocalDateTime.now();

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
                "payerOrganizationName",
                claim.getPayerOrganizationName()
        );

        metadata.put(
                "providerOrganizationName",
                claim.getProviderOrganizationName()
        );

        String message =
                "Your insurance claim "
                        + claim.getClaimNumber()
                        + " has been approved for INR "
                        + claim.getApprovedAmount()
                        .setScale(2)
                        .toPlainString()
                        + ".";

        return HealthcareEvent.builder()
                .eventId(eventId)
                .eventType(
                        NotificationType.CLAIM_APPROVED
                )
                .occurredAt(occurredAt)
                .recipientEmail(
                        normalizeEmail(
                                claim.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.CLAIM
                )
                .referenceId(
                        claim.getId()
                )
                .title(
                        "Claim Approved"
                )
                .message(message)
                .metadata(metadata)
                .build();
    }

    private String convertEventToJson(
            HealthcareEvent event
    ) {

        try {

            return objectMapper.writeValueAsString(event);

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Unable to convert CLAIM_APPROVED "
                            + "event to JSON",
                    exception
            );
        }
    }

    private void validateClaimForPublishing(
            Claim claim
    ) {

        if (claim == null) {
            throw new IllegalArgumentException(
                    "Claim is required for event publishing"
            );
        }

        if (claim.getId() == null) {
            throw new IllegalArgumentException(
                    "Claim ID is required for event publishing"
            );
        }

        if (claim.getClaimNumber() == null
                || claim.getClaimNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Claim number is required "
                            + "for event publishing"
            );
        }

        if (claim.getMemberEmail() == null
                || claim.getMemberEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Claim Member email is required "
                            + "for event publishing"
            );
        }

        if (claim.getStatus() == ClaimStatus.APPROVED
                && claim.getApprovedAmount() == null) {

            throw new IllegalArgumentException(
                    "Approved amount is required "
                            + "for APPROVED claim event publishing"
            );
        }


        if (claim.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Claim status is required "
                            + "for event publishing"
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

    */
/*private void publishClaimEvent(
            Claim claim,
            NotificationType eventType,
            String title,
            String message
    ) {

        validateClaimForPublishing(claim);

        HealthcareEvent event =
                buildClaimEvent(
                        claim,
                        eventType,
                        title,
                        message
                );

        String eventJson =
                convertEventToJson(event);

        String messageKey =
                claim.getId().toString();

        try {

            kafkaTemplate
                    .send(
                            claimEventsTopic,
                            messageKey,
                            eventJson
                    )
                    .whenComplete(
                            (sendResult, exception) -> {

                                if (exception != null) {

                                    LOGGER.error(
                                            "Failed to publish {} event. "
                                                    + "Claim ID: {}, "
                                                    + "Event ID: {}",
                                            eventType,
                                            claim.getId(),
                                            event.getEventId(),
                                            exception
                                    );

                                    return;
                                }

                                LOGGER.info(
                                        "Published {} event. "
                                                + "Claim ID: {}, "
                                                + "Event ID: {}, "
                                                + "Topic: {}, "
                                                + "Partition: {}, "
                                                + "Offset: {}",
                                        eventType,
                                        claim.getId(),
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

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to submit {} event to Kafka. "
                            + "Claim ID: {}, "
                            + "Event ID: {}",
                    eventType,
                    claim.getId(),
                    event.getEventId(),
                    exception
            );
        }
    }*//*

    private void publishClaimEvent(
            Claim claim,
            NotificationType eventType,
            String title,
            String message
    ) {

        try {

            validateClaimForPublishing(
                    claim
            );

            HealthcareEvent event =
                    buildClaimEvent(
                            claim,
                            eventType,
                            title,
                            message
                    );

            String eventJson =
                    convertEventToJson(
                            event
                    );

            String messageKey =
                    claim.getId().toString();

            kafkaTemplate
                    .send(
                            claimEventsTopic,
                            messageKey,
                            eventJson
                    )
                    .whenComplete(
                            (sendResult, exception) -> {

                                if (exception != null) {

                                    LOGGER.error(
                                            "Failed to publish {} event. "
                                                    + "Claim ID: {}, "
                                                    + "Event ID: {}",
                                            eventType,
                                            claim.getId(),
                                            event.getEventId(),
                                            exception
                                    );

                                    return;
                                }

                                LOGGER.info(
                                        "Published {} event. "
                                                + "Claim ID: {}, "
                                                + "Event ID: {}, "
                                                + "Topic: {}, "
                                                + "Partition: {}, "
                                                + "Offset: {}",
                                        eventType,
                                        claim.getId(),
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

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to publish {} event to Kafka. "
                            + "Claim ID: {}. "
                            + "The Claim operation remains successful.",
                    eventType,
                    claim == null
                            ? null
                            : claim.getId(),
                    exception
            );
        }
    }
    private String getClaimNumberSafely(
            Claim claim
    ) {

        if (claim == null
                || claim.getClaimNumber() == null
                || claim.getClaimNumber().isBlank()) {

            return "";
        }

        return claim.getClaimNumber().trim();
    }
}*/




package com.naveen.healthcare.claimservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.claimservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.claimservice.entity.Claim;
import com.naveen.healthcare.claimservice.enums.ClaimStatus;
import com.naveen.healthcare.claimservice.enums.NotificationType;
import com.naveen.healthcare.claimservice.enums.RecipientRole;
import com.naveen.healthcare.claimservice.enums.ReferenceType;
import com.naveen.healthcare.claimservice.service.ClaimEventPublisher;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClaimEventPublisherImpl
        implements ClaimEventPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    ClaimEventPublisherImpl.class
            );

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${claim.kafka.topics.claim-events}")
    private String claimEventsTopic;

    @Override
    public void publishClaimSubmitted(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_SUBMITTED,
                "Claim Submitted",
                "Your insurance claim "
                        + getClaimNumberSafely(claim)
                        + " has been submitted successfully."
        );
    }

    @Override
    public void publishClaimUnderReview(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_UNDER_REVIEW,
                "Claim Under Review",
                "Your insurance claim "
                        + getClaimNumberSafely(claim)
                        + " is currently under review."
        );
    }

    @Override
    public void publishClaimApproved(
            Claim claim
    ) {

        String approvedAmount =
                getApprovedAmountSafely(claim);

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_APPROVED,
                "Claim Approved",
                "Your insurance claim "
                        + getClaimNumberSafely(claim)
                        + " has been approved for INR "
                        + approvedAmount
                        + "."
        );
    }

    @Override
    public void publishClaimRejected(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_REJECTED,
                "Claim Rejected",
                "Your insurance claim "
                        + getClaimNumberSafely(claim)
                        + " has been rejected."
        );
    }

    @Override
    public void publishClaimCancelled(
            Claim claim
    ) {

        publishClaimEvent(
                claim,
                NotificationType.CLAIM_CANCELLED,
                "Claim Cancelled",
                "Your insurance claim "
                        + getClaimNumberSafely(claim)
                        + " has been cancelled."
        );
    }

    private void publishClaimEvent(
            Claim claim,
            NotificationType eventType,
            String title,
            String message
    ) {

        try {

            validateClaimForPublishing(
                    claim
            );

            HealthcareEvent event =
                    buildClaimEvent(
                            claim,
                            eventType,
                            title,
                            message
                    );

            String eventJson =
                    convertEventToJson(
                            event
                    );

            String messageKey =
                    claim.getId().toString();

            kafkaTemplate
                    .send(
                            claimEventsTopic,
                            messageKey,
                            eventJson
                    )
                    .whenComplete(
                            (sendResult, exception) -> {

                                if (exception != null) {

                                    LOGGER.error(
                                            "Failed to publish {} event. "
                                                    + "Claim ID: {}, "
                                                    + "event ID: {}",
                                            eventType,
                                            claim.getId(),
                                            event.getEventId(),
                                            exception
                                    );

                                    return;
                                }

                                LOGGER.info(
                                        "Published {} event. "
                                                + "Claim ID: {}, "
                                                + "event ID: {}, "
                                                + "topic: {}, "
                                                + "partition: {}, "
                                                + "offset: {}",
                                        eventType,
                                        claim.getId(),
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

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to publish {} event to Kafka. "
                            + "Claim ID: {}. "
                            + "The Claim operation remains successful.",
                    eventType,
                    claim == null
                            ? null
                            : claim.getId(),
                    exception
            );
        }
    }

    private HealthcareEvent buildClaimEvent(
            Claim claim,
            NotificationType eventType,
            String title,
            String message
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
                "memberId",
                claim.getMemberId()
        );

        metadata.put(
                "providerId",
                claim.getProviderId()
        );

        metadata.put(
                "providerOrganizationName",
                claim.getProviderOrganizationName()
        );

        metadata.put(
                "payerId",
                claim.getPayerId()
        );

        metadata.put(
                "payerOrganizationName",
                claim.getPayerOrganizationName()
        );

        metadata.put(
                "planId",
                claim.getPlanId()
        );

        metadata.put(
                "planCode",
                claim.getPlanCode()
        );

        metadata.put(
                "planName",
                claim.getPlanName()
        );

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        claim,
                        eventType
                );

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        eventType
                )
                .occurredAt(
                        occurredAt
                )
                .recipientEmail(
                        normalizeEmail(
                                claim.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.CLAIM
                )
                .referenceId(
                        claim.getId()
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

    private LocalDateTime resolveOccurredAt(
            Claim claim,
            NotificationType eventType
    ) {

        if (eventType == NotificationType.CLAIM_SUBMITTED
                && claim.getSubmittedAt() != null) {

            return claim.getSubmittedAt();
        }

        if (eventType == NotificationType.CLAIM_UNDER_REVIEW
                && claim.getReviewedAt() != null) {

            return claim.getReviewedAt();
        }

        if ((eventType == NotificationType.CLAIM_APPROVED
                || eventType == NotificationType.CLAIM_REJECTED
                || eventType == NotificationType.CLAIM_CANCELLED)
                && claim.getResolvedAt() != null) {

            return claim.getResolvedAt();
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
                    "Unable to convert Claim event to JSON",
                    exception
            );
        }
    }

    private void validateClaimForPublishing(
            Claim claim
    ) {

        if (claim == null) {

            throw new IllegalArgumentException(
                    "Claim is required for event publishing"
            );
        }

        if (claim.getId() == null) {

            throw new IllegalArgumentException(
                    "Claim ID is required for event publishing"
            );
        }

        if (claim.getClaimNumber() == null
                || claim.getClaimNumber().isBlank()) {

            throw new IllegalArgumentException(
                    "Claim number is required "
                            + "for event publishing"
            );
        }

        if (claim.getMemberEmail() == null
                || claim.getMemberEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Claim Member email is required "
                            + "for event publishing"
            );
        }

        if (claim.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Claim status is required "
                            + "for event publishing"
            );
        }

        if (claim.getStatus() == ClaimStatus.APPROVED
                && claim.getApprovedAmount() == null) {

            throw new IllegalArgumentException(
                    "Approved amount is required "
                            + "for APPROVED Claim event publishing"
            );
        }
    }

    private String getClaimNumberSafely(
            Claim claim
    ) {

        if (claim == null
                || claim.getClaimNumber() == null
                || claim.getClaimNumber().isBlank()) {

            return "";
        }

        return claim
                .getClaimNumber()
                .trim();
    }

    private String getApprovedAmountSafely(
            Claim claim
    ) {

        if (claim == null
                || claim.getApprovedAmount() == null) {

            return "0.00";
        }

        return claim
                .getApprovedAmount()
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                )
                .toPlainString();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Claim Member email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}