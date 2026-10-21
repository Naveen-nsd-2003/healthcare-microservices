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
} from 'rxjs';

import {
  InsurancePolicy
} from '../models/insurance-policy';

import {
  PolicyStatus
} from '../models/policy-status';

import {
  UpdatePolicyStatusRequest
} from '../models/update-policy-status-request';

import {
  PolicyApiService
} from './policy-api.service';

@Injectable({
  providedIn: 'root'
})
export class PolicyStateService {

  private readonly policyApi =
    inject(PolicyApiService);

  private readonly policiesState =
    signal<InsurancePolicy[]>([]);

  private readonly selectedPolicyState =
    signal<InsurancePolicy | null>(null);

  private readonly loadingState =
    signal(false);

  private readonly detailsLoadingState =
    signal(false);

  private readonly updatingState =
    signal(false);

  private readonly errorMessageState =
    signal<string | null>(null);

  readonly policies =
    this.policiesState.asReadonly();

  readonly selectedPolicy =
    this.selectedPolicyState.asReadonly();

  readonly loading =
    this.loadingState.asReadonly();

  readonly detailsLoading =
    this.detailsLoadingState.asReadonly();

  readonly updating =
    this.updatingState.asReadonly();

  readonly errorMessage =
    this.errorMessageState.asReadonly();

  readonly totalCount =
    computed(
      () =>
        this.policiesState().length
    );

  readonly activeCount =
    computed(
      () =>
        this.countPoliciesByStatus(
          'ACTIVE'
        )
    );

  readonly suspendedCount =
    computed(
      () =>
        this.countPoliciesByStatus(
          'SUSPENDED'
        )
    );

  readonly expiredCount =
    computed(
      () =>
        this.countPoliciesByStatus(
          'EXPIRED'
        )
    );

  readonly cancelledCount =
    computed(
      () =>
        this.countPoliciesByStatus(
          'CANCELLED'
        )
    );

  loadMyPolicies(): void {

    if (this.loadingState()) {
      return;
    }

    this.loadingState.set(true);
    this.errorMessageState.set(null);

    this.policyApi
      .getMyPolicies()
      .pipe(
        finalize(
          () => {
            this.loadingState.set(false);
          }
        )
      )
      .subscribe({
        next: policies => {

          this.policiesState.set(
            this.sortPolicies(policies)
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadMyPolicyById(
    policyId: string
  ): void {

    if (
      this.detailsLoadingState()
    ) {
      return;
    }

    this.detailsLoadingState.set(true);
    this.errorMessageState.set(null);
    this.selectedPolicyState.set(null);

    this.policyApi
      .getMyPolicyById(policyId)
      .pipe(
        finalize(
          () => {
            this.detailsLoadingState.set(false);
          }
        )
      )
      .subscribe({
        next: policy => {

          this.selectedPolicyState.set(
            policy
          );

          this.updatePolicyInCollection(
            policy
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.selectedPolicyState.set(null);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadPayerPolicies(): void {

    if (this.loadingState()) {
      return;
    }

    this.loadingState.set(true);
    this.errorMessageState.set(null);

    this.policyApi
      .getPayerPolicies()
      .pipe(
        finalize(
          () => {
            this.loadingState.set(false);
          }
        )
      )
      .subscribe({
        next: policies => {

          this.policiesState.set(
            this.sortPolicies(policies)
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadPayerPolicyById(
    policyId: string
  ): void {

    if (
      this.detailsLoadingState()
    ) {
      return;
    }

    this.detailsLoadingState.set(true);
    this.errorMessageState.set(null);
    this.selectedPolicyState.set(null);

    this.policyApi
      .getPayerPolicyById(policyId)
      .pipe(
        finalize(
          () => {
            this.detailsLoadingState.set(false);
          }
        )
      )
      .subscribe({
        next: policy => {

          this.selectedPolicyState.set(
            policy
          );

          this.updatePolicyInCollection(
            policy
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.selectedPolicyState.set(null);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  updatePolicyStatus(
    policyId: string,
    status: PolicyStatus,
    afterSuccess?: (
      policy: InsurancePolicy
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.updatingState()) {
      return;
    }

    const request:
      UpdatePolicyStatusRequest = {
        status
      };

    this.updatingState.set(true);
    this.errorMessageState.set(null);

    this.policyApi
      .updatePolicyStatus(
        policyId,
        request
      )
      .pipe(
        finalize(
          () => {
            this.updatingState.set(false);
          }
        )
      )
      .subscribe({
        next: policy => {

          this.selectedPolicyState.set(
            policy
          );

          this.updatePolicyInCollection(
            policy
          );

          this.errorMessageState.set(null);

          afterSuccess?.(policy);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(error);

          this.errorMessageState.set(
            message
          );

          afterError?.(message);
        }
      });
  }

  clearSelectedPolicy(): void {

    this.selectedPolicyState.set(null);
    this.errorMessageState.set(null);
  }

  clearError(): void {

    this.errorMessageState.set(null);
  }

  clearState(): void {

    this.policiesState.set([]);
    this.selectedPolicyState.set(null);
    this.loadingState.set(false);
    this.detailsLoadingState.set(false);
    this.updatingState.set(false);
    this.errorMessageState.set(null);
  }

  private countPoliciesByStatus(
    status: PolicyStatus
  ): number {

    return this.policiesState()
      .filter(
        policy =>
          policy.status === status
      )
      .length;
  }

  private updatePolicyInCollection(
    updatedPolicy: InsurancePolicy
  ): void {

    this.policiesState.update(
      policies => {

        const policyExists =
          policies.some(
            policy =>
              policy.id === updatedPolicy.id
          );

        if (!policyExists) {

          return this.sortPolicies([
            updatedPolicy,
            ...policies
          ]);
        }

        const updatedPolicies =
          policies.map(
            policy =>
              policy.id === updatedPolicy.id
                ? updatedPolicy
                : policy
          );

        return this.sortPolicies(
          updatedPolicies
        );
      }
    );
  }

  private sortPolicies(
    policies: InsurancePolicy[]
  ): InsurancePolicy[] {

    const statusOrder:
      Record<PolicyStatus, number> = {
        ACTIVE: 0,
        SUSPENDED: 1,
        EXPIRED: 2,
        CANCELLED: 3
      };

    return [...policies].sort(
      (
        firstPolicy,
        secondPolicy
      ) => {

        const statusDifference =
          statusOrder[firstPolicy.status]
          - statusOrder[secondPolicy.status];

        if (
          statusDifference !== 0
        ) {

          return statusDifference;
        }

        const firstCreatedAt =
          new Date(
            firstPolicy.createdAt
          ).getTime();

        const secondCreatedAt =
          new Date(
            secondPolicy.createdAt
          ).getTime();

        return secondCreatedAt
          - firstCreatedAt;
      }
    );
  }

  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 0) {

      return 'Unable to connect to the Payers Service. '
        + 'Please verify that the service is running.';
    }

    const backendError =
      error.error;

    if (
      typeof backendError === 'string'
      && backendError.trim().length > 0
    ) {

      return backendError;
    }

    if (
      backendError !== null
      && typeof backendError === 'object'
      && 'message' in backendError
      && typeof backendError.message === 'string'
      && backendError.message.trim().length > 0
    ) {

      return backendError.message;
    }

    if (error.status === 400) {

      return 'The requested policy operation '
        + 'is not valid.';
    }

    if (error.status === 401) {

      return 'Your session has expired. '
        + 'Please sign in again.';
    }

    if (error.status === 403) {

      return 'You are not authorized to access '
        + 'this insurance policy.';
    }

    if (error.status === 404) {

      return 'The requested insurance policy '
        + 'could not be found.';
    }

    if (error.status === 409) {

      return 'The policy status cannot be changed '
        + 'from its current state.';
    }

    if (error.status >= 500) {

      return 'Policy information is temporarily '
        + 'unavailable. Please try again.';
    }

    return 'Unable to process the insurance policy. '
      + 'Please try again.';
  }
}