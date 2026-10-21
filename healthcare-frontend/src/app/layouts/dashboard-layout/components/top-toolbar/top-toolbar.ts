import {
  Component,
  DestroyRef,
  EventEmitter,
  OnInit,
  Output,
  ViewChild,
  inject
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  MatBadgeModule
} from '@angular/material/badge';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatDividerModule
} from '@angular/material/divider';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatMenuModule,
  MatMenuTrigger
} from '@angular/material/menu';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  interval
} from 'rxjs';

import {
  takeUntilDestroyed
} from '@angular/core/rxjs-interop';

import {
  AuthService
} from '../../../../core/auth/auth.service';

import {
  UserRole
} from '../../../../core/models/user-role';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  Notification as HealthcareNotification
} from '../../../../features/notifications/models/notification';

import {
  NotificationNavigationService
} from '../../../../features/notifications/services/notification-navigation.service';

import {
  NotificationStateService
} from '../../../../features/notifications/services/notification-state.service';

@Component({
  selector: 'app-top-toolbar',
  imports: [
    DatePipe,
    MatBadgeModule,
    MatButtonModule,
    MatDividerModule,
    MatIconModule,
    MatMenuModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './top-toolbar.html',
  styleUrl: './top-toolbar.scss'
})
export class TopToolbar
  implements OnInit {

  private readonly unreadCountRefreshIntervalMs =
    45_000;

  private readonly destroyRef =
    inject(DestroyRef);

  private readonly router =
    inject(Router);

  readonly authService =
    inject(AuthService);

  readonly notificationState =
    inject(NotificationStateService);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  private readonly notificationNavigation =
    inject(NotificationNavigationService);

  @Output()
  readonly menuToggled =
    new EventEmitter<void>();

  @Output()
  readonly logoutRequested =
    new EventEmitter<void>();

  @ViewChild(
    'notificationMenuTrigger',
    {
      read: MatMenuTrigger
    }
  )
  private notificationMenuTrigger?:
    MatMenuTrigger;

  ngOnInit(): void {

    this.notificationState
      .loadUnreadCount();

    interval(
      this.unreadCountRefreshIntervalMs
    )
      .pipe(
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe(
        () => {

          this.notificationState
            .loadUnreadCount();
        }
      );
  }

  toggleMenu(): void {

    this.menuToggled.emit();
  }

  requestLogout(): void {

    this.logoutRequested.emit();
  }

  openNotificationMenu(): void {

    this.notificationState
      .loadUnreadNotifications();
  }

  refreshNotifications(
    event: MouseEvent
  ): void {

    event.stopPropagation();

    if (
      this.notificationState
        .dropdownLoading()
    ) {

      return;
    }

    this.notificationState
      .loadUnreadNotifications();
  }

  openNotification(
    notification: HealthcareNotification,
    event: MouseEvent
  ): void {

    event.stopPropagation();

    if (
      this.notificationState
        .isMarkingAsRead(
          notification.id
        )
      || this.notificationState
        .markingAllAsRead()
    ) {

      return;
    }

    const navigateToReference = (
      selectedNotification:
        HealthcareNotification
    ): void => {

      this.notificationMenuTrigger
        ?.closeMenu();

      void this.notificationNavigation
        .navigateToNotification(
          selectedNotification
        );
    };

    if (notification.readStatus) {

      navigateToReference(
        notification
      );

      return;
    }

    this.notificationState.markAsRead(
      notification,
      updatedNotification => {

        navigateToReference(
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

    if (
      this.notificationState
        .isMarkingAsRead(
          notification.id
        )
      || this.notificationState
        .markingAllAsRead()
    ) {

      return;
    }

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

  markAllAsRead(
    event: MouseEvent
  ): void {

    event.stopPropagation();

    if (
      this.notificationState
        .hasReadActionInProgress()
    ) {

      return;
    }

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

  viewAllNotifications(
    event?: MouseEvent
  ): void {

    event?.stopPropagation();

    const role =
      this.authService.currentRole();

    if (!role) {
      return;
    }

    this.notificationMenuTrigger
      ?.closeMenu();

    const route =
      this.getNotificationRoute(
        role
      );

    void this.router.navigate([
      route
    ]);
  }

  getNotificationIcon(
    notification: HealthcareNotification
  ): string {

    const type =
      notification.notificationType;

    if (
      type.startsWith(
        'APPOINTMENT'
      )
    ) {

      return 'calendar_month';
    }

    if (
      type.startsWith(
        'CLAIM'
      )
    ) {

      return 'receipt_long';
    }

    if (
      type.startsWith(
        'ENROLLMENT'
      )
    ) {

      return 'assignment';
    }

    if (
      type.startsWith(
        'POLICY'
      )
    ) {

      return 'policy';
    }

    if (
      type.startsWith(
        'MEDICAL_RECORD'
      )
    ) {

      return 'medical_information';
    }

    return 'notifications';
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

    const type =
      notification.notificationType;

    if (
      type.includes(
        'APPROVED'
      )
      || type.includes(
        'ACTIVATED'
      )
      || type.includes(
        'COMPLETED'
      )
      || type.includes(
        'CONFIRMED'
      )
      || type.includes(
        'CREATED'
      )
    ) {

      return 'notification-icon--success';
    }

    if (
      type.includes(
        'REJECTED'
      )
      || type.includes(
        'CANCELLED'
      )
      || type.includes(
        'EXPIRED'
      )
      || type.includes(
        'NO_SHOW'
      )
    ) {

      return 'notification-icon--danger';
    }

    if (
      type.includes(
        'SUSPENDED'
      )
      || type.includes(
        'UNDER_REVIEW'
      )
      || type.includes(
        'AMENDED'
      )
    ) {

      return 'notification-icon--warning';
    }

    return 'notification-icon--information';
  }

  private getNotificationRoute(
    role: UserRole
  ): string {

    switch (role) {

      case 'MEMBER':
        return '/member/notifications';

      case 'PROVIDER':
        return '/provider/notifications';

      case 'PAYER':
        return '/payer/notifications';

      case 'ADMIN':
        return '/admin/notifications';

      default:
        return '/login';
    }
  }
}