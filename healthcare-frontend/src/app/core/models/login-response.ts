import { UserRole } from './user-role';

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  email: string;
  role: UserRole;
  firstLogin: boolean;
}