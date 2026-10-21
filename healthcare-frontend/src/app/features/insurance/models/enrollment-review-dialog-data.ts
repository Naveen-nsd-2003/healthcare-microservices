export type EnrollmentReviewAction =
  | 'APPROVE'
  | 'REJECT';

export interface EnrollmentReviewDialogData {
  action: EnrollmentReviewAction;
  enrollmentId: string;
  memberEmail: string;
  planName: string;
}

export interface EnrollmentReviewDialogResult {
  action: EnrollmentReviewAction;
  reviewRemarks: string;
}