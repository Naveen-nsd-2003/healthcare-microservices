package com.naveen.healthcare.appointmentservice.entity;

import com.naveen.healthcare.appointmentservice.enums.OutboxEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_status_next_attempt",
                        columnList = "status,next_attempt_at"
                ),
                @Index(
                        name = "idx_outbox_aggregate",
                        columnList = "aggregate_type,aggregate_id"
                ),
                @Index(
                        name = "idx_outbox_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_outbox_processing_started_at",
                        columnList = "processing_started_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private UUID id;

    @Column(
            name = "event_id",
            nullable = false,
            updatable = false,
            unique = true
    )
    private UUID eventId;

    @Column(
            name = "aggregate_type",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String aggregateType;

    @Column(
            name = "aggregate_id",
            nullable = false,
            updatable = false
    )
    private UUID aggregateId;

    @Column(
            name = "event_type",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String eventType;

    @Column(
            name = "topic_name",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String topicName;

    @Column(
            name = "message_key",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String messageKey;

    @Lob
    @Column(
            name = "payload",
            nullable = false,
            updatable = false,
            columnDefinition = "LONGTEXT"
    )
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private OutboxEventStatus status =
            OutboxEventStatus.PENDING;

    @Column(
            name = "retry_count",
            nullable = false
    )
    @Builder.Default
    private Integer retryCount = 0;

    @Column(
            name = "next_attempt_at",
            nullable = false
    )
    private LocalDateTime nextAttemptAt;

    @Column(
            name = "processing_started_at"
    )
    private LocalDateTime processingStartedAt;

    @Column(
            name = "last_attempt_at"
    )
    private LocalDateTime lastAttemptAt;

    @Column(
            name = "published_at"
    )
    private LocalDateTime publishedAt;

    @Column(
            name = "last_error",
            length = 1000
    )
    private String lastError;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    private Long version;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        if (id == null) {
            id = UUID.randomUUID();
        }

        if (eventId == null) {
            eventId = UUID.randomUUID();
        }

        if (status == null) {
            status = OutboxEventStatus.PENDING;
        }

        if (retryCount == null) {
            retryCount = 0;
        }

        if (nextAttemptAt == null) {
            nextAttemptAt = now;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}