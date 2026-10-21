export type AdminAccountRole =
  | 'ADMIN'
  | 'MEMBER'
  | 'PROVIDER'
  | 'PAYER';

export type AdminAccountStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'BLOCKED'
  | 'SUSPENDED';

export interface AdminAccountSummary {

  id: string;

  username: string;

  firstName: string;

  lastName: string;

  fullName: string;

  email: string;

  phoneNumber: string | null;

  role: AdminAccountRole;

  status: AdminAccountStatus;

  firstLogin: boolean;

  accountLocked: boolean;

  failedAttempts: number;

  createdAt: string;

  updatedAt: string;
}