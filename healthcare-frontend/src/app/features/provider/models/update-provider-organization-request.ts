import {
    ProviderType
  } from './provider-type';
  
  export interface UpdateProviderOrganizationRequest {
    organizationName: string;
    providerType: ProviderType;
    contactNumber: string;
    website: string | null;
    addressLine: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    emergencyServicesAvailable: boolean;
  }