import {
  EnrollmentStatus
} from './enrollment-status';

import {
  NomineeRelationship
} from './nominee-relationship';

export interface Enrollment {
  id: string;
  memberId: string;
  memberEmail: string;
  planId: string;
  planCode: string;
  planName: string;
  coverageAmount: number;
  monthlyPremium: number;
  payerId: string;
  payerOrganizationName: string;
  requestedCoverageStartDate: string;
  nomineeName: string;
  nomineeRelationship: NomineeRelationship;
  nomineeContactNumber: string;
  status: EnrollmentStatus;
  reviewRemarks: string | null;
  reviewedAt: string | null;
  createdAt: string;
  updatedAt: string;
}
