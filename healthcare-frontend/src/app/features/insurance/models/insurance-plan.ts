import {
  InsurancePlanStatus
} from './insurance-plan-status';

export interface InsurancePlan {
  id: string;
  planName: string;
  planCode: string;
  description: string;
  coverageAmount: number;
  monthlyPremium: number;
  annualDeductible: number;
  copayPercentage: number;
  coinsurancePercentage: number;
  minimumAge: number;
  maximumAge: number;
  policyDurationMonths: number;
  hospitalizationCoverage: boolean;
  outpatientCoverage: boolean;
  prescriptionCoverage: boolean;
  maternityCoverage: boolean;
  waitingPeriodDays: number;
  status: InsurancePlanStatus;
  payerId: string;
  payerOrganizationName: string;
  createdAt: string;
  updatedAt: string;
}