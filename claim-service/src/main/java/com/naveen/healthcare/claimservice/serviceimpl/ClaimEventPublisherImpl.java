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

    @Override
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

        if (claim.getClaimedAmount() == null) {
            throw new IllegalArgumentException(
                    "Claimed amount is required "
                            + "for event publishing"
            );
        }

        if (claim.getApprovedAmount() == null) {
            throw new IllegalArgumentException(
                    "Approved amount is required "
                            + "for event publishing"
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
}