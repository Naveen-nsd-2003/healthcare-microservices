export interface EligibleClaimPolicy {

    policyId: string;
  
    policyNumber: string;
  
    payerId: string;
  
    payerOrganizationName: string;
  
    planId: string;
  
    planCode: string;
  
    planName: string;
  
    coverageAmount: number;
  
    annualDeductible: number;
  
    copayPercentage: number;
  
    coinsurancePercentage: number;
  
    outpatientCoverage: boolean;
  
    coverageStartDate: string;
  
    coverageEndDate: string;
  }