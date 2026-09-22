package com.naveen.healthcare.notificationservice.dto.event;

import com.naveen.healthcare.notificationservice.enums.NotificationType;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import com.naveen.healthcare.notificationservice.enums.ReferenceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class HealthcareEvent {

    private UUID eventId;

    private NotificationType eventType;

    private LocalDateTime occurredAt;

    private String recipientEmail;

    private RecipientRole recipientRole;

    private ReferenceType referenceType;

    private UUID referenceId;

    private String title;

    private String message;

    private Map<String, Object> metadata;
}