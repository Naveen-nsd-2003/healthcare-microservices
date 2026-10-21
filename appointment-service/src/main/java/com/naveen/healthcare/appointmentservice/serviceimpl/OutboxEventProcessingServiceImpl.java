package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.naveen.healthcare.appointmentservice.entity.OutboxEvent;
import com.naveen.healthcare.appointmentservice.enums.OutboxEventStatus;
import com.naveen.healthcare.appointmentservice.repository.OutboxEventRepository;
import com.naveen.healthcare.appointmentservice.service.OutboxEventProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventProcessingServiceImpl
        implements OutboxEventProcessingService {

    private static final int MAXIMUM_ERROR_LENGTH =
            1000;

    private final OutboxEventRepository
            outboxEventRepository;

    @Override
    @Transactional
    public List<OutboxEvent> claimPendingEvents(
            int batchSize
    ) {

        validateBatchSize(
                batchSize
        );

        LocalDateTime now =
                LocalDateTime.now();

        List<OutboxEvent> events =
                outboxEventRepository
                        .findEligibleEventsForProcessing(
                                OutboxEventStatus.PENDING,
                                now,
                                PageRequest.of(
                                        0,
                                        batchSize
                                )
                        );

        for (OutboxEvent event : events) {

            event.setStatus(
                    OutboxEventStatus.PROCESSING
            );

            event.setProcessingStartedAt(
                    now
            );

            event.setLastAttemptAt(
                    now
            );

            event.setLastError(
                    null
            );
        }

        if (!events.isEmpty()) {

            outboxEventRepository.saveAllAndFlush(
                    events
            );
        }

        return List.copyOf(
                events
        );
    }

    @Override
    @Transactional
    public void markEventAsPublished(
            UUID outboxEventId
    ) {

        OutboxEvent event =
                findOutboxEvent(
                        outboxEventId
                );

        if (event.getStatus()
                == OutboxEventStatus.PUBLISHED) {

            return;
        }

        if (event.getStatus()
                != OutboxEventStatus.PROCESSING) {

            throw new IllegalStateException(
                    "Only a PROCESSING outbox event "
                            + "can be marked as PUBLISHED"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        event.setStatus(
                OutboxEventStatus.PUBLISHED
        );

        event.setPublishedAt(
                now
        );

        event.setProcessingStartedAt(
                null
        );

        event.setLastAttemptAt(
                now
        );

        event.setLastError(
                null
        );

        outboxEventRepository.saveAndFlush(
                event
        );
    }

    @Override
    @Transactional
    public void rescheduleFailedEvent(
            UUID outboxEventId,
            String failureMessage,
            int maximumRetries,
            Duration initialRetryDelay,
            Duration maximumRetryDelay
    ) {

        validateRetryConfiguration(
                maximumRetries,
                initialRetryDelay,
                maximumRetryDelay
        );

        OutboxEvent event =
                findOutboxEvent(
                        outboxEventId
                );

        if (event.getStatus()
                == OutboxEventStatus.PUBLISHED) {

            return;
        }

        int updatedRetryCount =
                safeRetryCount(
                        event
                ) + 1;

        LocalDateTime now =
                LocalDateTime.now();

        event.setRetryCount(
                updatedRetryCount
        );

        event.setLastAttemptAt(
                now
        );

        event.setProcessingStartedAt(
                null
        );

        event.setPublishedAt(
                null
        );

        event.setLastError(
                normalizeFailureMessage(
                        failureMessage
                )
        );

        if (updatedRetryCount
                >= maximumRetries) {

            event.setStatus(
                    OutboxEventStatus.FAILED
            );

            event.setNextAttemptAt(
                    now
            );

        } else {

            Duration retryDelay =
                    calculateRetryDelay(
                            updatedRetryCount,
                            initialRetryDelay,
                            maximumRetryDelay
                    );

            event.setStatus(
                    OutboxEventStatus.PENDING
            );

            event.setNextAttemptAt(
                    now.plus(
                            retryDelay
                    )
            );
        }

        outboxEventRepository.saveAndFlush(
                event
        );
    }

    @Override
    @Transactional
    public int recoverStaleProcessingEvents(
            int batchSize,
            Duration processingTimeout,
            int maximumRetries,
            Duration initialRetryDelay,
            Duration maximumRetryDelay
    ) {

        validateBatchSize(
                batchSize
        );

        validateProcessingTimeout(
                processingTimeout
        );

        validateRetryConfiguration(
                maximumRetries,
                initialRetryDelay,
                maximumRetryDelay
        );

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime staleBefore =
                now.minus(
                        processingTimeout
                );

        List<OutboxEvent> staleEvents =
                outboxEventRepository
                        .findStaleProcessingEvents(
                                OutboxEventStatus.PROCESSING,
                                staleBefore,
                                PageRequest.of(
                                        0,
                                        batchSize
                                )
                        );

        for (OutboxEvent event : staleEvents) {

            int updatedRetryCount =
                    safeRetryCount(
                            event
                    ) + 1;

            event.setRetryCount(
                    updatedRetryCount
            );

            event.setProcessingStartedAt(
                    null
            );

            event.setLastAttemptAt(
                    now
            );

            event.setLastError(
                    "Recovered stale PROCESSING outbox event"
            );

            if (updatedRetryCount
                    >= maximumRetries) {

                event.setStatus(
                        OutboxEventStatus.FAILED
                );

                event.setNextAttemptAt(
                        now
                );

            } else {

                Duration retryDelay =
                        calculateRetryDelay(
                                updatedRetryCount,
                                initialRetryDelay,
                                maximumRetryDelay
                        );

                event.setStatus(
                        OutboxEventStatus.PENDING
                );

                event.setNextAttemptAt(
                        now.plus(
                                retryDelay
                        )
                );
            }
        }

        if (!staleEvents.isEmpty()) {

            outboxEventRepository.saveAllAndFlush(
                    staleEvents
            );
        }

        return staleEvents.size();
    }

    private OutboxEvent findOutboxEvent(
            UUID outboxEventId
    ) {

        if (outboxEventId == null) {

            throw new IllegalArgumentException(
                    "Outbox event ID is required"
            );
        }

        return outboxEventRepository
                .findById(
                        outboxEventId
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Outbox event could not be found"
                        )
                );
    }

    private int safeRetryCount(
            OutboxEvent event
    ) {

        if (event.getRetryCount() == null) {
            return 0;
        }

        return Math.max(
                0,
                event.getRetryCount()
        );
    }

    private Duration calculateRetryDelay(
            int retryCount,
            Duration initialRetryDelay,
            Duration maximumRetryDelay
    ) {

        long initialDelaySeconds =
                initialRetryDelay.getSeconds();

        long maximumDelaySeconds =
                maximumRetryDelay.getSeconds();

        long calculatedDelay =
                initialDelaySeconds;

        int exponent =
                Math.max(
                        0,
                        retryCount - 1
                );

        for (int index = 0;
             index < exponent;
             index++) {

            if (calculatedDelay
                    >= maximumDelaySeconds) {

                return Duration.ofSeconds(
                        maximumDelaySeconds
                );
            }

            if (calculatedDelay
                    > Long.MAX_VALUE / 2) {

                calculatedDelay =
                        maximumDelaySeconds;

                break;
            }

            calculatedDelay =
                    calculatedDelay * 2;
        }

        long boundedDelay =
                Math.min(
                        calculatedDelay,
                        maximumDelaySeconds
                );

        return Duration.ofSeconds(
                boundedDelay
        );
    }

    private String normalizeFailureMessage(
            String failureMessage
    ) {

        String normalizedMessage;

        if (failureMessage == null
                || failureMessage.isBlank()) {

            normalizedMessage =
                    "Kafka event publishing failed";

        } else {

            normalizedMessage =
                    failureMessage.trim();
        }

        if (normalizedMessage.length()
                <= MAXIMUM_ERROR_LENGTH) {

            return normalizedMessage;
        }

        return normalizedMessage.substring(
                0,
                MAXIMUM_ERROR_LENGTH
        );
    }

    private void validateBatchSize(
            int batchSize
    ) {

        if (batchSize <= 0) {

            throw new IllegalArgumentException(
                    "Outbox batch size must be greater than zero"
            );
        }
    }

    private void validateProcessingTimeout(
            Duration processingTimeout
    ) {

        if (processingTimeout == null
                || processingTimeout.isZero()
                || processingTimeout.isNegative()) {

            throw new IllegalArgumentException(
                    "Outbox processing timeout must be positive"
            );
        }
    }

    private void validateRetryConfiguration(
            int maximumRetries,
            Duration initialRetryDelay,
            Duration maximumRetryDelay
    ) {

        if (maximumRetries <= 0) {

            throw new IllegalArgumentException(
                    "Maximum outbox retries must be greater than zero"
            );
        }

        if (initialRetryDelay == null
                || initialRetryDelay.isZero()
                || initialRetryDelay.isNegative()) {

            throw new IllegalArgumentException(
                    "Initial outbox retry delay must be positive"
            );
        }

        if (maximumRetryDelay == null
                || maximumRetryDelay.isZero()
                || maximumRetryDelay.isNegative()) {

            throw new IllegalArgumentException(
                    "Maximum outbox retry delay must be positive"
            );
        }

        if (maximumRetryDelay.compareTo(
                initialRetryDelay
        ) < 0) {

            throw new IllegalArgumentException(
                    "Maximum outbox retry delay cannot "
                            + "be smaller than the initial delay"
            );
        }
    }
}