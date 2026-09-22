package com.naveen.healthcare.notificationservice.serviceimpl;

import com.naveen.healthcare.notificationservice.dto.event.HealthcareEvent;
import com.naveen.healthcare.notificationservice.entity.Notification;
import com.naveen.healthcare.notificationservice.exception.InvalidNotificationEventException;
import com.naveen.healthcare.notificationservice.repository.NotificationRepository;
import com.naveen.healthcare.notificationservice.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NotificationEventServiceImpl
        implements NotificationEventService {

    private final NotificationRepository
            notificationRepository;

    @Override
    @Transactional
    public void processEvent(
            HealthcareEvent event
    ) {

        validateEvent(event);

        if (notificationRepository.existsByEventId(
                event.getEventId()
        )) {

            return;
        }

        Notification notification =
                Notification.builder()
                        .eventId(
                                event.getEventId()
                        )
                        .notificationType(
                                event.getEventType()
                        )
                        .recipientEmail(
                                normalizeEmail(
                                        event.getRecipientEmail()
                                )
                        )
                        .recipientRole(
                                event.getRecipientRole()
                        )
                        .title(
                                normalizeRequiredText(
                                        event.getTitle(),
                                        "Notification title"
                                )
                        )
                        .message(
                                normalizeRequiredText(
                                        event.getMessage(),
                                        "Notification message"
                                )
                        )
                        .referenceType(
                                event.getReferenceType()
                        )
                        .referenceId(
                                event.getReferenceId()
                        )
                        .readStatus(false)
                        .readAt(null)
                        .occurredAt(
                                event.getOccurredAt()
                        )
                        .build();

        try {

            notificationRepository.saveAndFlush(
                    notification
            );

        } catch (DataIntegrityViolationException exception) {

            if (notificationRepository.existsByEventId(
                    event.getEventId()
            )) {
                return;
            }

            throw exception;
        }
    }

    private void validateEvent(
            HealthcareEvent event
    ) {

        if (event == null) {
            throw new InvalidNotificationEventException(
                    "Healthcare event is required"
            );
        }

        if (event.getEventId() == null) {
            throw new InvalidNotificationEventException(
                    "Healthcare event ID is required"
            );
        }

        if (event.getEventType() == null) {
            throw new InvalidNotificationEventException(
                    "Healthcare event type is required"
            );
        }

        if (event.getRecipientEmail() == null
                || event.getRecipientEmail().isBlank()) {

            throw new InvalidNotificationEventException(
                    "Notification recipient email is required"
            );
        }

        if (event.getRecipientRole() == null) {
            throw new InvalidNotificationEventException(
                    "Notification recipient role is required"
            );
        }

        if (event.getReferenceType() == null) {
            throw new InvalidNotificationEventException(
                    "Notification reference type is required"
            );
        }

        if (event.getReferenceId() == null) {
            throw new InvalidNotificationEventException(
                    "Notification reference ID is required"
            );
        }

        if (event.getTitle() == null
                || event.getTitle().isBlank()) {

            throw new InvalidNotificationEventException(
                    "Notification title is required"
            );
        }

        if (event.getMessage() == null
                || event.getMessage().isBlank()) {

            throw new InvalidNotificationEventException(
                    "Notification message is required"
            );
        }

        if (event.getOccurredAt() == null) {
            throw new InvalidNotificationEventException(
                    "Healthcare event occurrence time is required"
            );
        }

        if (event.getOccurredAt().isAfter(
                LocalDateTime.now().plusMinutes(5)
        )) {

            throw new InvalidNotificationEventException(
                    "Healthcare event occurrence time "
                            + "cannot be in the future"
            );
        }

        validateFieldLength(
                event.getRecipientEmail(),
                150,
                "Notification recipient email"
        );

        validateFieldLength(
                event.getTitle(),
                250,
                "Notification title"
        );

        validateFieldLength(
                event.getMessage(),
                2000,
                "Notification message"
        );
    }

    private void validateFieldLength(
            String value,
            int maximumLength,
            String fieldName
    ) {

        if (value != null
                && value.trim().length() > maximumLength) {

            throw new InvalidNotificationEventException(
                    fieldName
                            + " must not exceed "
                            + maximumLength
                            + " characters"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidNotificationEventException(
                    "Notification recipient email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidNotificationEventException(
                    fieldName + " is required"
            );
        }

        return value.trim();
    }
}