import {
  PolicyStatus
} from './policy-status';

export interface InsurancePolicy {
  id: string;
  policyNumber: string;
  enrollmentId: string;
  memberId: string;
  memberEmail: string;
  planId: string;
  planName: string;
  planCode: string;
  payerId: string;
  payerOrganizationName: string;
  coverageAmount: number;
  monthlyPremium: number;
  annualDeductible: number;
  copayPercentage: number;
  coinsurancePercentage: number;
  policyDurationMonths: number;
  waitingPeriodDays: number;
  hospitalizationCoverage: boolean;
  outpatientCoverage: boolean;
  prescriptionCoverage: boolean;
  maternityCoverage: boolean;
  coverageStartDate: string;
  coverageEndDate: string;
  status: PolicyStatus;
  createdAt: string;
  updatedAt: string;
}