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
  CreatePayerOrganizationRequest
} from '../models/create-payer-organization-request';

import {
  PayerOrganization
} from '../models/payer-organization';

import {
  UpdatePayerOrganizationRequest
} from '../models/update-payer-organization-request';

import {
  PayerOrganizationApiService
} from './payer-organization-api.service';

@Injectable({
  providedIn: 'root'
})
export class PayerOrganizationStateService {

  private readonly organizationApi =
    inject(PayerOrganizationApiService);

  private readonly organizationState =
    signal<PayerOrganization | null>(
      null
    );

  private readonly organizationNotCreatedState =
    signal(false);

  private readonly loadingState =
    signal(false);

  private readonly creatingState =
    signal(false);

  private readonly updatingState =
    signal(false);

  private readonly errorMessageState =
    signal<string | null>(
      null
    );

  readonly organization =
    this.organizationState.asReadonly();

  readonly organizationNotCreated =
    this.organizationNotCreatedState
      .asReadonly();

  readonly loading =
    this.loadingState.asReadonly();

  readonly creating =
    this.creatingState.asReadonly();

  readonly updating =
    this.updatingState.asReadonly();

  readonly errorMessage =
    this.errorMessageState.asReadonly();

  readonly hasOrganization =
    computed(() => {
      return this.organizationState() !== null;
    });

  readonly organizationIsPending =
    computed(() => {
      return this.organizationState()?.status
        === 'PENDING_APPROVAL';
    });

  readonly organizationIsActive =
    computed(() => {
      return this.organizationState()?.status
        === 'ACTIVE';
    });

  readonly organizationIsRejected =
    computed(() => {
      return this.organizationState()?.status
        === 'REJECTED';
    });

  readonly organizationIsSuspended =
    computed(() => {
      return this.organizationState()?.status
        === 'SUSPENDED';
    });

  readonly organizationIsInactive =
    computed(() => {
      return this.organizationState()?.status
        === 'INACTIVE';
    });

  readonly saving =
    computed(() => {
      return this.creatingState()
        || this.updatingState();
    });

  loadMyOrganization(
    afterSuccess?: (
      organization: PayerOrganization
    ) => void,
    afterNotFound?: () => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.loadingState()) {
      return;
    }

    this.loadingState.set(
      true
    );

    this.errorMessageState.set(
      null
    );

    this.organizationApi
      .getMyOrganization()
      .pipe(
        finalize(() => {
          this.loadingState.set(
            false
          );
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );

          this.organizationNotCreatedState.set(
            false
          );

          this.errorMessageState.set(
            null
          );

          afterSuccess?.(
            organization
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.organizationState.set(
            null
          );

          if (error.status === 404) {

            this.organizationNotCreatedState.set(
              true
            );

            this.errorMessageState.set(
              null
            );

            afterNotFound?.();

            return;
          }

          this.organizationNotCreatedState.set(
            false
          );

          const message =
            this.resolveErrorMessage(
              error
            );

          this.errorMessageState.set(
            message
          );

          afterError?.(
            message
          );
        }
      });
  }

  createMyOrganization(
    request: CreatePayerOrganizationRequest,
    afterSuccess?: (
      organization: PayerOrganization
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.saving()) {
      return;
    }

    if (this.hasOrganization()) {

      const message =
        'A Payer organization is already '
        + 'associated with this account.';

      this.errorMessageState.set(
        message
      );

      afterError?.(
        message
      );

      return;
    }

    this.creatingState.set(
      true
    );

    this.errorMessageState.set(
      null
    );

    this.organizationApi
      .createMyOrganization(
        request
      )
      .pipe(
        finalize(() => {
          this.creatingState.set(
            false
          );
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );

          this.organizationNotCreatedState.set(
            false
          );

          this.errorMessageState.set(
            null
          );

          afterSuccess?.(
            organization
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(
              error
            );

          this.errorMessageState.set(
            message
          );

          afterError?.(
            message
          );
        }
      });
  }

  updateMyOrganization(
    request: UpdatePayerOrganizationRequest,
    afterSuccess?: (
      organization: PayerOrganization
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.saving()) {
      return;
    }

    if (!this.hasOrganization()) {

      const message =
        'Submit the Payer organization '
        + 'before attempting to update it.';

      this.errorMessageState.set(
        message
      );

      afterError?.(
        message
      );

      return;
    }

    this.updatingState.set(
      true
    );

    this.errorMessageState.set(
      null
    );

    this.organizationApi
      .updateMyOrganization(
        request
      )
      .pipe(
        finalize(() => {
          this.updatingState.set(
            false
          );
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );

          this.organizationNotCreatedState.set(
            false
          );

          this.errorMessageState.set(
            null
          );

          afterSuccess?.(
            organization
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(
              error
            );

          this.errorMessageState.set(
            message
          );

          afterError?.(
            message
          );
        }
      });
  }

  clearError(): void {

    this.errorMessageState.set(
      null
    );
  }

  clearState(): void {

    this.organizationState.set(
      null
    );

    this.organizationNotCreatedState.set(
      false
    );

    this.loadingState.set(
      false
    );

    this.creatingState.set(
      false
    );

    this.updatingState.set(
      false
    );

    this.errorMessageState.set(
      null
    );
  }

  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 0) {

      return 'Unable to connect to the Payers Service. '
        + 'Please verify that the service is running.';
    }

    const backendMessage =
      this.extractBackendMessage(
        error.error
      );

    if (backendMessage) {
      return backendMessage;
    }

    if (error.status === 400) {

      return 'Please review the Payer organization '
        + 'information and correct the invalid fields.';
    }

    if (error.status === 401) {

      return 'Your session has expired. '
        + 'Please sign in again.';
    }

    if (error.status === 403) {

      return 'You are not authorized to manage '
        + 'this Payer organization.';
    }

    if (error.status === 404) {

      return 'The requested Payer organization '
        + 'could not be found.';
    }

    if (error.status === 409) {

      return 'A Payer organization already exists '
        + 'for this account, or the registration '
        + 'number is already in use.';
    }

    if (error.status >= 500) {

      return 'The Payers Service is temporarily '
        + 'unavailable. Please try again.';
    }

    return 'Unable to process the Payer organization. '
      + 'Please try again.';
  }

  private extractBackendMessage(
    backendError: unknown
  ): string | null {

    if (typeof backendError === 'string') {

      const normalizedMessage =
        backendError.trim();

      return normalizedMessage.length > 0
        ? normalizedMessage
        : null;
    }

    if (
      backendError === null
      || typeof backendError !== 'object'
    ) {

      return null;
    }

    if (
      'message' in backendError
      && typeof backendError.message === 'string'
    ) {

      const normalizedMessage =
        backendError.message.trim();

      if (normalizedMessage.length > 0) {
        return normalizedMessage;
      }
    }

    if (
      'detail' in backendError
      && typeof backendError.detail === 'string'
    ) {

      const normalizedDetail =
        backendError.detail.trim();

      if (normalizedDetail.length > 0) {
        return normalizedDetail;
      }
    }

    return null;
  }
}