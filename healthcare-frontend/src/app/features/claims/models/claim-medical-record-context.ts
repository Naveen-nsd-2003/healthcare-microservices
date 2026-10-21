export type ClaimMedicalRecordStatus =
  | 'ACTIVE'
  | 'AMENDED';

export interface ClaimMedicalRecordContext {

  medicalRecordId: string;

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

  diagnosis: string;

  treatmentPlan: string;

  status: ClaimMedicalRecordStatus;
}