package com.naveen.healthcare.notificationservice.service;

import com.naveen.healthcare.notificationservice.dto.response.MarkAllReadResponse;
import com.naveen.healthcare.notificationservice.dto.response.NotificationResponse;
import com.naveen.healthcare.notificationservice.dto.response.UnreadCountResponse;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(
            String authenticatedEmail,
            RecipientRole recipientRole
    );

    List<NotificationResponse> getMyUnreadNotifications(
            String authenticatedEmail,
            RecipientRole recipientRole
    );

    UnreadCountResponse getMyUnreadCount(
            String authenticatedEmail,
            RecipientRole recipientRole
    );

    NotificationResponse markNotificationAsRead(
            String authenticatedEmail,
            RecipientRole recipientRole,
            UUID notificationId
    );

    MarkAllReadResponse markAllNotificationsAsRead(
            String authenticatedEmail,
            RecipientRole recipientRole
    );
}