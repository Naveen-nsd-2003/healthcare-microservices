import {
  PayerStatus
} from './payer-status';

export interface PayerOrganization {
  id: string;
  organizationName: string;
  registrationNumber: string;
  adminEmail: string;
  contactNumber: string;
  website: string | null;
  addressLine: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  status: PayerStatus;
  submittedAt: string | null;
  reviewedAt: string | null;
  reviewedByEmail: string | null;
  reviewRemarks: string | null;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
}