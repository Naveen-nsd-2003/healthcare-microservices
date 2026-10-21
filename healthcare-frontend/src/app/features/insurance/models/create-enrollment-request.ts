import {
  NomineeRelationship
} from './nominee-relationship';

export interface CreateEnrollmentRequest {
  planId: string;
  requestedCoverageStartDate: string;
  nomineeName: string;
  nomineeRelationship: NomineeRelationship;
  nomineeContactNumber: string;
}