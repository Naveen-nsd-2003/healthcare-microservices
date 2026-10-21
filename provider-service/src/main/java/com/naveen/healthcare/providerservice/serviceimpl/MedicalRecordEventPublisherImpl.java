package com.naveen.healthcare.providerservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.providerservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.providerservice.entity.MedicalRecord;
import com.naveen.healthcare.providerservice.entity.OutboxEvent;
import com.naveen.healthcare.providerservice.enums.NotificationType;
import com.naveen.healthcare.providerservice.enums.OutboxEventStatus;
import com.naveen.healthcare.providerservice.enums.RecipientRole;
import com.naveen.healthcare.providerservice.enums.ReferenceType;
import com.naveen.healthcare.providerservice.repository.OutboxEventRepository;
import com.naveen.healthcare.providerservice.service.MedicalRecordEventPublisher;
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
public class MedicalRecordEventPublisherImpl
        implements MedicalRecordEventPublisher {

    private static final String AGGREGATE_TYPE =
            "MEDICAL_RECORD";

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository
            outboxEventRepository;

    @Value(
            "${provider.kafka.topics.medical-record-events}"
    )
    private String medicalRecordEventsTopic;

    @Override
    public void publishMedicalRecordCreated(
            MedicalRecord medicalRecord
    ) {

        validateMedicalRecord(
                medicalRecord
        );

        HealthcareEvent event =
                buildEvent(
                        medicalRecord,
                        NotificationType.MEDICAL_RECORD_CREATED,
                        "Medical Record Created",
                        "A Medical Record was created for your "
                                + "appointment with "
                                + medicalRecord.getDoctorName()
                                + " at "
                                + medicalRecord
                                .getProviderOrganizationName()
                                + " on "
                                + medicalRecord.getAppointmentDate()
                                + ".",
                        resolveOccurredAt(
                                medicalRecord.getCreatedAt()
                        )
                );

        saveEvent(
                medicalRecord,
                event
        );
    }

    @Override
    public void publishMedicalRecordAmended(
            MedicalRecord medicalRecord
    ) {

        validateMedicalRecord(
                medicalRecord
        );

        HealthcareEvent event =
                buildEvent(
                        medicalRecord,
                        NotificationType.MEDICAL_RECORD_AMENDED,
                        "Medical Record Amended",
                        "Your Medical Record for the appointment "
                                + "with "
                                + medicalRecord.getDoctorName()
                                + " at "
                                + medicalRecord
                                .getProviderOrganizationName()
                                + " on "
                                + medicalRecord.getAppointmentDate()
                                + " was amended.",
                        resolveOccurredAt(
                                medicalRecord.getUpdatedAt()
                        )
                );

        saveEvent(
                medicalRecord,
                event
        );
    }

    private void saveEvent(
            MedicalRecord medicalRecord,
            HealthcareEvent event
    ) {

        LocalDateTime now =
                LocalDateTime.now();

        OutboxEvent outboxEvent =
                OutboxEvent.builder()
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
                                medicalRecord.getId()
                        )
                        .eventType(
                                event.getEventType().name()
                        )
                        .topicName(
                                normalizeText(
                                        medicalRecordEventsTopic,
                                        "Medical Record Kafka topic"
                                )
                        )
                        .messageKey(
                                medicalRecord.getId().toString()
                        )
                        .payload(
                                serializeEvent(
                                        event
                                )
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

        outboxEventRepository.saveAndFlush(
                outboxEvent
        );
    }

    private HealthcareEvent buildEvent(
            MedicalRecord medicalRecord,
            NotificationType notificationType,
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
                                medicalRecord.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.MEDICAL_RECORD
                )
                .referenceId(
                        medicalRecord.getId()
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
                                medicalRecord
                        )
                )
                .build();
    }

    private Map<String, Object> buildMetadata(
            MedicalRecord medicalRecord
    ) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "medicalRecordId",
                medicalRecord.getId().toString()
        );

        metadata.put(
                "appointmentId",
                medicalRecord.getAppointmentId().toString()
        );

        metadata.put(
                "memberId",
                medicalRecord.getMemberId().toString()
        );

        metadata.put(
                "providerId",
                medicalRecord.getProviderId().toString()
        );

        metadata.put(
                "providerOrganizationName",
                medicalRecord.getProviderOrganizationName()
        );

        metadata.put(
                "departmentId",
                medicalRecord.getDepartmentId().toString()
        );

        metadata.put(
                "departmentName",
                medicalRecord.getDepartmentName()
        );

        metadata.put(
                "doctorId",
                medicalRecord.getDoctorId().toString()
        );

        metadata.put(
                "doctorName",
                medicalRecord.getDoctorName()
        );

        metadata.put(
                "doctorSpecialization",
                medicalRecord.getDoctorSpecialization()
        );

        metadata.put(
                "appointmentDate",
                medicalRecord.getAppointmentDate().toString()
        );

        metadata.put(
                "status",
                medicalRecord.getStatus().name()
        );

        if (medicalRecord.getFollowUpDate() != null) {

            metadata.put(
                    "followUpDate",
                    medicalRecord.getFollowUpDate().toString()
            );
        }

        return metadata;
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
                    "Unable to serialize Medical Record event",
                    exception
            );
        }
    }

    private void validateMedicalRecord(
            MedicalRecord medicalRecord
    ) {

        if (medicalRecord == null) {

            throw new IllegalArgumentException(
                    "Medical Record is required"
            );
        }

        if (medicalRecord.getId() == null
                || medicalRecord.getAppointmentId() == null
                || medicalRecord.getMemberId() == null
                || medicalRecord.getProviderId() == null
                || medicalRecord.getDepartmentId() == null
                || medicalRecord.getDoctorId() == null
                || medicalRecord.getAppointmentDate() == null
                || medicalRecord.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Complete Medical Record information "
                            + "is required for notification"
            );
        }

        normalizeEmail(
                medicalRecord.getMemberEmail()
        );

        normalizeText(
                medicalRecord.getProviderOrganizationName(),
                "Provider organization name"
        );

        normalizeText(
                medicalRecord.getDepartmentName(),
                "Department name"
        );

        normalizeText(
                medicalRecord.getDoctorName(),
                "Doctor name"
        );
    }

    private LocalDateTime resolveOccurredAt(
            LocalDateTime timestamp
    ) {

        return timestamp == null
                ? LocalDateTime.now()
                : timestamp;
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Medical Record Member email is required"
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