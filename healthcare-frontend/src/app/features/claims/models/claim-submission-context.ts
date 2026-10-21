import {
    ClaimAppointmentContext
  } from './claim-appointment-context';
  
  import {
    ClaimMedicalRecordContext
  } from './claim-medical-record-context';
  
  import {
    EligibleClaimPolicy
  } from './eligible-claim-policy';
  
  export interface ClaimSubmissionContext {
  
    message: string;
  
    appointment: ClaimAppointmentContext;
  
    medicalRecord: ClaimMedicalRecordContext;
  
    eligiblePolicies: EligibleClaimPolicy[];
  }