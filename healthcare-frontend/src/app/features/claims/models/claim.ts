import {
    ClaimStatus
  } from './claim-status';
  
  export interface Claim {
  
    id: string;
  
    claimNumber: string;
  
    appointmentId: string;
  
    medicalRecordId: string;
  
    policyId: string;
  
    policyNumber: string;
  
    memberId: string;
  
    memberEmail: string;
  
    providerId: string;
  
    providerOrganizationName: string;
  
    departmentId: string;
  
    departmentName: string;
  
    doctorId: string;
  
    doctorName: string;
  
    doctorSpecialization: string;
  
    payerId: string;
  
    payerOrganizationName: string;
  
    planId: string;
  
    planCode: string;
  
    planName: string;
  
    serviceDate: string;
  
    consultationFee: number;
  
    claimedAmount: number;
  
    approvedAmount: number | null;
  
    coverageAmount: number;
  
    annualDeductible: number;
  
    copayPercentage: number;
  
    coinsurancePercentage: number;
  
    policyCoverageStartDate: string;
  
    policyCoverageEndDate: string;
  
    diagnosisSnapshot: string;
  
    treatmentPlanSnapshot: string;
  
    claimDescription: string;
  
    status: ClaimStatus;
  
    reviewRemarks: string | null;
  
    rejectionReason: string | null;
  
    cancellationReason: string | null;
  
    submittedAt: string;
  
    reviewedAt: string | null;
  
    resolvedAt: string | null;
  
    cancelledAt: string | null;
  
    createdAt: string;
  
    updatedAt: string;
  }