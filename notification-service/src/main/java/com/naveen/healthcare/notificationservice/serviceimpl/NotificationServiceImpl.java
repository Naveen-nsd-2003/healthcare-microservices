package com.naveen.healthcare.notificationservice.serviceimpl;

import com.naveen.healthcare.notificationservice.dto.response.MarkAllReadResponse;
import com.naveen.healthcare.notificationservice.dto.response.NotificationResponse;
import com.naveen.healthcare.notificationservice.dto.response.UnreadCountResponse;
import com.naveen.healthcare.notificationservice.entity.Notification;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import com.naveen.healthcare.notificationservice.exception.NotificationNotFoundException;
import com.naveen.healthcare.notificationservice.repository.NotificationRepository;
import com.naveen.healthcare.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    private final NotificationRepository
            notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(
            String authenticatedEmail,
            RecipientRole recipientRole
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        validateRecipientRole(recipientRole);

        return notificationRepository
                .findAllByRecipientEmailIgnoreCaseAndRecipientRoleOrderByOccurredAtDescCreatedAtDesc(
                        normalizedEmail,
                        recipientRole
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse>
    getMyUnreadNotifications(
            String authenticatedEmail,
            RecipientRole recipientRole
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        validateRecipientRole(recipientRole);

        return notificationRepository
                .findAllByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalseOrderByOccurredAtDescCreatedAtDesc(
                        normalizedEmail,
                        recipientRole
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse getMyUnreadCount(
            String authenticatedEmail,
            RecipientRole recipientRole
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        validateRecipientRole(recipientRole);

        long unreadCount =
                notificationRepository
                        .countByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalse(
                                normalizedEmail,
                                recipientRole
                        );

        return new UnreadCountResponse(
                unreadCount
        );
    }

    @Override
    @Transactional
    public NotificationResponse markNotificationAsRead(
            String authenticatedEmail,
            RecipientRole recipientRole,
            UUID notificationId
    ) {

        validateNotificationId(notificationId);

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        validateRecipientRole(recipientRole);

        Notification notification =
                notificationRepository
                        .findByIdAndRecipientEmailIgnoreCaseAndRecipientRole(
                                notificationId,
                                normalizedEmail,
                                recipientRole
                        )
                        .orElseThrow(() ->
                                new NotificationNotFoundException(
                                        "Notification not found for "
                                                + "the authenticated user"
                                )
                        );

        if (Boolean.TRUE.equals(
                notification.getReadStatus()
        )) {

            return mapToResponse(notification);
        }

        notification.setReadStatus(true);
        notification.setReadAt(
                LocalDateTime.now()
        );

        Notification updatedNotification =
                notificationRepository.save(
                        notification
                );

        return mapToResponse(
                updatedNotification
        );
    }

    @Override
    @Transactional
    public MarkAllReadResponse markAllNotificationsAsRead(
            String authenticatedEmail,
            RecipientRole recipientRole
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        validateRecipientRole(recipientRole);

        LocalDateTime readAt =
                LocalDateTime.now();

        int updatedCount =
                notificationRepository.markAllAsRead(
                        normalizedEmail,
                        recipientRole,
                        readAt
                );

        String message;

        if (updatedCount == 0) {
            message =
                    "There are no unread notifications";
        } else {
            message =
                    "All unread notifications were marked as read";
        }

        return new MarkAllReadResponse(
                message,
                updatedCount,
                readAt
        );
    }

    private NotificationResponse mapToResponse(
            Notification notification
    ) {

        return NotificationResponse.builder()
                .id(
                        notification.getId()
                )
                .eventId(
                        notification.getEventId()
                )
                .notificationType(
                        notification.getNotificationType()
                )
                .recipientEmail(
                        notification.getRecipientEmail()
                )
                .recipientRole(
                        notification.getRecipientRole()
                )
                .title(
                        notification.getTitle()
                )
                .message(
                        notification.getMessage()
                )
                .referenceType(
                        notification.getReferenceType()
                )
                .referenceId(
                        notification.getReferenceId()
                )
                .readStatus(
                        notification.getReadStatus()
                )
                .readAt(
                        notification.getReadAt()
                )
                .occurredAt(
                        notification.getOccurredAt()
                )
                .createdAt(
                        notification.getCreatedAt()
                )
                .updatedAt(
                        notification.getUpdatedAt()
                )
                .build();
    }

    private void validateNotificationId(
            UUID notificationId
    ) {

        if (notificationId == null) {
            throw new NotificationNotFoundException(
                    "Notification ID is required"
            );
        }
    }

    private void validateRecipientRole(
            RecipientRole recipientRole
    ) {

        if (recipientRole == null) {
            throw new IllegalArgumentException(
                    "Authenticated recipient role is required"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}