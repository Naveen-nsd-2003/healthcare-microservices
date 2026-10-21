import {
    ProviderType
  } from './provider-type';
  
  export interface CreateProviderOrganizationRequest {
    organizationName: string;
    providerType: ProviderType;
    registrationNumber: string;
    licenseNumber: string;
    contactNumber: string;
    website: string | null;
    addressLine: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    emergencyServicesAvailable: boolean;
  }