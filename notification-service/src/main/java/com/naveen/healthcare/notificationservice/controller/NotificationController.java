package com.naveen.healthcare.notificationservice.controller;

import com.naveen.healthcare.notificationservice.dto.response.MarkAllReadResponse;
import com.naveen.healthcare.notificationservice.dto.response.NotificationResponse;
import com.naveen.healthcare.notificationservice.dto.response.UnreadCountResponse;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import com.naveen.healthcare.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService
            notificationService;

    @PreAuthorize(
            "hasAnyRole('MEMBER','PROVIDER','PAYER','ADMIN')"
    )
    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponse>>
    getMyNotifications(
            Authentication authentication
    ) {

        List<NotificationResponse> response =
                notificationService.getMyNotifications(
                        authentication.getName(),
                        resolveRecipientRole(authentication)
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasAnyRole('MEMBER','PROVIDER','PAYER','ADMIN')"
    )
    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponse>>
    getMyUnreadNotifications(
            Authentication authentication
    ) {

        List<NotificationResponse> response =
                notificationService
                        .getMyUnreadNotifications(
                                authentication.getName(),
                                resolveRecipientRole(
                                        authentication
                                )
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasAnyRole('MEMBER','PROVIDER','PAYER','ADMIN')"
    )
    @GetMapping("/me/unread-count")
    public ResponseEntity<UnreadCountResponse>
    getMyUnreadCount(
            Authentication authentication
    ) {

        UnreadCountResponse response =
                notificationService.getMyUnreadCount(
                        authentication.getName(),
                        resolveRecipientRole(authentication)
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasAnyRole('MEMBER','PROVIDER','PAYER','ADMIN')"
    )
    @PatchMapping("/me/{notificationId}/read")
    public ResponseEntity<NotificationResponse>
    markNotificationAsRead(
            Authentication authentication,
            @PathVariable UUID notificationId
    ) {

        NotificationResponse response =
                notificationService.markNotificationAsRead(
                        authentication.getName(),
                        resolveRecipientRole(authentication),
                        notificationId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize(
            "hasAnyRole('MEMBER','PROVIDER','PAYER','ADMIN')"
    )
    @PatchMapping("/me/read-all")
    public ResponseEntity<MarkAllReadResponse>
    markAllNotificationsAsRead(
            Authentication authentication
    ) {

        MarkAllReadResponse response =
                notificationService
                        .markAllNotificationsAsRead(
                                authentication.getName(),
                                resolveRecipientRole(
                                        authentication
                                )
                        );

        return ResponseEntity.ok(response);
    }

    private RecipientRole resolveRecipientRole(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "Authenticated user is required"
            );
        }

        for (
                GrantedAuthority authority
                : authentication.getAuthorities()
        ) {

            String authorityValue =
                    authority.getAuthority();

            if ("ROLE_MEMBER".equals(
                    authorityValue
            )) {
                return RecipientRole.MEMBER;
            }

            if ("ROLE_PROVIDER".equals(
                    authorityValue
            )) {
                return RecipientRole.PROVIDER;
            }

            if ("ROLE_PAYER".equals(
                    authorityValue
            )) {
                return RecipientRole.PAYER;
            }

            if ("ROLE_ADMIN".equals(
                    authorityValue
            )) {
                return RecipientRole.ADMIN;
            }
        }

        throw new IllegalArgumentException(
                "Authenticated user does not have "
                        + "a supported notification role"
        );
    }
}