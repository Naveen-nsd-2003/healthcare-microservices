import {
    DoctorStatus
  } from './doctor-status';
  
  export interface Doctor {
    id: string;
    firstName: string;
    lastName: string;
    fullName: string;
    medicalRegistrationNumber: string;
    qualification: string;
    specialization: string;
    yearsOfExperience: number;
    consultationFee: number;
    biography: string | null;
    status: DoctorStatus;
    departmentId: string;
    departmentName: string;
    departmentCode: string;
    providerId: string;
    providerOrganizationName: string;
    createdAt: string;
    updatedAt: string;
  }