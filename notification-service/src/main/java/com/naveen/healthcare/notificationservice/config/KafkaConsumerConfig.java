package com.naveen.healthcare.notificationservice.config;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final long RETRY_INTERVAL_MILLISECONDS =
            1000L;

    private static final long MAXIMUM_RETRY_ATTEMPTS =
            2L;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        FixedBackOff fixedBackOff =
                new FixedBackOff(
                        RETRY_INTERVAL_MILLISECONDS,
                        MAXIMUM_RETRY_ATTEMPTS
                );

        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        (ConsumerRecord<?, ?> record,
                         Exception exception) -> {

                            System.err.println(
                                    "Kafka event processing failed"
                            );

                            System.err.println(
                                    "Topic: "
                                            + record.topic()
                            );

                            System.err.println(
                                    "Partition: "
                                            + record.partition()
                            );

                            System.err.println(
                                    "Offset: "
                                            + record.offset()
                            );

                            System.err.println(
                                    "Reason: "
                                            + exception.getMessage()
                            );
                        },
                        fixedBackOff
                );

        errorHandler.setCommitRecovered(true);

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
}
