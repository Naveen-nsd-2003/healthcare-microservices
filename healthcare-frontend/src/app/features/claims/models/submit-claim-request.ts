export interface SubmitClaimRequest {

    appointmentId: string;
  
    medicalRecordId: string;
  
    policyId: string;
  
    claimedAmount: number;
  
    claimDescription: string;
  }