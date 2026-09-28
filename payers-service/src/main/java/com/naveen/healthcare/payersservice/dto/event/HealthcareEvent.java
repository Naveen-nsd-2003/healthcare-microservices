package com.naveen.healthcare.payersservice.dto.event;

import com.naveen.healthcare.payersservice.enums.NotificationType;
import com.naveen.healthcare.payersservice.enums.RecipientRole;
import com.naveen.healthcare.payersservice.enums.ReferenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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