/*
package com.naveen.healthcare.notificationservice.repository;

import com.naveen.healthcare.notificationservice.entity.Notification;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    boolean existsByEventId(
            UUID eventId
    );

    Optional<Notification> findByEventId(
            UUID eventId
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(
            String recipientEmail
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleOrderByCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndReadStatusFalseOrderByCreatedAtDesc(
            String recipientEmail
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalseOrderByCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    */
/*
     * User-facing notification ordering should use the
     * business event occurrence time, not insertion time.
     *//*

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleOrderByOccurredAtDescCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalseOrderByOccurredAtDescCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    long countByRecipientEmailIgnoreCaseAndReadStatusFalse(
            String recipientEmail
    );

    long countByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalse(
            String recipientEmail,
            RecipientRole recipientRole
    );

    Optional<Notification>
    findByIdAndRecipientEmailIgnoreCase(
            UUID notificationId,
            String recipientEmail
    );

    Optional<Notification>
    findByIdAndRecipientEmailIgnoreCaseAndRecipientRole(
            UUID notificationId,
            String recipientEmail,
            RecipientRole recipientRole
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            """
            update Notification notification
            set notification.readStatus = true,
                notification.readAt = :readAt,
                notification.updatedAt = :readAt
            where lower(notification.recipientEmail) =
                  lower(:recipientEmail)
              and notification.recipientRole = :recipientRole
              and notification.readStatus = false
            """
    )
    int markAllAsRead(
            @Param("recipientEmail")
            String recipientEmail,

            @Param("recipientRole")
            RecipientRole recipientRole,

            @Param("readAt")
            LocalDateTime readAt
    );
}*/

package com.naveen.healthcare.notificationservice.repository;

import com.naveen.healthcare.notificationservice.entity.Notification;
import com.naveen.healthcare.notificationservice.enums.RecipientRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository
        extends JpaRepository<Notification, UUID> {

    boolean existsByEventId(
            UUID eventId
    );

    Optional<Notification> findByEventId(
            UUID eventId
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(
            String recipientEmail
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleOrderByCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndReadStatusFalseOrderByCreatedAtDesc(
            String recipientEmail
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalseOrderByCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    /*
     * User-facing notification ordering should use the
     * business event occurrence time, not insertion time.
     */
    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleOrderByOccurredAtDescCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    List<Notification>
    findAllByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalseOrderByOccurredAtDescCreatedAtDesc(
            String recipientEmail,
            RecipientRole recipientRole
    );

    long countByRecipientEmailIgnoreCaseAndReadStatusFalse(
            String recipientEmail
    );

    long countByRecipientEmailIgnoreCaseAndRecipientRoleAndReadStatusFalse(
            String recipientEmail,
            RecipientRole recipientRole
    );

    Optional<Notification>
    findByIdAndRecipientEmailIgnoreCase(
            UUID notificationId,
            String recipientEmail
    );

    Optional<Notification>
    findByIdAndRecipientEmailIgnoreCaseAndRecipientRole(
            UUID notificationId,
            String recipientEmail,
            RecipientRole recipientRole
    );

    @Modifying(
            clearAutomatically = true,
            flushAutomatically = true
    )
    @Query(
            """
            update Notification notification
            set notification.readStatus = true,
                notification.readAt = :readAt,
                notification.updatedAt = :readAt
            where lower(notification.recipientEmail) =
                  lower(:recipientEmail)
              and notification.recipientRole = :recipientRole
              and notification.readStatus = false
            """
    )
    int markAllAsRead(
            @Param("recipientEmail")
            String recipientEmail,

            @Param("recipientRole")
            RecipientRole recipientRole,

            @Param("readAt")
            LocalDateTime readAt
    );
}