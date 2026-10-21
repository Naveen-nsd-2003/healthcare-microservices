package com.naveen.healthcare.notificationservice.config;

import com.naveen.healthcare.notificationservice.exception.InvalidNotificationEventException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    KafkaConsumerConfig.class
            );

    private static final long
            RETRY_INTERVAL_MILLISECONDS =
            1000L;

    private static final long
            MAXIMUM_RETRY_ATTEMPTS =
            2L;

    @Value(
            "${notification.kafka.topics.appointment-events}"
    )
    private String appointmentEventsTopic;

    @Value(
            "${notification.kafka.topics.claim-events}"
    )
    private String claimEventsTopic;

    @Value(
            "${notification.kafka.topics.insurance-events}"
    )
    private String insuranceEventsTopic;

    @Value(
            "${notification.kafka.topics.medical-record-events}"
    )
    private String medicalRecordEventsTopic;

    @Value(
            "${notification.kafka.topics.organization-events}"
    )
    private String organizationEventsTopic;

    @Value(
            "${notification.kafka.dead-letter-topics.appointment-events}"
    )
    private String appointmentEventsDeadLetterTopic;

    @Value(
            "${notification.kafka.dead-letter-topics.claim-events}"
    )
    private String claimEventsDeadLetterTopic;

    @Value(
            "${notification.kafka.dead-letter-topics.insurance-events}"
    )
    private String insuranceEventsDeadLetterTopic;

    @Value(
            "${notification.kafka.dead-letter-topics.medical-record-events}"
    )
    private String medicalRecordEventsDeadLetterTopic;

    @Value(
            "${notification.kafka.dead-letter-topics.organization-events}"
    )
    private String organizationEventsDeadLetterTopic;

    @Bean
    public DeadLetterPublishingRecoverer
    deadLetterPublishingRecoverer(
            KafkaTemplate<String, String> kafkaTemplate
    ) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (
                                consumerRecord,
                                exception
                        ) ->
                                resolveDeadLetterTopicPartition(
                                        consumerRecord
                                )
                );

        recoverer.setFailIfSendResultIsError(
                true
        );

        return recoverer;
    }

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer recoverer
    ) {

        FixedBackOff fixedBackOff =
                new FixedBackOff(
                        RETRY_INTERVAL_MILLISECONDS,
                        MAXIMUM_RETRY_ATTEMPTS
                );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        (
                                record,
                                exception
                        ) -> {

                            logDeadLetterRecovery(
                                    record,
                                    exception
                            );

                            recoverer.accept(
                                    record,
                                    exception
                            );
                        },
                        fixedBackOff
                );

        errorHandler.addNotRetryableExceptions(
                InvalidNotificationEventException.class
        );

        errorHandler.setCommitRecovered(
                true
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<
            String,
            String
            > kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {

        ConcurrentKafkaListenerContainerFactory<
                String,
                String
                > factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(
                consumerFactory
        );

        factory.setCommonErrorHandler(
                kafkaErrorHandler
        );

        return factory;
    }

    private TopicPartition
    resolveDeadLetterTopicPartition(
            ConsumerRecord<?, ?> consumerRecord
    ) {

        if (consumerRecord == null) {

            throw new IllegalArgumentException(
                    "Kafka consumer record is required "
                            + "to resolve the dead-letter topic"
            );
        }

        String sourceTopic =
                consumerRecord.topic();

        String deadLetterTopic =
                resolveDeadLetterTopic(
                        sourceTopic
                );

        return new TopicPartition(
                deadLetterTopic,
                consumerRecord.partition()
        );
    }

    private String resolveDeadLetterTopic(
            String sourceTopic
    ) {

        if (
                appointmentEventsTopic.equals(
                        sourceTopic
                )
        ) {

            return appointmentEventsDeadLetterTopic;
        }

        if (
                claimEventsTopic.equals(
                        sourceTopic
                )
        ) {

            return claimEventsDeadLetterTopic;
        }

        if (
                insuranceEventsTopic.equals(
                        sourceTopic
                )
        ) {

            return insuranceEventsDeadLetterTopic;
        }

        if (
                medicalRecordEventsTopic.equals(
                        sourceTopic
                )
        ) {

            return medicalRecordEventsDeadLetterTopic;
        }

        if (
                organizationEventsTopic.equals(
                        sourceTopic
                )
        ) {

            return organizationEventsDeadLetterTopic;
        }

        return sourceTopic + ".DLT";
    }

    private void logDeadLetterRecovery(
            ConsumerRecord<?, ?> record,
            Exception exception
    ) {

        LOGGER.error(
                "Kafka event processing failed after retries. "
                        + "Publishing record to the dead-letter topic. "
                        + "Topic: {}, partition: {}, offset: {}, "
                        + "key: {}, reason: {}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                resolveExceptionMessage(
                        exception
                )
        );
    }

    private String resolveExceptionMessage(
            Exception exception
    ) {

        if (exception == null) {

            return "Unknown Kafka processing failure";
        }

        Throwable cause =
                exception.getCause();

        if (
                cause != null
                        && cause.getMessage() != null
                        && !cause.getMessage().isBlank()
        ) {

            return cause
                    .getClass()
                    .getSimpleName()
                    + ": "
                    + cause.getMessage().trim();
        }

        if (
                exception.getMessage() != null
                        && !exception.getMessage().isBlank()
        ) {

            return exception
                    .getClass()
                    .getSimpleName()
                    + ": "
                    + exception.getMessage().trim();
        }

        return exception
                .getClass()
                .getSimpleName();
    }
}