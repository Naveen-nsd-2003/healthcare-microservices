package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.entity.OutboxEvent;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface OutboxEventProcessingService {

    List<OutboxEvent> claimPendingEvents(
            int batchSize
    );

    void markEventAsPublished(
            UUID outboxEventId
    );

    void rescheduleFailedEvent(
            UUID outboxEventId,
            String failureMessage,
            int maximumRetries,
            Duration initialRetryDelay,
            Duration maximumRetryDelay
    );

    int recoverStaleProcessingEvents(
            int batchSize,
            Duration processingTimeout,
            int maximumRetries,
            Duration initialRetryDelay,
            Duration maximumRetryDelay
    );
}