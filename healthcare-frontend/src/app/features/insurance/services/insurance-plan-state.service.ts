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
  CreateInsurancePlanRequest
} from '../models/create-insurance-plan-request';

import {
  InsurancePlan
} from '../models/insurance-plan';

import {
  InsurancePlanStatus
} from '../models/insurance-plan-status';

import {
  UpdateInsurancePlanRequest
} from '../models/update-insurance-plan-request';

import {
  UpdateInsurancePlanStatusRequest
} from '../models/update-insurance-plan-status-request';

import {
  InsurancePlanApiService
} from './insurance-plan-api.service';

@Injectable({
  providedIn: 'root'
})
export class InsurancePlanStateService {

  private readonly planApi =
    inject(InsurancePlanApiService);

  private readonly plansState =
    signal<InsurancePlan[]>([]);

  private readonly selectedPlanState =
    signal<InsurancePlan | null>(null);

  private readonly loadingState =
    signal(false);

  private readonly detailsLoadingState =
    signal(false);

  private readonly submittingState =
    signal(false);

  private readonly updatingState =
    signal(false);

  private readonly statusUpdatingState =
    signal(false);

  private readonly errorMessageState =
    signal<string | null>(null);

  readonly plans =
    this.plansState.asReadonly();

  readonly selectedPlan =
    this.selectedPlanState.asReadonly();

  readonly loading =
    this.loadingState.asReadonly();

  readonly detailsLoading =
    this.detailsLoadingState.asReadonly();

  readonly submitting =
    this.submittingState.asReadonly();

  readonly updating =
    this.updatingState.asReadonly();

  readonly statusUpdating =
    this.statusUpdatingState.asReadonly();

  readonly errorMessage =
    this.errorMessageState.asReadonly();

  readonly availablePlanCount =
    computed(() => {
      return this.plansState().length;
    });

  readonly payerOrganizationCount =
    computed(() => {

      const payerIds =
        this.plansState()
          .map(plan => plan.payerId);

      return new Set(payerIds).size;
    });

  readonly totalCount =
    computed(() => {
      return this.plansState().length;
    });

  readonly draftCount =
    computed(() => {
      return this.countPlansByStatus('DRAFT');
    });

  readonly activeCount =
    computed(() => {
      return this.countPlansByStatus('ACTIVE');
    });

  readonly inactiveCount =
    computed(() => {
      return this.countPlansByStatus('INACTIVE');
    });

  loadAvailablePlans(): void {

    if (this.loadingState()) {
      return;
    }

    this.loadingState.set(true);
    this.errorMessageState.set(null);

    this.planApi
      .getAvailablePlans()
      .pipe(
        finalize(() => {
          this.loadingState.set(false);
        })
      )
      .subscribe({
        next: plans => {

          const sortedPlans =
            [...plans].sort(
              (firstPlan, secondPlan) => {
                return firstPlan.monthlyPremium
                  - secondPlan.monthlyPremium;
              }
            );

          this.plansState.set(sortedPlans);
          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.plansState.set([]);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadAvailablePlanById(
    planId: string,
    afterSuccess?: (
      plan: InsurancePlan
    ) => void
  ): void {

    if (this.detailsLoadingState()) {
      return;
    }

    this.detailsLoadingState.set(true);
    this.errorMessageState.set(null);
    this.selectedPlanState.set(null);

    this.planApi
      .getAvailablePlanById(planId)
      .pipe(
        finalize(() => {
          this.detailsLoadingState.set(false);
        })
      )
      .subscribe({
        next: plan => {

          this.selectedPlanState.set(plan);
          this.errorMessageState.set(null);

          afterSuccess?.(plan);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.selectedPlanState.set(null);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadMyPlans(): void {

    if (this.loadingState()) {
      return;
    }

    this.loadingState.set(true);
    this.errorMessageState.set(null);

    this.planApi
      .getMyPlans()
      .pipe(
        finalize(() => {
          this.loadingState.set(false);
        })
      )
      .subscribe({
        next: plans => {

          this.plansState.set(
            this.sortPayerPlans(plans)
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.plansState.set([]);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadMyPlanById(
    planId: string,
    afterSuccess?: (
      plan: InsurancePlan
    ) => void
  ): void {

    if (this.detailsLoadingState()) {
      return;
    }

    this.detailsLoadingState.set(true);
    this.errorMessageState.set(null);
    this.selectedPlanState.set(null);

    this.planApi
      .getMyPlanById(planId)
      .pipe(
        finalize(() => {
          this.detailsLoadingState.set(false);
        })
      )
      .subscribe({
        next: plan => {

          this.selectedPlanState.set(plan);

          this.updatePlanInCollection(plan);

          this.errorMessageState.set(null);

          afterSuccess?.(plan);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.selectedPlanState.set(null);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  createPlan(
    request: CreateInsurancePlanRequest,
    afterSuccess?: (
      plan: InsurancePlan
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.submittingState()) {
      return;
    }

    this.submittingState.set(true);
    this.errorMessageState.set(null);

    this.planApi
      .createPlan(request)
      .pipe(
        finalize(() => {
          this.submittingState.set(false);
        })
      )
      .subscribe({
        next: plan => {

          this.selectedPlanState.set(plan);

          this.updatePlanInCollection(plan);

          this.errorMessageState.set(null);

          afterSuccess?.(plan);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(error);

          this.errorMessageState.set(message);

          afterError?.(message);
        }
      });
  }

  updateMyPlan(
    planId: string,
    request: UpdateInsurancePlanRequest,
    afterSuccess?: (
      plan: InsurancePlan
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.updatingState()) {
      return;
    }

    this.updatingState.set(true);
    this.errorMessageState.set(null);

    this.planApi
      .updateMyPlan(
        planId,
        request
      )
      .pipe(
        finalize(() => {
          this.updatingState.set(false);
        })
      )
      .subscribe({
        next: plan => {

          this.selectedPlanState.set(plan);

          this.updatePlanInCollection(plan);

          this.errorMessageState.set(null);

          afterSuccess?.(plan);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(error);

          this.errorMessageState.set(message);

          afterError?.(message);
        }
      });
  }

  updateMyPlanStatus(
    planId: string,
    status: InsurancePlanStatus,
    afterSuccess?: (
      plan: InsurancePlan
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.statusUpdatingState()) {
      return;
    }

    const request:
      UpdateInsurancePlanStatusRequest = {
        status
      };

    this.statusUpdatingState.set(true);
    this.errorMessageState.set(null);

    this.planApi
      .updateMyPlanStatus(
        planId,
        request
      )
      .pipe(
        finalize(() => {
          this.statusUpdatingState.set(false);
        })
      )
      .subscribe({
        next: plan => {

          this.selectedPlanState.set(plan);

          this.updatePlanInCollection(plan);

          this.errorMessageState.set(null);

          afterSuccess?.(plan);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(error);

          this.errorMessageState.set(message);

          afterError?.(message);
        }
      });
  }

  clearSelectedPlan(): void {

    this.selectedPlanState.set(null);
    this.errorMessageState.set(null);
  }

  clearError(): void {

    this.errorMessageState.set(null);
  }

  clearState(): void {

    this.plansState.set([]);
    this.selectedPlanState.set(null);
    this.loadingState.set(false);
    this.detailsLoadingState.set(false);
    this.submittingState.set(false);
    this.updatingState.set(false);
    this.statusUpdatingState.set(false);
    this.errorMessageState.set(null);
  }

  private countPlansByStatus(
    status: InsurancePlanStatus
  ): number {

    return this.plansState()
      .filter(plan => {
        return plan.status === status;
      })
      .length;
  }

  private updatePlanInCollection(
    updatedPlan: InsurancePlan
  ): void {

    this.plansState.update(
      currentPlans => {

        const planExists =
          currentPlans.some(plan => {
            return plan.id === updatedPlan.id;
          });

        if (!planExists) {

          return this.sortPayerPlans([
            updatedPlan,
            ...currentPlans
          ]);
        }

        const updatedPlans =
          currentPlans.map(plan => {

            if (plan.id === updatedPlan.id) {
              return updatedPlan;
            }

            return plan;
          });

        return this.sortPayerPlans(
          updatedPlans
        );
      }
    );
  }

  private sortPayerPlans(
    plans: InsurancePlan[]
  ): InsurancePlan[] {

    const statusOrder:
      Record<InsurancePlanStatus, number> = {
        DRAFT: 0,
        ACTIVE: 1,
        INACTIVE: 2
      };

    return [...plans].sort(
      (
        firstPlan,
        secondPlan
      ) => {

        const statusDifference =
          statusOrder[firstPlan.status]
          - statusOrder[secondPlan.status];

        if (statusDifference !== 0) {
          return statusDifference;
        }

        const firstCreatedTime =
          this.getDateValue(
            firstPlan.createdAt
          );

        const secondCreatedTime =
          this.getDateValue(
            secondPlan.createdAt
          );

        return secondCreatedTime
          - firstCreatedTime;
      }
    );
  }

  private getDateValue(
    dateValue: string
  ): number {

    const timestamp =
      new Date(dateValue).getTime();

    if (Number.isNaN(timestamp)) {
      return 0;
    }

    return timestamp;
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

    if (
      backendError !== null
      && typeof backendError === 'object'
      && 'detail' in backendError
      && typeof backendError.detail === 'string'
      && backendError.detail.trim().length > 0
    ) {

      return backendError.detail;
    }

    if (error.status === 400) {

      return 'Please review the insurance plan '
        + 'information and correct the invalid fields.';
    }

    if (error.status === 401) {

      return 'Your session has expired. '
        + 'Please sign in again.';
    }

    if (error.status === 403) {

      return 'You are not authorized to manage '
        + 'insurance plans.';
    }

    if (error.status === 404) {

      return 'The requested insurance plan '
        + 'could not be found.';
    }

    if (error.status === 409) {

      return 'The insurance plan cannot be processed '
        + 'because the plan code already exists or '
        + 'the current plan status does not allow '
        + 'this operation.';
    }

    if (error.status >= 500) {

      return 'Insurance plan management is temporarily '
        + 'unavailable. Please try again.';
    }

    return 'Unable to process the insurance plan. '
      + 'Please try again.';
  }
}