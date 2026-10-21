import {
  MemberStatus
} from './member-status';

export interface MemberProfile {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  dateOfBirth: string;
  phoneNumber: string;
  addressLine: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  emergencyContactName: string;
  emergencyContactNumber: string;
  status: MemberStatus;
  createdAt: string;
  updatedAt: string;
}