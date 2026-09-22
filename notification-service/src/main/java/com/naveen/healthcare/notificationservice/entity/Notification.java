package com.naveen.healthcare.notificationservice.entity;

import com.naveen.healthcare.notificationservice.enums.NotificationType;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import com.naveen.healthcare.notificationservice.enums.ReferenceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_event_id",
                        columnNames = "event_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_notification_recipient_email",
                        columnList = "recipient_email"
                ),
                @Index(
                        name = "idx_notification_recipient_role",
                        columnList = "recipient_role"
                ),
                @Index(
                        name = "idx_notification_type",
                        columnList = "notification_type"
                ),
                @Index(
                        name = "idx_notification_read_status",
                        columnList = "read_status"
                ),
                @Index(
                        name = "idx_notification_reference",
                        columnList = "reference_type,reference_id"
                ),
                @Index(
                        name = "idx_notification_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_notification_recipient_unread",
                        columnList = "recipient_email,read_status"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "event_id",
            nullable = false,
            updatable = false,
            unique = true
    )
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "notification_type",
            nullable = false,
            updatable = false,
            length = 50
    )
    private NotificationType notificationType;

    @Column(
            name = "recipient_email",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "recipient_role",
            nullable = false,
            updatable = false,
            length = 30
    )
    private RecipientRole recipientRole;

    @Column(
            name = "title",
            nullable = false,
            updatable = false,
            length = 250
    )
    private String title;

    @Column(
            name = "message",
            nullable = false,
            updatable = false,
            length = 2000
    )
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "reference_type",
            nullable = false,
            updatable = false,
            length = 30
    )
    private ReferenceType referenceType;

    @Column(
            name = "reference_id",
            nullable = false,
            updatable = false
    )
    private UUID referenceId;

    @Column(
            name = "read_status",
            nullable = false
    )
    @Builder.Default
    private Boolean readStatus = false;

    @Column(
            name = "read_at"
    )
    private LocalDateTime readAt;

    @Column(
            name = "occurred_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime occurredAt;

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

    @PrePersist
    protected void onCreate() {

        LocalDateTime currentTimestamp =
                LocalDateTime.now();

        if (readStatus == null) {
            readStatus = false;
        }

        if (occurredAt == null) {
            occurredAt = currentTimestamp;
        }

        if (createdAt == null) {
            createdAt = currentTimestamp;
        }

        updatedAt = currentTimestamp;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}