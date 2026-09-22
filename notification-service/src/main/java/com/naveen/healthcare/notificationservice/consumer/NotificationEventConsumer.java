package com.naveen.healthcare.notificationservice.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naveen.healthcare.notificationservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.notificationservice.exception.InvalidNotificationEventException;
import com.naveen.healthcare.notificationservice.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;

    private final NotificationEventService
            notificationEventService;

    @KafkaListener(
            topics = {
                    "${notification.kafka.topics.appointment-events}",
                    "${notification.kafka.topics.claim-events}",
                    "${notification.kafka.topics.insurance-events}"
            },
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEvent(
            ConsumerRecord<String, String> consumerRecord
    ) {

        if (consumerRecord == null) {
            throw new InvalidNotificationEventException(
                    "Kafka consumer record is required"
            );
        }

        String eventJson =
                consumerRecord.value();

        if (eventJson == null
                || eventJson.isBlank()) {

            throw new InvalidNotificationEventException(
                    "Kafka event payload is required"
            );
        }

        HealthcareEvent healthcareEvent =
                convertEvent(eventJson);

        notificationEventService.processEvent(
                healthcareEvent
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
}