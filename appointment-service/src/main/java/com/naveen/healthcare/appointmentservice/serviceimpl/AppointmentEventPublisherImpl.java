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
    }*/
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
}