import {
  Injectable,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  finalize
} from 'rxjs/operators';

import {
  ApproveClaimRequest
} from '../models/approve-claim-request';

import {
  CancelClaimRequest
} from '../models/cancel-claim-request';

import {
  Claim
} from '../models/claim';

import {
  ClaimSubmissionContext
} from '../models/claim-submission-context';

import {
  RejectClaimRequest
} from '../models/reject-claim-request';

import {
  SubmitClaimRequest
} from '../models/submit-claim-request';

import {
  ClaimApiService
} from './claim-api.service';

type ClaimSuccessCallback =
  (claim: Claim) => void;

type ContextSuccessCallback =
  (context: ClaimSubmissionContext) => void;

type ErrorCallback =
  (message: string) => void;

@Injectable({
  providedIn: 'root'
})
export class ClaimStateService {

  private readonly claimApi =
    inject(ClaimApiService);

  private readonly providerClaimsSignal =
    signal<Claim[]>([]);

  private readonly payerClaimsSignal =
    signal<Claim[]>([]);

  private readonly memberClaimsSignal =
    signal<Claim[]>([]);

  private readonly selectedClaimSignal =
    signal<Claim | null>(null);

  private readonly submissionContextSignal =
    signal<ClaimSubmissionContext | null>(
      null
    );

  private readonly contextLoadingSignal =
    signal(false);

  private readonly listLoadingSignal =
    signal(false);

  private readonly detailsLoadingSignal =
    signal(false);

  private readonly submittingSignal =
    signal(false);

  private readonly cancellingSignal =
    signal(false);

  private readonly reviewingSignal =
    signal(false);

  private readonly approvingSignal =
    signal(false);

  private readonly rejectingSignal =
    signal(false);

  private readonly errorMessageSignal =
    signal<string | null>(null);

  readonly providerClaims =
    this.providerClaimsSignal.asReadonly();

  readonly payerClaims =
    this.payerClaimsSignal.asReadonly();

  readonly memberClaims =
    this.memberClaimsSignal.asReadonly();

  readonly selectedClaim =
    this.selectedClaimSignal.asReadonly();

  readonly submissionContext =
    this.submissionContextSignal.asReadonly();

  readonly contextLoading =
    this.contextLoadingSignal.asReadonly();

  readonly listLoading =
    this.listLoadingSignal.asReadonly();

  readonly detailsLoading =
    this.detailsLoadingSignal.asReadonly();

  readonly submitting =
    this.submittingSignal.asReadonly();

  readonly cancelling =
    this.cancellingSignal.asReadonly();

  readonly reviewing =
    this.reviewingSignal.asReadonly();

  readonly approving =
    this.approvingSignal.asReadonly();

  readonly rejecting =
    this.rejectingSignal.asReadonly();

  readonly errorMessage =
    this.errorMessageSignal.asReadonly();

  readonly actionInProgress =
    computed(() => {
      return this.submittingSignal()
        || this.cancellingSignal()
        || this.reviewingSignal()
        || this.approvingSignal()
        || this.rejectingSignal();
    });

  readonly loading =
    computed(() => {
      return this.contextLoadingSignal()
        || this.listLoadingSignal()
        || this.detailsLoadingSignal();
    });

  loadProviderClaimSubmissionContext(
    appointmentId: string,
    medicalRecordId: string,
    onSuccess?: ContextSuccessCallback,
    onError?: ErrorCallback
  ): void {

    const normalizedAppointmentId =
      appointmentId.trim();

    const normalizedMedicalRecordId =
      medicalRecordId.trim();

    if (
      normalizedAppointmentId.length === 0
      || normalizedMedicalRecordId.length === 0
      || this.contextLoadingSignal()
    ) {
      return;
    }

    this.contextLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.submissionContextSignal.set(null);

    this.claimApi
      .getProviderClaimSubmissionContext(
        normalizedAppointmentId,
        normalizedMedicalRecordId
      )
      .pipe(
        finalize(() => {
          this.contextLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          context: ClaimSubmissionContext
        ) => {
          this.submissionContextSignal.set(
            context
          );

          if (onSuccess) {
            onSuccess(context);
          }
        },
        error: (
          error: unknown
        ) => {
          this.submissionContextSignal.set(
            null
          );

          const message =
            this.extractErrorMessage(
              error,
              'Unable to prepare the claim submission.'
            );

          this.errorMessageSignal.set(
            message
          );

          if (onError) {
            onError(message);
          }
        }
      });
  }

  submitClaim(
    request: SubmitClaimRequest,
    onSuccess?: ClaimSuccessCallback,
    onError?: ErrorCallback
  ): void {

    if (this.submittingSignal()) {
      return;
    }

    this.submittingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .submitClaim(request)
      .pipe(
        finalize(() => {
          this.submittingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertProviderClaim(
            claim
          );

          this.upsertMemberClaim(
            claim
          );

          if (onSuccess) {
            onSuccess(claim);
          }
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to submit the claim.'
            );

          this.errorMessageSignal.set(
            message
          );

          if (onError) {
            onError(message);
          }
        }
      });
  }

  loadProviderClaims(): void {

    if (this.listLoadingSignal()) {
      return;
    }

    this.listLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .getProviderClaims()
      .pipe(
        finalize(() => {
          this.listLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claims: Claim[]
        ) => {
          this.providerClaimsSignal.set(
            claims
          );
        },
        error: (
          error: unknown
        ) => {
          this.providerClaimsSignal.set([]);

          this.errorMessageSignal.set(
            this.extractErrorMessage(
              error,
              'Unable to load Provider claims.'
            )
          );
        }
      });
  }

  loadProviderClaimById(
    claimId: string
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.detailsLoadingSignal()
    ) {
      return;
    }

    this.detailsLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.selectedClaimSignal.set(null);

    this.claimApi
      .getProviderClaimById(
        normalizedClaimId
      )
      .pipe(
        finalize(() => {
          this.detailsLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertProviderClaim(
            claim
          );
        },
        error: (
          error: unknown
        ) => {
          this.errorMessageSignal.set(
            this.extractErrorMessage(
              error,
              'Unable to load the claim.'
            )
          );
        }
      });
  }

  cancelProviderClaim(
    claimId: string,
    request: CancelClaimRequest,
    onSuccess?: ClaimSuccessCallback,
    onError?: ErrorCallback
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.cancellingSignal()
    ) {
      return;
    }

    this.cancellingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .cancelProviderClaim(
        normalizedClaimId,
        request
      )
      .pipe(
        finalize(() => {
          this.cancellingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertProviderClaim(
            claim
          );

          this.upsertMemberClaim(
            claim
          );

          this.upsertPayerClaim(
            claim
          );

          if (onSuccess) {
            onSuccess(claim);
          }
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to cancel the claim.'
            );

          this.errorMessageSignal.set(
            message
          );

          if (onError) {
            onError(message);
          }
        }
      });
  }

  loadPayerClaims(): void {

    if (this.listLoadingSignal()) {
      return;
    }

    this.listLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .getPayerClaims()
      .pipe(
        finalize(() => {
          this.listLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claims: Claim[]
        ) => {
          this.payerClaimsSignal.set(
            claims
          );
        },
        error: (
          error: unknown
        ) => {
          this.payerClaimsSignal.set([]);

          this.errorMessageSignal.set(
            this.extractErrorMessage(
              error,
              'Unable to load Payer claims.'
            )
          );
        }
      });
  }

  loadPayerClaimById(
    claimId: string
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.detailsLoadingSignal()
    ) {
      return;
    }

    this.detailsLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.selectedClaimSignal.set(null);

    this.claimApi
      .getPayerClaimById(
        normalizedClaimId
      )
      .pipe(
        finalize(() => {
          this.detailsLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertPayerClaim(
            claim
          );
        },
        error: (
          error: unknown
        ) => {
          this.errorMessageSignal.set(
            this.extractErrorMessage(
              error,
              'Unable to load the claim.'
            )
          );
        }
      });
  }

  startPayerClaimReview(
    claimId: string,
    onSuccess?: ClaimSuccessCallback,
    onError?: ErrorCallback
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.reviewingSignal()
    ) {
      return;
    }

    this.reviewingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .startPayerClaimReview(
        normalizedClaimId
      )
      .pipe(
        finalize(() => {
          this.reviewingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertPayerClaim(
            claim
          );

          this.upsertProviderClaim(
            claim
          );

          this.upsertMemberClaim(
            claim
          );

          if (onSuccess) {
            onSuccess(claim);
          }
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to start the claim review.'
            );

          this.errorMessageSignal.set(
            message
          );

          if (onError) {
            onError(message);
          }
        }
      });
  }

  approveClaim(
    claimId: string,
    request: ApproveClaimRequest,
    onSuccess?: ClaimSuccessCallback,
    onError?: ErrorCallback
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.approvingSignal()
    ) {
      return;
    }

    this.approvingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .approveClaim(
        normalizedClaimId,
        request
      )
      .pipe(
        finalize(() => {
          this.approvingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertPayerClaim(
            claim
          );

          this.upsertProviderClaim(
            claim
          );

          this.upsertMemberClaim(
            claim
          );

          if (onSuccess) {
            onSuccess(claim);
          }
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to approve the claim.'
            );

          this.errorMessageSignal.set(
            message
          );

          if (onError) {
            onError(message);
          }
        }
      });
  }

  rejectClaim(
    claimId: string,
    request: RejectClaimRequest,
    onSuccess?: ClaimSuccessCallback,
    onError?: ErrorCallback
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.rejectingSignal()
    ) {
      return;
    }

    this.rejectingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .rejectClaim(
        normalizedClaimId,
        request
      )
      .pipe(
        finalize(() => {
          this.rejectingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertPayerClaim(
            claim
          );

          this.upsertProviderClaim(
            claim
          );

          this.upsertMemberClaim(
            claim
          );

          if (onSuccess) {
            onSuccess(claim);
          }
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to reject the claim.'
            );

          this.errorMessageSignal.set(
            message
          );

          if (onError) {
            onError(message);
          }
        }
      });
  }

  loadMyClaims(): void {

    if (this.listLoadingSignal()) {
      return;
    }

    this.listLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.claimApi
      .getMyClaims()
      .pipe(
        finalize(() => {
          this.listLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claims: Claim[]
        ) => {
          this.memberClaimsSignal.set(
            claims
          );
        },
        error: (
          error: unknown
        ) => {
          this.memberClaimsSignal.set([]);

          this.errorMessageSignal.set(
            this.extractErrorMessage(
              error,
              'Unable to load your claims.'
            )
          );
        }
      });
  }

  loadMyClaimById(
    claimId: string
  ): void {

    const normalizedClaimId =
      claimId.trim();

    if (
      normalizedClaimId.length === 0
      || this.detailsLoadingSignal()
    ) {
      return;
    }

    this.detailsLoadingSignal.set(true);

    this.errorMessageSignal.set(null);

    this.selectedClaimSignal.set(null);

    this.claimApi
      .getMyClaimById(
        normalizedClaimId
      )
      .pipe(
        finalize(() => {
          this.detailsLoadingSignal.set(false);
        })
      )
      .subscribe({
        next: (
          claim: Claim
        ) => {
          this.selectedClaimSignal.set(
            claim
          );

          this.upsertMemberClaim(
            claim
          );
        },
        error: (
          error: unknown
        ) => {
          this.errorMessageSignal.set(
            this.extractErrorMessage(
              error,
              'Unable to load the claim.'
            )
          );
        }
      });
  }

  selectProviderClaim(
    claimId: string
  ): void {

    const claim =
      this.providerClaimsSignal()
        .find(providerClaim => {
          return providerClaim.id === claimId;
        });

    this.selectedClaimSignal.set(
      claim ?? null
    );

    this.errorMessageSignal.set(null);
  }

  selectPayerClaim(
    claimId: string
  ): void {

    const claim =
      this.payerClaimsSignal()
        .find(payerClaim => {
          return payerClaim.id === claimId;
        });

    this.selectedClaimSignal.set(
      claim ?? null
    );

    this.errorMessageSignal.set(null);
  }

  selectMemberClaim(
    claimId: string
  ): void {

    const claim =
      this.memberClaimsSignal()
        .find(memberClaim => {
          return memberClaim.id === claimId;
        });

    this.selectedClaimSignal.set(
      claim ?? null
    );

    this.errorMessageSignal.set(null);
  }

  clearProviderClaims(): void {

    this.providerClaimsSignal.set([]);

    this.errorMessageSignal.set(null);
  }

  clearPayerClaims(): void {

    this.payerClaimsSignal.set([]);

    this.errorMessageSignal.set(null);
  }

  clearMemberClaims(): void {

    this.memberClaimsSignal.set([]);

    this.errorMessageSignal.set(null);
  }

  clearSelectedClaim(): void {

    this.selectedClaimSignal.set(null);

    this.errorMessageSignal.set(null);
  }

  clearSubmissionContext(): void {

    this.submissionContextSignal.set(null);

    this.errorMessageSignal.set(null);
  }

  clearError(): void {

    this.errorMessageSignal.set(null);
  }

  clearState(): void {

    this.providerClaimsSignal.set([]);

    this.payerClaimsSignal.set([]);

    this.memberClaimsSignal.set([]);

    this.selectedClaimSignal.set(null);

    this.submissionContextSignal.set(null);

    this.contextLoadingSignal.set(false);

    this.listLoadingSignal.set(false);

    this.detailsLoadingSignal.set(false);

    this.submittingSignal.set(false);

    this.cancellingSignal.set(false);

    this.reviewingSignal.set(false);

    this.approvingSignal.set(false);

    this.rejectingSignal.set(false);

    this.errorMessageSignal.set(null);
  }

  private upsertProviderClaim(
    claim: Claim
  ): void {

    this.providerClaimsSignal.update(
      currentClaims => {
        return this.upsertClaim(
          currentClaims,
          claim
        );
      }
    );
  }

  private upsertPayerClaim(
    claim: Claim
  ): void {

    this.payerClaimsSignal.update(
      currentClaims => {
        return this.upsertClaim(
          currentClaims,
          claim
        );
      }
    );
  }

  private upsertMemberClaim(
    claim: Claim
  ): void {

    this.memberClaimsSignal.update(
      currentClaims => {
        return this.upsertClaim(
          currentClaims,
          claim
        );
      }
    );
  }

  private upsertClaim(
    currentClaims: Claim[],
    updatedClaim: Claim
  ): Claim[] {

    const existingClaimIndex =
      currentClaims.findIndex(claim => {
        return claim.id === updatedClaim.id;
      });

    if (existingClaimIndex === -1) {
      return [
        updatedClaim,
        ...currentClaims
      ];
    }

    return currentClaims.map(claim => {
      return claim.id === updatedClaim.id
        ? updatedClaim
        : claim;
    });
  }

  private extractErrorMessage(
    error: unknown,
    fallbackMessage: string
  ): string {

    if (!(error instanceof HttpErrorResponse)) {
      return fallbackMessage;
    }

    if (
      typeof error.error === 'string'
      && error.error.trim().length > 0
    ) {
      return error.error.trim();
    }

    const backendMessage =
      this.readBackendMessage(
        error.error
      );

    if (backendMessage) {
      return backendMessage;
    }

    if (error.status === 0) {
      return 'Unable to connect to the server. '
        + 'Check your connection and try again.';
    }

    if (error.status === 400) {
      return 'The claim information is invalid. '
        + 'Review the information and try again.';
    }

    if (error.status === 401) {
      return 'Your session has expired. '
        + 'Please sign in again.';
    }

    if (error.status === 403) {
      return 'You are not authorized to perform '
        + 'this claim operation.';
    }

    if (error.status === 404) {
      return 'The requested claim or related '
        + 'healthcare information could not be found.';
    }

    if (error.status === 409) {
      return 'A claim already exists for this '
        + 'appointment or Medical Record.';
    }

    if (error.status === 422) {
      return 'The claim cannot be processed '
        + 'in its current state.';
    }

    if (error.status === 503) {
      return 'Claim verification is temporarily '
        + 'unavailable. Please try again later.';
    }

    return fallbackMessage;
  }

  private readBackendMessage(
    responseBody: unknown
  ): string | null {

    if (
      responseBody === null
      || typeof responseBody !== 'object'
    ) {
      return null;
    }

    const response =
      responseBody as {
        message?: unknown;
        detail?: unknown;
        error?: unknown;
      };

    if (
      typeof response.message === 'string'
      && response.message.trim().length > 0
    ) {
      return response.message.trim();
    }

    if (
      typeof response.detail === 'string'
      && response.detail.trim().length > 0
    ) {
      return response.detail.trim();
    }

    if (
      typeof response.error === 'string'
      && response.error.trim().length > 0
    ) {
      return response.error.trim();
    }

    return null;
  }
}