import {
    DepartmentStatus
  } from './department-status';
  
  export interface Department {
    id: string;
    departmentName: string;
    departmentCode: string;
    description: string;
    contactNumber: string | null;
    location: string | null;
    status: DepartmentStatus;
    providerId: string;
    providerOrganizationName: string;
    createdAt: string;
    updatedAt: string;
  }