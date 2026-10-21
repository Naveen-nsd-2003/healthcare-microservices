import {
  ProviderStatus
} from './provider-status';

import {
  ProviderType
} from './provider-type';

export interface ProviderOrganization {
  id: string;
  organizationName: string;
  providerType: ProviderType;
  registrationNumber: string;
  licenseNumber: string;
  adminEmail: string;
  contactNumber: string;
  website: string | null;
  addressLine: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  emergencyServicesAvailable: boolean;
  status: ProviderStatus;
  submittedAt: string | null;
  reviewedAt: string | null;
  reviewedByEmail: string | null;
  reviewRemarks: string | null;
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
}