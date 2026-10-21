export interface UpdateInsurancePlanRequest {
    planName: string;
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
  }