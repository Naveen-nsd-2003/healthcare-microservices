import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  Notification
} from '../models/notification';

import {
  UnreadCountResponse
} from '../models/unread-count-response';

import {
  MarkAllReadResponse
} from '../models/mark-all-read-response';

@Injectable({
  providedIn: 'root'
})
export class NotificationApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/notifications/me';

  getMyNotifications():
    Observable<Notification[]> {

    return this.http.get<Notification[]>(
      this.baseUrl
    );
  }

  getMyUnreadNotifications():
    Observable<Notification[]> {

    return this.http.get<Notification[]>(
      `${this.baseUrl}/unread`
    );
  }

  getMyUnreadCount():
    Observable<UnreadCountResponse> {

    return this.http.get<UnreadCountResponse>(
      `${this.baseUrl}/unread-count`
    );
  }

  markNotificationAsRead(
    notificationId: string
  ): Observable<Notification> {

    return this.http.patch<Notification>(
      `${this.baseUrl}/${notificationId}/read`,
      null
    );
  }

  markAllNotificationsAsRead():
    Observable<MarkAllReadResponse> {

    return this.http.patch<MarkAllReadResponse>(
      `${this.baseUrl}/read-all`,
      null
    );
  }
}