export interface CreatePayerOrganizationRequest {
    organizationName: string;
    registrationNumber: string;
    contactNumber: string;
    website: string | null;
    addressLine: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
  }