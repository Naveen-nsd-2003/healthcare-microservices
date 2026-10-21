import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  DatePipe,
  NgClass
} from '@angular/common';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatButtonToggleModule
} from '@angular/material/button-toggle';

import {
  MatCardModule
} from '@angular/material/card';

import {
  MatFormFieldModule
} from '@angular/material/form-field';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  MatSelectModule
} from '@angular/material/select';

import {
  Notification as HealthcareNotification
} from '../../models/notification';

import {
  NotificationCategoryFilter,
  NotificationReadFilter
} from '../../models/notification-filter';

import {
  NotificationNavigationService
} from '../../services/notification-navigation.service';

import {
  NotificationStateService
} from '../../services/notification-state.service';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

@Component({
  selector: 'app-notifications-page',
  imports: [
    DatePipe,
    NgClass,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './notifications-page.html',
  styleUrl: './notifications-page.scss'
})
export class NotificationsPage
  implements OnInit {

  readonly notificationState =
    inject(NotificationStateService);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  private readonly notificationNavigation =
    inject(NotificationNavigationService);

  readonly readFilter =
    signal<NotificationReadFilter>(
      'ALL'
    );

  readonly categoryFilter =
    signal<NotificationCategoryFilter>(
      'ALL'
    );

  readonly filteredNotifications =
    computed(
      () => {

        const notifications =
          this.notificationState
            .notifications();

        const selectedReadFilter =
          this.readFilter();

        const selectedCategory =
          this.categoryFilter();

        return notifications.filter(
          notification => {

            const matchesReadFilter =
              selectedReadFilter === 'ALL'
              || !notification.readStatus;

            const matchesCategory =
              selectedCategory === 'ALL'
              || notification.referenceType
              === selectedCategory;

            return matchesReadFilter
              && matchesCategory;
          }
        );
      }
    );

  readonly displayedNotificationCount =
    computed(
      () =>
        this.filteredNotifications()
          .length
    );

  ngOnInit(): void {

    this.loadNotifications();
  }

  loadNotifications(): void {

    this.notificationState
      .loadAllNotifications();
  }

  changeReadFilter(
    value: NotificationReadFilter
  ): void {

    this.readFilter.set(
      value
    );
  }

  changeCategoryFilter(
    value: NotificationCategoryFilter
  ): void {

    this.categoryFilter.set(
      value
    );
  }

  resetFilters(): void {

    this.readFilter.set(
      'ALL'
    );

    this.categoryFilter.set(
      'ALL'
    );
  }

  openNotification(
    notification: HealthcareNotification
  ): void {

    const isProcessing =
      this.notificationState
        .isMarkingAsRead(
          notification.id
        );

    if (
      isProcessing
      || this.notificationState
        .markingAllAsRead()
    ) {

      return;
    }

    if (notification.readStatus) {

      void this.notificationNavigation
        .navigateToNotification(
          notification
        );

      return;
    }

    this.notificationState.markAsRead(
      notification,
      updatedNotification => {

        void this.notificationNavigation
          .navigateToNotification(
            updatedNotification
          );
      },
      message => {

        this.notificationMessage
          .showError(
            message
          );
      }
    );
  }

  markNotificationAsRead(
    notification: HealthcareNotification,
    event: MouseEvent
  ): void {

    event.stopPropagation();

    this.notificationState.markAsRead(
      notification,
      () => {

        this.notificationMessage
          .showSuccess(
            'Notification marked as read.'
          );
      },
      message => {

        this.notificationMessage
          .showError(
            message
          );
      }
    );
  }

  markAllAsRead(): void {

    this.notificationState.markAllAsRead(
      (
        message,
        updatedCount
      ) => {

        if (updatedCount === 0) {

          this.notificationMessage
            .showInformation(
              message
            );

          return;
        }

        this.notificationMessage
          .showSuccess(
            message
          );
      },
      message => {

        this.notificationMessage
          .showError(
            message
          );
      }
    );
  }

  getNotificationIcon(
    notification: HealthcareNotification
  ): string {

    switch (
      notification.referenceType
    ) {

      case 'APPOINTMENT':
        return 'calendar_month';

      case 'CLAIM':
        return 'receipt_long';

      case 'ENROLLMENT':
        return 'assignment';

      case 'POLICY':
        return 'policy';

      case 'MEDICAL_RECORD':
        return 'medical_information';

      default:
        return 'notifications';
    }
  }

  getNotificationCategory(
    notification: HealthcareNotification
  ): string {

    switch (
      notification.referenceType
    ) {

      case 'APPOINTMENT':
        return 'Appointment';

      case 'CLAIM':
        return 'Claim';

      case 'ENROLLMENT':
        return 'Enrollment';

      case 'POLICY':
        return 'Policy';

      case 'MEDICAL_RECORD':
        return 'Medical Record';

      default:
        return 'Notification';
    }
  }

  getNotificationClass(
    notification: HealthcareNotification
  ): string {

    const notificationType =
      notification.notificationType;

    if (
      notificationType.includes(
        'APPROVED'
      )
      || notificationType.includes(
        'ACTIVATED'
      )
      || notificationType.includes(
        'COMPLETED'
      )
      || notificationType.includes(
        'CONFIRMED'
      )
      || notificationType.includes(
        'CREATED'
      )
    ) {

      return 'notification-icon--success';
    }

    if (
      notificationType.includes(
        'REJECTED'
      )
      || notificationType.includes(
        'CANCELLED'
      )
      || notificationType.includes(
        'EXPIRED'
      )
      || notificationType.includes(
        'NO_SHOW'
      )
    ) {

      return 'notification-icon--danger';
    }

    if (
      notificationType.includes(
        'SUSPENDED'
      )
      || notificationType.includes(
        'UNDER_REVIEW'
      )
      || notificationType.includes(
        'AMENDED'
      )
    ) {

      return 'notification-icon--warning';
    }

    return 'notification-icon--information';
  }

  getReadableNotificationType(
    notification: HealthcareNotification
  ): string {

    return notification
      .notificationType
      .toLowerCase()
      .split('_')
      .map(
        word =>
          word.charAt(0).toUpperCase()
          + word.slice(1)
      )
      .join(' ');
  }
}