package com.naveen.healthcare.notificationservice.dto.response;

import com.naveen.healthcare.notificationservice.enums.NotificationType;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import com.naveen.healthcare.notificationservice.enums.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class NotificationResponse {

    private UUID id;

    private UUID eventId;

    private NotificationType notificationType;

    private String recipientEmail;

    private RecipientRole recipientRole;

    private String title;

    private String message;

    private ReferenceType referenceType;

    private UUID referenceId;

    private Boolean readStatus;

    private LocalDateTime readAt;

    private LocalDateTime occurredAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}