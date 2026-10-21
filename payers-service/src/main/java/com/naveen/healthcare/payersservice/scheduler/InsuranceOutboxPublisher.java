package com.naveen.healthcare.payersservice.scheduler;

import com.naveen.healthcare.payersservice.entity.OutboxEvent;
import com.naveen.healthcare.payersservice.service.OutboxEventProcessingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class InsuranceOutboxPublisher {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    InsuranceOutboxPublisher.class
            );

    private final KafkaTemplate<String, String>
            kafkaTemplate;

    private final OutboxEventProcessingService
            outboxEventProcessingService;

    @Value(
            "${insurance.outbox.publisher.batch-size:50}"
    )
    private int batchSize;

    @Value(
            "${insurance.outbox.publisher.maximum-retries:10}"
    )
    private int maximumRetries;

    @Value(
            "${insurance.outbox.publisher.initial-retry-delay-seconds:5}"
    )
    private long initialRetryDelaySeconds;

    @Value(
            "${insurance.outbox.publisher.maximum-retry-delay-seconds:3600}"
    )
    private long maximumRetryDelaySeconds;

    @Value(
            "${insurance.outbox.publisher.processing-timeout-seconds:300}"
    )
    private long processingTimeoutSeconds;

    @Value(
            "${insurance.outbox.publisher.send-timeout-seconds:30}"
    )
    private long sendTimeoutSeconds;

    @Scheduled(
            fixedDelayString =
                    "${insurance.outbox.publisher.fixed-delay-ms:2000}"
    )
    public void publishPendingEvents() {

        recoverStaleEvents();

        List<OutboxEvent> events =
                outboxEventProcessingService
                        .claimPendingEvents(
                                batchSize
                        );

        for (OutboxEvent event : events) {

            publishEvent(
                    event
            );
        }
    }

    private void publishEvent(
            OutboxEvent event
    ) {

        try {

            SendResult<String, String> sendResult =
                    kafkaTemplate
                            .send(
                                    event.getTopicName(),
                                    event.getMessageKey(),
                                    event.getPayload()
                            )
                            .get(
                                    sendTimeoutSeconds,
                                    TimeUnit.SECONDS
                            );

            outboxEventProcessingService
                    .markEventAsPublished(
                            event.getId()
                    );

            RecordMetadata metadata =
                    sendResult.getRecordMetadata();

            LOGGER.info(
                    "Published insurance outbox event. "
                            + "Outbox ID: {}, event ID: {}, "
                            + "event type: {}, aggregate ID: {}, "
                            + "topic: {}, partition: {}, offset: {}",
                    event.getId(),
                    event.getEventId(),
                    event.getEventType(),
                    event.getAggregateId(),
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset()
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            handlePublishingFailure(
                    event,
                    "Insurance outbox publishing was interrupted"
            );

        } catch (Exception exception) {

            handlePublishingFailure(
                    event,
                    resolveFailureMessage(
                            exception
                    )
            );
        }
    }

    private void handlePublishingFailure(
            OutboxEvent event,
            String failureMessage
    ) {

        try {

            outboxEventProcessingService
                    .rescheduleFailedEvent(
                            event.getId(),
                            failureMessage,
                            maximumRetries,
                            Duration.ofSeconds(
                                    initialRetryDelaySeconds
                            ),
                            Duration.ofSeconds(
                                    maximumRetryDelaySeconds
                            )
                    );

        } catch (RuntimeException stateException) {

            LOGGER.error(
                    "Unable to reschedule insurance outbox event. "
                            + "Outbox ID: {}, event ID: {}",
                    event.getId(),
                    event.getEventId(),
                    stateException
            );

            return;
        }

        LOGGER.warn(
                "Insurance outbox publishing failed. "
                        + "Outbox ID: {}, event ID: {}, "
                        + "event type: {}, reason: {}",
                event.getId(),
                event.getEventId(),
                event.getEventType(),
                failureMessage
        );
    }

    private void recoverStaleEvents() {

        try {

            int recoveredCount =
                    outboxEventProcessingService
                            .recoverStaleProcessingEvents(
                                    batchSize,
                                    Duration.ofSeconds(
                                            processingTimeoutSeconds
                                    ),
                                    maximumRetries,
                                    Duration.ofSeconds(
                                            initialRetryDelaySeconds
                                    ),
                                    Duration.ofSeconds(
                                            maximumRetryDelaySeconds
                                    )
                            );

            if (recoveredCount > 0) {

                LOGGER.warn(
                        "Recovered {} stale insurance "
                                + "outbox event(s)",
                        recoveredCount
                );
            }

        } catch (RuntimeException exception) {

            LOGGER.error(
                    "Unable to recover stale insurance "
                            + "outbox events",
                    exception
            );
        }
    }

    private String resolveFailureMessage(
            Exception exception
    ) {

        if (exception == null) {
            return "Kafka publishing failed";
        }

        Throwable cause =
                exception.getCause();

        if (cause != null
                && cause.getMessage() != null
                && !cause.getMessage().isBlank()) {

            return cause.getClass().getSimpleName()
                    + ": "
                    + cause.getMessage().trim();
        }

        if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {

            return exception.getClass().getSimpleName()
                    + ": "
                    + exception.getMessage().trim();
        }

        return exception
                .getClass()
                .getSimpleName();
    }
}