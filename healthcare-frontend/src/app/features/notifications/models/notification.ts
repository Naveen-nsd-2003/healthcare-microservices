import {
  NotificationType
} from './notification-type';

import {
  NotificationReferenceType
} from './notification-reference-type';

import {
  NotificationRecipientRole
} from './notification-recipient-role';

export interface Notification {
  id: string;
  eventId: string;
  notificationType: NotificationType;
  recipientEmail: string;
  recipientRole: NotificationRecipientRole;
  title: string;
  message: string;
  referenceType: NotificationReferenceType;
  referenceId: string;
  readStatus: boolean;
  readAt: string | null;
  occurredAt: string;
  createdAt: string;
  updatedAt: string;
}