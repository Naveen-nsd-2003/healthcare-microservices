package com.naveen.healthcare.appointmentservice.dto.event;

import com.naveen.healthcare.appointmentservice.enums.NotificationType;
import com.naveen.healthcare.appointmentservice.enums.RecipientRole;
import com.naveen.healthcare.appointmentservice.enums.ReferenceType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Builder
@Getter
@Setter
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
