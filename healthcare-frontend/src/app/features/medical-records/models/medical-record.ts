import {
    MedicalRecordStatus
  } from './medical-record-status';
  
  export interface MedicalRecord {
  
    id: string;
  
    appointmentId: string;
  
    memberId: string;
  
    memberEmail: string;
  
    providerId: string;
  
    providerOrganizationName: string;
  
    departmentId: string;
  
    departmentName: string;
  
    doctorId: string;
  
    doctorName: string;
  
    doctorSpecialization: string;
  
    appointmentDate: string;
  
    appointmentStartTime: string;
  
    appointmentEndTime: string;
  
    chiefComplaint: string;
  
    diagnosis: string;
  
    clinicalObservations: string | null;
  
    treatmentPlan: string;
  
    prescriptionNotes: string | null;
  
    followUpDate: string | null;
  
    status: MedicalRecordStatus;
  
    createdAt: string;
  
    updatedAt: string;
  }