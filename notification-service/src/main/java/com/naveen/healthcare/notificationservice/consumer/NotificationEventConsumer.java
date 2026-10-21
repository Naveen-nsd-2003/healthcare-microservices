package com.naveen.healthcare.notificationservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.notificationservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.notificationservice.exception.InvalidNotificationEventException;
import com.naveen.healthcare.notificationservice.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    NotificationEventConsumer.class
            );

    private final ObjectMapper objectMapper;

    private final NotificationEventService
            notificationEventService;

    @KafkaListener(
            topics = {
                    "${notification.kafka.topics.appointment-events}",
                    "${notification.kafka.topics.claim-events}",
                    "${notification.kafka.topics.insurance-events}",
                    "${notification.kafka.topics.medical-record-events}",
                    "${notification.kafka.topics.organization-events}"
            },
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory =
                    "kafkaListenerContainerFactory"
    )
    public void consumeEvent(
            ConsumerRecord<String, String> consumerRecord
    ) {

        validateConsumerRecord(
                consumerRecord
        );

        HealthcareEvent healthcareEvent =
                convertEvent(
                        consumerRecord.value()
                );

        LOGGER.debug(
                "Processing notification event. "
                        + "Topic: {}, partition: {}, "
                        + "offset: {}, event ID: {}, "
                        + "event type: {}, recipient role: {}",
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                healthcareEvent.getEventId(),
                healthcareEvent.getEventType(),
                healthcareEvent.getRecipientRole()
        );

        notificationEventService.processEvent(
                healthcareEvent
        );

        LOGGER.debug(
                "Notification event processed successfully. "
                        + "Topic: {}, partition: {}, "
                        + "offset: {}, event ID: {}",
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset(),
                healthcareEvent.getEventId()
        );
    }

    private HealthcareEvent convertEvent(
            String eventJson
    ) {

        try {

            return objectMapper.readValue(
                    eventJson,
                    HealthcareEvent.class
            );

        } catch (JsonProcessingException exception) {

            throw new InvalidNotificationEventException(
                    "Kafka event contains invalid JSON "
                            + "or unsupported event values",
                    exception
            );
        }
    }

    private void validateConsumerRecord(
            ConsumerRecord<String, String> consumerRecord
    ) {

        if (consumerRecord == null) {

            throw new InvalidNotificationEventException(
                    "Kafka consumer record is required"
            );
        }

        String eventJson =
                consumerRecord.value();

        if (
                eventJson == null
                        || eventJson.isBlank()
        ) {

            throw new InvalidNotificationEventException(
                    "Kafka event payload is required"
            );
        }
    }
}