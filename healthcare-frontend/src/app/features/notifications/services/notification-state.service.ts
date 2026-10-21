
import {
  Injectable,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  finalize
} from 'rxjs';

import {
  Notification
} from '../models/notification';

import {
  NotificationApiService
} from './notification-api.service';

@Injectable({
  providedIn: 'root'
})
export class NotificationStateService {

  private readonly notificationApi =
    inject(NotificationApiService);

  private readonly notificationsState =
    signal<Notification[]>([]);

  private readonly unreadNotificationsState =
    signal<Notification[]>([]);

  private readonly unreadCountState =
    signal(0);

  private readonly loadingState =
    signal(false);

  private readonly dropdownLoadingState =
    signal(false);

  private readonly unreadCountLoadingState =
    signal(false);

  private readonly markingAllAsReadState =
    signal(false);

  private readonly markingAsReadIdsState =
    signal<ReadonlySet<string>>(
      new Set<string>()
    );

  private readonly errorMessageState =
    signal<string | null>(
      null
    );

  readonly notifications =
    this.notificationsState
      .asReadonly();

  readonly unreadNotifications =
    this.unreadNotificationsState
      .asReadonly();

  readonly unreadCount =
    this.unreadCountState
      .asReadonly();

  readonly loading =
    this.loadingState
      .asReadonly();

  readonly dropdownLoading =
    this.dropdownLoadingState
      .asReadonly();

  readonly unreadCountLoading =
    this.unreadCountLoadingState
      .asReadonly();

  readonly markingAllAsRead =
    this.markingAllAsReadState
      .asReadonly();

  readonly markingAsReadIds =
    this.markingAsReadIdsState
      .asReadonly();

  readonly errorMessage =
    this.errorMessageState
      .asReadonly();

  readonly hasUnreadNotifications =
    computed(
      () =>
        this.unreadCountState() > 0
    );

  readonly hasReadActionInProgress =
    computed(
      () =>
        this.markingAllAsReadState()
        || this.markingAsReadIdsState()
          .size > 0
    );

  readonly recentUnreadNotifications =
    computed(
      () =>
        this.unreadNotificationsState()
          .slice(0, 5)
    );

  loadUnreadCount(): void {

    if (
      this.unreadCountLoadingState()
    ) {

      return;
    }

    this.unreadCountLoadingState.set(
      true
    );

    this.notificationApi
      .getMyUnreadCount()
      .pipe(
        finalize(
          () => {

            this.unreadCountLoadingState
              .set(false);
          }
        )
      )
      .subscribe({
        next: response => {

          this.unreadCountState.set(
            response.unreadCount
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          /*
           * Authentication errors are handled by the
           * global HTTP error interceptor.
           *
           * A failed unread-count request should not
           * make the whole toolbar unusable.
           */
          console.error(
            'Unable to load unread notification count',
            error
          );
        }
      });
  }

  loadUnreadNotifications(): void {

    if (
      this.dropdownLoadingState()
    ) {

      return;
    }

    this.dropdownLoadingState.set(
      true
    );

    this.errorMessageState.set(
      null
    );

    this.notificationApi
      .getMyUnreadNotifications()
      .pipe(
        finalize(
          () => {

            this.dropdownLoadingState
              .set(false);
          }
        )
      )
      .subscribe({
        next: notifications => {

          const sortedNotifications =
            this.sortNewestFirst(
              notifications
            );

          this.unreadNotificationsState.set(
            sortedNotifications
          );

          this.unreadCountState.set(
            sortedNotifications.length
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.errorMessageState.set(
            this.resolveErrorMessage(
              error
            )
          );
        }
      });
  }

  loadAllNotifications(): void {

    if (
      this.loadingState()
    ) {

      return;
    }

    this.loadingState.set(
      true
    );

    this.errorMessageState.set(
      null
    );

    this.notificationApi
      .getMyNotifications()
      .pipe(
        finalize(
          () => {

            this.loadingState.set(
              false
            );
          }
        )
      )
      .subscribe({
        next: notifications => {

          const sortedNotifications =
            this.sortNewestFirst(
              notifications
            );

          this.notificationsState.set(
            sortedNotifications
          );

          const unreadNotifications =
            sortedNotifications.filter(
              notification =>
                !notification.readStatus
            );

          this.unreadNotificationsState.set(
            unreadNotifications
          );

          this.unreadCountState.set(
            unreadNotifications.length
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.errorMessageState.set(
            this.resolveErrorMessage(
              error
            )
          );
        }
      });
  }

  markAsRead(
    notification: Notification,
    afterSuccess?: (
      updatedNotification: Notification
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (notification.readStatus) {

      afterSuccess?.(
        notification
      );

      return;
    }

    if (
      this.markingAllAsReadState()
      || this.isMarkingAsRead(
        notification.id
      )
    ) {

      return;
    }

    this.addMarkingAsReadId(
      notification.id
    );

    this.notificationApi
      .markNotificationAsRead(
        notification.id
      )
      .pipe(
        finalize(
          () => {

            this.removeMarkingAsReadId(
              notification.id
            );
          }
        )
      )
      .subscribe({
        next: updatedNotification => {

          this.updateNotificationLocally(
            updatedNotification
          );

          this.unreadNotificationsState.update(
            notifications =>
              notifications.filter(
                currentNotification =>
                  currentNotification.id
                  !== updatedNotification.id
              )
          );

          this.unreadCountState.update(
            count =>
              Math.max(
                0,
                count - 1
              )
          );

          this.errorMessageState.set(
            null
          );

          afterSuccess?.(
            updatedNotification
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(
              error
            );

          this.errorMessageState.set(
            message
          );

          afterError?.(
            message
          );
        }
      });
  }

  markAllAsRead(
    afterSuccess?: (
      message: string,
      updatedCount: number
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (
      this.markingAllAsReadState()
      || this.markingAsReadIdsState()
        .size > 0
    ) {

      return;
    }

    if (
      this.unreadCountState() === 0
    ) {

      afterSuccess?.(
        'All notifications are already read.',
        0
      );

      return;
    }

    this.markingAllAsReadState.set(
      true
    );

    this.notificationApi
      .markAllNotificationsAsRead()
      .pipe(
        finalize(
          () => {

            this.markingAllAsReadState
              .set(false);
          }
        )
      )
      .subscribe({
        next: response => {

          this.notificationsState.update(
            notifications =>
              notifications.map(
                notification => {

                  if (
                    notification.readStatus
                  ) {

                    return notification;
                  }

                  return {
                    ...notification,
                    readStatus: true,
                    readAt:
                      notification.readAt
                      ?? response.readAt,
                    updatedAt:
                      response.readAt
                  };
                }
              )
          );

          this.unreadNotificationsState.set(
            []
          );

          this.unreadCountState.set(
            0
          );

          this.errorMessageState.set(
            null
          );

          afterSuccess?.(
            response.message,
            response.updatedCount
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(
              error
            );

          this.errorMessageState.set(
            message
          );

          afterError?.(
            message
          );
        }
      });
  }

  isMarkingAsRead(
    notificationId: string
  ): boolean {

    return this.markingAsReadIdsState()
      .has(notificationId);
  }

  clearError(): void {

    this.errorMessageState.set(
      null
    );
  }

  clearState(): void {

    this.notificationsState.set(
      []
    );

    this.unreadNotificationsState.set(
      []
    );

    this.unreadCountState.set(
      0
    );

    this.loadingState.set(
      false
    );

    this.dropdownLoadingState.set(
      false
    );

    this.unreadCountLoadingState.set(
      false
    );

    this.markingAllAsReadState.set(
      false
    );

    this.markingAsReadIdsState.set(
      new Set<string>()
    );

    this.errorMessageState.set(
      null
    );
  }

  private addMarkingAsReadId(
    notificationId: string
  ): void {

    this.markingAsReadIdsState.update(
      currentIds => {

        const updatedIds =
          new Set(currentIds);

        updatedIds.add(
          notificationId
        );

        return updatedIds;
      }
    );
  }

  private removeMarkingAsReadId(
    notificationId: string
  ): void {

    this.markingAsReadIdsState.update(
      currentIds => {

        const updatedIds =
          new Set(currentIds);

        updatedIds.delete(
          notificationId
        );

        return updatedIds;
      }
    );
  }

  private updateNotificationLocally(
    updatedNotification: Notification
  ): void {

    this.notificationsState.update(
      notifications =>
        notifications.map(
          notification =>
            notification.id
            === updatedNotification.id
              ? updatedNotification
              : notification
        )
    );
  }

  private sortNewestFirst(
    notifications: Notification[]
  ): Notification[] {

    return [
      ...notifications
    ].sort(
      (
        firstNotification,
        secondNotification
      ) => {

        const firstTime =
          this.resolveTimestamp(
            firstNotification.occurredAt
          );

        const secondTime =
          this.resolveTimestamp(
            secondNotification.occurredAt
          );

        if (firstTime !== secondTime) {

          return secondTime
            - firstTime;
        }

        const firstCreatedTime =
          this.resolveTimestamp(
            firstNotification.createdAt
          );

        const secondCreatedTime =
          this.resolveTimestamp(
            secondNotification.createdAt
          );

        return secondCreatedTime
          - firstCreatedTime;
      }
    );
  }

  private resolveTimestamp(
    value: string
  ): number {

    const timestamp =
      new Date(value).getTime();

    if (
      Number.isNaN(timestamp)
    ) {

      return 0;
    }

    return timestamp;
  }

  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 0) {

      return 'Unable to connect to the notification service.';
    }

    const backendMessage =
      error.error?.message;

    if (
      typeof backendMessage === 'string'
      && backendMessage
        .trim()
        .length > 0
    ) {

      return backendMessage.trim();
    }

    if (error.status === 400) {

      return 'The notification request is invalid.';
    }

    if (error.status === 401) {

      return 'Your session has expired. Please sign in again.';
    }

    if (error.status === 403) {

      return 'You are not authorized to access notifications.';
    }

    if (error.status === 404) {

      return 'The selected notification could not be found.';
    }

    return 'Unable to process the notification request. Please try again.';
  }
}