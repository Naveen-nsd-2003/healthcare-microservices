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
  AdminDashboard
} from '../models/admin-dashboard';

import {
  CreateStaffRequest
} from '../models/create-staff-request';

import {
  AdminApiService
} from './admin-api.service';

type DashboardSuccessCallback =
  (dashboard: AdminDashboard) => void;

type AccountCreationSuccessCallback =
  (message: string) => void;

type ErrorCallback =
  (message: string) => void;

@Injectable({
  providedIn: 'root'
})
export class AdminStateService {

  private readonly adminApi =
    inject(AdminApiService);

  private readonly dashboardSignal =
    signal<AdminDashboard | null>(
      null
    );

  private readonly dashboardLoadingSignal =
    signal(false);

  private readonly providerCreatingSignal =
    signal(false);

  private readonly payerCreatingSignal =
    signal(false);

  private readonly errorMessageSignal =
    signal<string | null>(null);

  readonly dashboard =
    this.dashboardSignal.asReadonly();

  readonly dashboardLoading =
    this.dashboardLoadingSignal.asReadonly();

  readonly providerCreating =
    this.providerCreatingSignal.asReadonly();

  readonly payerCreating =
    this.payerCreatingSignal.asReadonly();

  readonly errorMessage =
    this.errorMessageSignal.asReadonly();

  readonly accountCreationInProgress =
    computed(() => {
      return this.providerCreatingSignal()
        || this.payerCreatingSignal();
    });

  readonly loading =
    computed(() => {
      return this.dashboardLoadingSignal()
        || this.providerCreatingSignal()
        || this.payerCreatingSignal();
    });

  loadDashboard(
    onSuccess?: DashboardSuccessCallback,
    onError?: ErrorCallback
  ): void {

    if (
      this.dashboardLoadingSignal()
    ) {
      return;
    }

    this.dashboardLoadingSignal.set(
      true
    );

    this.errorMessageSignal.set(
      null
    );

    this.adminApi
      .getDashboard()
      .pipe(
        finalize(() => {
          this.dashboardLoadingSignal.set(
            false
          );
        })
      )
      .subscribe({
        next: (
          dashboard: AdminDashboard
        ) => {
          this.dashboardSignal.set(
            dashboard
          );

          if (onSuccess) {
            onSuccess(dashboard);
          }
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to load the Admin dashboard.'
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

  createProvider(
    request: CreateStaffRequest,
    onSuccess?: AccountCreationSuccessCallback,
    onError?: ErrorCallback
  ): void {

    if (
      this.providerCreatingSignal()
    ) {
      return;
    }

    this.providerCreatingSignal.set(
      true
    );

    this.errorMessageSignal.set(
      null
    );

    this.adminApi
      .createProvider(request)
      .pipe(
        finalize(() => {
          this.providerCreatingSignal.set(
            false
          );
        })
      )
      .subscribe({
        next: (
          message: string
        ) => {
          if (onSuccess) {
            onSuccess(
              this.normalizeSuccessMessage(
                message,
                'Provider account created successfully.'
              )
            );
          }

          this.refreshDashboardAfterCreation();
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to create the Provider account.'
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

  createPayer(
    request: CreateStaffRequest,
    onSuccess?: AccountCreationSuccessCallback,
    onError?: ErrorCallback
  ): void {

    if (
      this.payerCreatingSignal()
    ) {
      return;
    }

    this.payerCreatingSignal.set(
      true
    );

    this.errorMessageSignal.set(
      null
    );

    this.adminApi
      .createPayer(request)
      .pipe(
        finalize(() => {
          this.payerCreatingSignal.set(
            false
          );
        })
      )
      .subscribe({
        next: (
          message: string
        ) => {
          if (onSuccess) {
            onSuccess(
              this.normalizeSuccessMessage(
                message,
                'Payer account created successfully.'
              )
            );
          }

          this.refreshDashboardAfterCreation();
        },
        error: (
          error: unknown
        ) => {
          const message =
            this.extractErrorMessage(
              error,
              'Unable to create the Payer account.'
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

  clearDashboard(): void {
    this.dashboardSignal.set(
      null
    );

    this.errorMessageSignal.set(
      null
    );
  }

  clearError(): void {
    this.errorMessageSignal.set(
      null
    );
  }

  clearState(): void {
    this.dashboardSignal.set(
      null
    );

    this.dashboardLoadingSignal.set(
      false
    );

    this.providerCreatingSignal.set(
      false
    );

    this.payerCreatingSignal.set(
      false
    );

    this.errorMessageSignal.set(
      null
    );
  }

  private refreshDashboardAfterCreation():
    void {

    this.dashboardSignal.set(
      null
    );

    this.loadDashboard();
  }

  private normalizeSuccessMessage(
    message: string | null | undefined,
    fallbackMessage: string
  ): string {

    if (
      typeof message === 'string'
      && message.trim().length > 0
    ) {
      return message.trim();
    }

    return fallbackMessage;
  }

  private extractErrorMessage(
    error: unknown,
    fallbackMessage: string
  ): string {

    if (
      !(error instanceof HttpErrorResponse)
    ) {
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
        + 'Check whether the Auth Service and '
        + 'API Gateway are running.';
    }

    if (error.status === 400) {
      return 'The account information is invalid. '
        + 'Review the form and try again.';
    }

    if (error.status === 401) {
      return 'Your Admin session has expired. '
        + 'Please sign in again.';
    }

    if (error.status === 403) {
      return 'Only an Administrator can perform '
        + 'this account-management operation.';
    }

    if (error.status === 404) {
      return 'The requested Admin resource '
        + 'could not be found.';
    }

    if (error.status === 409) {
      return 'An account already exists with the '
        + 'supplied username or email address.';
    }

    if (error.status >= 500) {
      return 'The account-management service is '
        + 'temporarily unavailable. Please try again.';
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