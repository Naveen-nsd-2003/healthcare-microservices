/*
package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.appointmentservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.appointmentservice.entity.Appointment;
import com.naveen.healthcare.appointmentservice.enums.NotificationType;
import com.naveen.healthcare.appointmentservice.enums.RecipientRole;
import com.naveen.healthcare.appointmentservice.enums.ReferenceType;
import com.naveen.healthcare.appointmentservice.service.AppointmentEventPublisher;
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
public class AppointmentEventPublisherImpl
        implements AppointmentEventPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    AppointmentEventPublisherImpl.class
            );

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Value("${appointment.kafka.topics.appointment-events}")
    private String appointmentEventsTopic;

    @Override
    public void publishAppointmentBooked(
            Appointment appointment
    ) {

        publishAppointmentEvent(
                appointment,
                NotificationType.APPOINTMENT_BOOKED,
                "Appointment Booked",
                "Your appointment with "
                        + appointment.getDoctorName()
                        + " on "
                        + appointment.getAppointmentDate()
                        + " from "
                        + appointment.getStartTime()
                        + " to "
                        + appointment.getEndTime()
                        + " has been booked successfully."
        );
    }

    @Override
    public void publishAppointmentConfirmed(
            Appointment appointment
    ) {

        publishAppointmentEvent(
                appointment,
                NotificationType.APPOINTMENT_CONFIRMED,
                "Appointment Confirmed",
                "Your appointment with "
                        + appointment.getDoctorName()
                        + " has been confirmed."
        );
    }

    @Override
    public void publishAppointmentCompleted(
            Appointment appointment
    ) {

        publishAppointmentEvent(
                appointment,
                NotificationType.APPOINTMENT_COMPLETED,
                "Appointment Completed",
                "Your appointment with "
                        + appointment.getDoctorName()
                        + " has been completed."
        );
    }

    @Override
    public void publishAppointmentCancelled(
            Appointment appointment
    ) {

        publishAppointmentEvent(
                appointment,
                NotificationType.APPOINTMENT_CANCELLED,
                "Appointment Cancelled",
                "Your appointment with "
                        + appointment.getDoctorName()
                        + " has been cancelled."
        );
    }

    */
/*private void publishAppointmentEvent(
            Appointment appointment,
            NotificationType eventType,
            String title,
            String message
    ) {

        validateAppointmentForPublishing(
                appointment
        );

        HealthcareEvent event =
                buildAppointmentEvent(
                        appointment,
                        eventType,
                        title,
                        message
                );

        String eventJson =
                convertEventToJson(event);

        String messageKey =
                appointment.getId().toString();

        kafkaTemplate.send(
                        appointmentEventsTopic,
                        messageKey,
                        eventJson
                )
                .whenComplete(
                        (sendResult, exception) -> {

                            if (exception != null) {

                                LOGGER.error(
                                        "Failed to publish {} event "
                                                + "for appointment {}",
                                        eventType,
                                        appointment.getId(),
                                        exception
                                );

                                return;
                            }

                            LOGGER.info(
                                    "Published {} event "
                                            + "for appointment {}",
                                    eventType,
                                    appointment.getId()
                            );
                        }
                );
    }*//*

    private void publishAppointmentEvent(
            Appointment appointment,
            NotificationType eventType,
            String title,
            String message
    ) {

        try {

            validateAppointmentForPublishing(
                    appointment
            );

            HealthcareEvent event =
                    buildAppointmentEvent(
                            appointment,
                            eventType,
                            title,
                            message
                    );

            String eventJson =
                    convertEventToJson(event);

            String messageKey =
                    appointment.getId().toString();

            kafkaTemplate
                    .send(
                            appointmentEventsTopic,
                            messageKey,
                            eventJson
                    )
                    .whenComplete(
                            (sendResult, exception) -> {

                                if (exception != null) {

                                    LOGGER.error(
                                            "Failed to publish {} event. "
                                                    + "Appointment ID: {}, "
                                                    + "Event ID: {}",
                                            eventType,
                                            appointment.getId(),
                                            event.getEventId(),
                                            exception
                                    );

                                    return;
                                }

                                LOGGER.info(
                                        "Published {} event. "
                                                + "Appointment ID: {}, "
                                                + "Event ID: {}, "
                                                + "Topic: {}, "
                                                + "Partition: {}, "
                                                + "Offset: {}",
                                        eventType,
                                        appointment.getId(),
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
                            + "Appointment ID: {}. "
                            + "The appointment operation remains successful.",
                    eventType,
                    appointment == null
                            ? null
                            : appointment.getId(),
                    exception
            );
        }
    }

    private HealthcareEvent buildAppointmentEvent(
            Appointment appointment,
            NotificationType eventType,
            String title,
            String message
    ) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "appointmentId",
                appointment.getId()
        );

        metadata.put(
                "doctorName",
                appointment.getDoctorName()
        );

        metadata.put(
                "departmentName",
                appointment.getDepartmentName()
        );

        metadata.put(
                "appointmentDate",
                appointment.getAppointmentDate()
        );

        metadata.put(
                "startTime",
                appointment.getStartTime()
        );

        metadata.put(
                "endTime",
                appointment.getEndTime()
        );

        metadata.put(
                "status",
                appointment.getStatus().name()
        );

        return HealthcareEvent.builder()
                .eventId(
                        UUID.randomUUID()
                )
                .eventType(
                        eventType
                )
                .occurredAt(
                        LocalDateTime.now()
                )
                .recipientEmail(
                        normalizeEmail(
                                appointment.getMemberEmail()
                        )
                )
                .recipientRole(
                        RecipientRole.MEMBER
                )
                .referenceType(
                        ReferenceType.APPOINTMENT
                )
                .referenceId(
                        appointment.getId()
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

    private String convertEventToJson(
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
                    "Unable to convert appointment "
                            + "event to JSON",
                    exception
            );
        }
    }

    private void validateAppointmentForPublishing(
            Appointment appointment
    ) {

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment is required"
            );
        }

        if (appointment.getId() == null) {
            throw new IllegalArgumentException(
                    "Appointment ID is required"
            );
        }

        if (appointment.getMemberEmail() == null
                || appointment.getMemberEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Member email is required"
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
}*/


package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.appointmentservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.appointmentservice.entity.Appointment;
import com.naveen.healthcare.appointmentservice.entity.OutboxEvent;
import com.naveen.healthcare.appointmentservice.enums.CancelledBy;
import com.naveen.healthcare.appointmentservice.enums.NotificationType;
import com.naveen.healthcare.appointmentservice.enums.OutboxEventStatus;
import com.naveen.healthcare.appointmentservice.enums.RecipientRole;
import com.naveen.healthcare.appointmentservice.enums.ReferenceType;
import com.naveen.healthcare.appointmentservice.repository.OutboxEventRepository;
import com.naveen.healthcare.appointmentservice.service.AppointmentEventPublisher;
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
public class AppointmentEventPublisherImpl
        implements AppointmentEventPublisher {

    private static final String AGGREGATE_TYPE =
            "APPOINTMENT";

    private final OutboxEventRepository
            outboxEventRepository;

    private final ObjectMapper objectMapper;

    @Value(
            "${appointment.kafka.topics.appointment-events}"
    )
    private String appointmentEventsTopic;

    @Override
    public void publishAppointmentBooked(
            Appointment appointment
    ) {

        validateAppointment(
                appointment
        );

        validateMemberRecipient(
                appointment
        );

        validateProviderRecipient(
                appointment
        );

        LocalDateTime occurredAt =
                resolveOccurredAt(
                        appointment.getCreatedAt()
                );

        HealthcareEvent memberEvent =
                buildEvent(
                        appointment,
                        NotificationType.APPOINTMENT_BOOKED,
                        RecipientRole.MEMBER,
                        appointment.getMemberEmail(),
                        "Appointment Booked",
                        buildMemberBookingMessage(
                                appointment
                        ),
                        occurredAt
                );

        HealthcareEvent providerEvent =
                buildEvent(
                        appointment,
                        NotificationType.APPOINTMENT_BOOKED,
                        RecipientRole.PROVIDER,
                        appointment.getProviderAdminEmail(),
                        "New Appointment Booked",
                        buildProviderBookingMessage(
                                appointment
                        ),
                        occurredAt
                );

        OutboxEvent memberOutboxEvent =
                buildOutboxEvent(
                        appointment,
                        memberEvent
                );

        OutboxEvent providerOutboxEvent =
                buildOutboxEvent(
                        appointment,
                        providerEvent
                );

        outboxEventRepository.saveAllAndFlush(
                List.of(
                        memberOutboxEvent,
                        providerOutboxEvent
                )
        );
    }

    @Override
    public void publishAppointmentConfirmed(
            Appointment appointment
    ) {

        validateAppointment(
                appointment
        );

        validateMemberRecipient(
                appointment
        );

        HealthcareEvent event =
                buildEvent(
                        appointment,
                        NotificationType.APPOINTMENT_CONFIRMED,
                        RecipientRole.MEMBER,
                        appointment.getMemberEmail(),
                        "Appointment Confirmed",
                        buildAppointmentConfirmedMessage(
                                appointment
                        ),
                        resolveOccurredAt(
                                appointment.getConfirmedAt()
                        )
                );

        saveEventToOutbox(
                appointment,
                event
        );
    }

    @Override
    public void publishAppointmentCompleted(
            Appointment appointment
    ) {

        validateAppointment(
                appointment
        );

        validateMemberRecipient(
                appointment
        );

        HealthcareEvent event =
                buildEvent(
                        appointment,
                        NotificationType.APPOINTMENT_COMPLETED,
                        RecipientRole.MEMBER,
                        appointment.getMemberEmail(),
                        "Appointment Completed",
                        buildAppointmentCompletedMessage(
                                appointment
                        ),
                        resolveOccurredAt(
                                appointment.getCompletedAt()
                        )
                );

        saveEventToOutbox(
                appointment,
                event
        );
    }

    @Override
    public void publishAppointmentCancelled(
            Appointment appointment
    ) {

        validateAppointment(
                appointment
        );

        validateMemberRecipient(
                appointment
        );

        if (appointment.getCancelledBy() == null) {

            throw new IllegalArgumentException(
                    "Appointment cancellation actor is required"
            );
        }

        HealthcareEvent memberEvent =
                buildEvent(
                        appointment,
                        NotificationType.APPOINTMENT_CANCELLED,
                        RecipientRole.MEMBER,
                        appointment.getMemberEmail(),
                        "Appointment Cancelled",
                        buildMemberCancellationMessage(
                                appointment
                        ),
                        resolveOccurredAt(
                                appointment.getCancelledAt()
                        )
                );

        OutboxEvent memberOutboxEvent =
                buildOutboxEvent(
                        appointment,
                        memberEvent
                );

        if (
                appointment.getCancelledBy()
                        == CancelledBy.MEMBER
        ) {

            validateProviderRecipient(
                    appointment
            );

            HealthcareEvent providerEvent =
                    buildEvent(
                            appointment,
                            NotificationType.APPOINTMENT_CANCELLED,
                            RecipientRole.PROVIDER,
                            appointment.getProviderAdminEmail(),
                            "Appointment Cancelled by Member",
                            buildProviderCancellationMessage(
                                    appointment
                            ),
                            resolveOccurredAt(
                                    appointment.getCancelledAt()
                            )
                    );

            OutboxEvent providerOutboxEvent =
                    buildOutboxEvent(
                            appointment,
                            providerEvent
                    );

            outboxEventRepository.saveAllAndFlush(
                    List.of(
                            memberOutboxEvent,
                            providerOutboxEvent
                    )
            );

            return;
        }

        outboxEventRepository.saveAndFlush(
                memberOutboxEvent
        );
    }

    @Override
    public void publishAppointmentNoShow(
            Appointment appointment
    ) {

        validateAppointment(
                appointment
        );

        validateMemberRecipient(
                appointment
        );

        HealthcareEvent event =
                buildEvent(
                        appointment,
                        NotificationType.APPOINTMENT_NO_SHOW,
                        RecipientRole.MEMBER,
                        appointment.getMemberEmail(),
                        "Appointment Marked as No-Show",
                        buildAppointmentNoShowMessage(
                                appointment
                        ),
                        resolveOccurredAt(
                                appointment.getNoShowAt()
                        )
                );

        saveEventToOutbox(
                appointment,
                event
        );
    }

    private void saveEventToOutbox(
            Appointment appointment,
            HealthcareEvent event
    ) {

        OutboxEvent outboxEvent =
                buildOutboxEvent(
                        appointment,
                        event
                );

        outboxEventRepository.saveAndFlush(
                outboxEvent
        );
    }

    private HealthcareEvent buildEvent(
            Appointment appointment,
            NotificationType notificationType,
            RecipientRole recipientRole,
            String recipientEmail,
            String title,
            String message,
            LocalDateTime occurredAt
    ) {

        if (notificationType == null) {

            throw new IllegalArgumentException(
                    "Notification type is required"
            );
        }

        if (recipientRole == null) {

            throw new IllegalArgumentException(
                    "Notification recipient role is required"
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
                        resolveOccurredAt(
                                occurredAt
                        )
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
                        ReferenceType.APPOINTMENT
                )
                .referenceId(
                        appointment.getId()
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
                                appointment
                        )
                )
                .build();
    }

    private OutboxEvent buildOutboxEvent(
            Appointment appointment,
            HealthcareEvent event
    ) {

        validateHealthcareEvent(
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
                        appointment.getId()
                )
                .eventType(
                        event.getEventType().name()
                )
                .topicName(
                        normalizeRequiredText(
                                appointmentEventsTopic,
                                "Appointment Kafka topic"
                        )
                )
                .messageKey(
                        appointment.getId().toString()
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
            Appointment appointment
    ) {

        Map<String, Object> metadata =
                new LinkedHashMap<>();

        metadata.put(
                "appointmentId",
                appointment.getId().toString()
        );

        metadata.put(
                "memberId",
                appointment.getMemberId().toString()
        );

        metadata.put(
                "memberEmail",
                appointment.getMemberEmail()
        );

        metadata.put(
                "providerId",
                appointment.getProviderId().toString()
        );

        metadata.put(
                "providerOrganizationName",
                appointment
                        .getProviderOrganizationName()
        );

        metadata.put(
                "departmentId",
                appointment.getDepartmentId().toString()
        );

        metadata.put(
                "departmentName",
                appointment.getDepartmentName()
        );

        metadata.put(
                "doctorId",
                appointment.getDoctorId().toString()
        );

        metadata.put(
                "doctorName",
                appointment.getDoctorName()
        );

        metadata.put(
                "doctorSpecialization",
                appointment.getDoctorSpecialization()
        );

        metadata.put(
                "appointmentDate",
                appointment.getAppointmentDate().toString()
        );

        metadata.put(
                "startTime",
                appointment.getStartTime().toString()
        );

        metadata.put(
                "endTime",
                appointment.getEndTime().toString()
        );

        metadata.put(
                "status",
                appointment.getStatus().name()
        );

        if (appointment.getCancelledBy() != null) {

            metadata.put(
                    "cancelledBy",
                    appointment.getCancelledBy().name()
            );
        }

        if (
                appointment.getCancellationReason() != null
                        && !appointment
                        .getCancellationReason()
                        .isBlank()
        ) {

            metadata.put(
                    "cancellationReason",
                    appointment
                            .getCancellationReason()
                            .trim()
            );
        }

        return metadata;
    }

    private String buildMemberBookingMessage(
            Appointment appointment
    ) {

        return "Your appointment with "
                + appointment.getDoctorName()
                + " at "
                + appointment.getProviderOrganizationName()
                + " on "
                + appointment.getAppointmentDate()
                + " from "
                + appointment.getStartTime()
                + " to "
                + appointment.getEndTime()
                + " has been booked successfully.";
    }

    private String buildProviderBookingMessage(
            Appointment appointment
    ) {

        return "A new appointment was booked by "
                + appointment.getMemberEmail()
                + " with "
                + appointment.getDoctorName()
                + " in "
                + appointment.getDepartmentName()
                + " on "
                + appointment.getAppointmentDate()
                + " from "
                + appointment.getStartTime()
                + " to "
                + appointment.getEndTime()
                + ".";
    }

    private String buildAppointmentConfirmedMessage(
            Appointment appointment
    ) {

        return "Your appointment with "
                + appointment.getDoctorName()
                + " at "
                + appointment.getProviderOrganizationName()
                + " on "
                + appointment.getAppointmentDate()
                + " at "
                + appointment.getStartTime()
                + " has been confirmed.";
    }

    private String buildAppointmentCompletedMessage(
            Appointment appointment
    ) {

        return "Your appointment with "
                + appointment.getDoctorName()
                + " at "
                + appointment.getProviderOrganizationName()
                + " on "
                + appointment.getAppointmentDate()
                + " has been completed.";
    }

    private String buildMemberCancellationMessage(
            Appointment appointment
    ) {

        String message;

        if (
                appointment.getCancelledBy()
                        == CancelledBy.PROVIDER
        ) {

            message =
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " at "
                            + appointment
                            .getProviderOrganizationName()
                            + " on "
                            + appointment.getAppointmentDate()
                            + " at "
                            + appointment.getStartTime()
                            + " was cancelled by the Provider.";

        } else {

            message =
                    "Your appointment with "
                            + appointment.getDoctorName()
                            + " on "
                            + appointment.getAppointmentDate()
                            + " at "
                            + appointment.getStartTime()
                            + " was cancelled successfully.";
        }

        return appendCancellationReason(
                message,
                appointment.getCancellationReason()
        );
    }

    private String buildProviderCancellationMessage(
            Appointment appointment
    ) {

        String message =
                "The appointment booked by "
                        + appointment.getMemberEmail()
                        + " with "
                        + appointment.getDoctorName()
                        + " on "
                        + appointment.getAppointmentDate()
                        + " at "
                        + appointment.getStartTime()
                        + " was cancelled by the Member.";

        return appendCancellationReason(
                message,
                appointment.getCancellationReason()
        );
    }

    private String buildAppointmentNoShowMessage(
            Appointment appointment
    ) {

        return "Your appointment with "
                + appointment.getDoctorName()
                + " at "
                + appointment.getProviderOrganizationName()
                + " on "
                + appointment.getAppointmentDate()
                + " at "
                + appointment.getStartTime()
                + " was marked as no-show.";
    }

    private String appendCancellationReason(
            String message,
            String cancellationReason
    ) {

        if (
                cancellationReason == null
                        || cancellationReason.isBlank()
        ) {

            return message;
        }

        return message
                + " Reason: "
                + cancellationReason.trim();
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
                    "Unable to serialize Appointment "
                            + "notification event",
                    exception
            );
        }
    }

    private void validateHealthcareEvent(
            HealthcareEvent event
    ) {

        if (event == null) {

            throw new IllegalArgumentException(
                    "Healthcare event is required"
            );
        }

        if (event.getEventId() == null) {

            throw new IllegalArgumentException(
                    "Healthcare event ID is required"
            );
        }

        if (event.getEventType() == null) {

            throw new IllegalArgumentException(
                    "Healthcare event type is required"
            );
        }

        if (event.getRecipientRole() == null) {

            throw new IllegalArgumentException(
                    "Healthcare event recipient role is required"
            );
        }

        if (event.getReferenceType() == null) {

            throw new IllegalArgumentException(
                    "Healthcare event reference type is required"
            );
        }

        if (event.getReferenceId() == null) {

            throw new IllegalArgumentException(
                    "Healthcare event reference ID is required"
            );
        }

        normalizeEmail(
                event.getRecipientEmail()
        );

        normalizeRequiredText(
                event.getTitle(),
                "Healthcare event title"
        );

        normalizeRequiredText(
                event.getMessage(),
                "Healthcare event message"
        );
    }

    private void validateAppointment(
            Appointment appointment
    ) {

        if (appointment == null) {

            throw new IllegalArgumentException(
                    "Appointment is required"
            );
        }

        if (appointment.getId() == null) {

            throw new IllegalArgumentException(
                    "Appointment ID is required"
            );
        }

        if (appointment.getMemberId() == null) {

            throw new IllegalArgumentException(
                    "Appointment Member ID is required"
            );
        }

        if (appointment.getProviderId() == null) {

            throw new IllegalArgumentException(
                    "Appointment Provider ID is required"
            );
        }

        if (appointment.getDepartmentId() == null) {

            throw new IllegalArgumentException(
                    "Appointment Department ID is required"
            );
        }

        if (appointment.getDoctorId() == null) {

            throw new IllegalArgumentException(
                    "Appointment Doctor ID is required"
            );
        }

        if (appointment.getStatus() == null) {

            throw new IllegalArgumentException(
                    "Appointment status is required"
            );
        }

        if (appointment.getAppointmentDate() == null) {

            throw new IllegalArgumentException(
                    "Appointment date is required"
            );
        }

        if (appointment.getStartTime() == null) {

            throw new IllegalArgumentException(
                    "Appointment start time is required"
            );
        }

        if (appointment.getEndTime() == null) {

            throw new IllegalArgumentException(
                    "Appointment end time is required"
            );
        }

        normalizeRequiredText(
                appointment.getProviderOrganizationName(),
                "Provider organization name"
        );

        normalizeRequiredText(
                appointment.getDepartmentName(),
                "Department name"
        );

        normalizeRequiredText(
                appointment.getDoctorName(),
                "Doctor name"
        );

        normalizeRequiredText(
                appointment.getDoctorSpecialization(),
                "Doctor specialization"
        );
    }

    private void validateMemberRecipient(
            Appointment appointment
    ) {

        normalizeEmail(
                appointment.getMemberEmail()
        );
    }

    private void validateProviderRecipient(
            Appointment appointment
    ) {

        normalizeEmail(
                appointment.getProviderAdminEmail()
        );
    }

    private LocalDateTime resolveOccurredAt(
            LocalDateTime eventTimestamp
    ) {

        if (eventTimestamp != null) {
            return eventTimestamp;
        }

        return LocalDateTime.now();
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
}