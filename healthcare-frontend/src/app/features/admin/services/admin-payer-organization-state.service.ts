import {
  Injectable,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  finalize
} from 'rxjs';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  PayerOrganization
} from '../../insurance/models/payer-organization';

import {
  AdminPayerOrganizationApiService
} from './admin-payer-organization-api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminPayerOrganizationState {

  private readonly api =
    inject(
      AdminPayerOrganizationApiService
    );

  private readonly organizationsState =
    signal<PayerOrganization[]>(
      []
    );

  private readonly organizationState =
    signal<PayerOrganization | null>(
      null
    );

  private readonly loadingState =
    signal(false);

  private readonly savingState =
    signal(false);

  private readonly errorState =
    signal<string | null>(
      null
    );

  readonly organizations =
    this.organizationsState.asReadonly();

  readonly organization =
    this.organizationState.asReadonly();

  readonly loading =
    this.loadingState.asReadonly();

  readonly saving =
    this.savingState.asReadonly();

  readonly errorMessage =
    this.errorState.asReadonly();

  readonly hasOrganization =
    computed(() => {
      return this.organizationState()
        !== null;
    });

  loadOrganizations(): void {

    this.loadingState.set(true);
    this.errorState.set(null);

    this.api
      .getAllPayers()
      .pipe(
        finalize(() => {
          this.loadingState.set(false);
        })
      )
      .subscribe({
        next: organizations => {

          this.organizationsState.set(
            organizations
          );
        },

        error: error => {

          this.handleError(
            error
          );
        }
      });
  }

  loadOrganization(
    payerId: string
  ): void {

    this.loadingState.set(true);
    this.errorState.set(null);

    this.api
      .getPayerById(
        payerId
      )
      .pipe(
        finalize(() => {
          this.loadingState.set(false);
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );
        },

        error: error => {

          this.handleError(
            error
          );
        }
      });
  }

  approve(
    payerId: string,
    reviewRemarks:
      string | null
  ): void {

    this.savingState.set(true);

    this.api
      .approvePayer(
        payerId,
        {
          reviewRemarks
        }
      )
      .pipe(
        finalize(() => {
          this.savingState.set(false);
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );
        },

        error: error => {

          this.handleError(
            error
          );
        }
      });
  }

  reject(
    payerId: string,
    rejectionReason: string,
    reviewRemarks:
      string | null
  ): void {

    this.savingState.set(true);

    this.api
      .rejectPayer(
        payerId,
        {
          rejectionReason,
          reviewRemarks
        }
      )
      .pipe(
        finalize(() => {
          this.savingState.set(false);
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );
        },

        error: error => {

          this.handleError(
            error
          );
        }
      });
  }

  updateStatus(
    payerId: string,
    status: string
  ): void {

    this.savingState.set(true);

    this.api
      .updateStatus(
        payerId,
        {
          status
        }
      )
      .pipe(
        finalize(() => {
          this.savingState.set(false);
        })
      )
      .subscribe({
        next: organization => {

          this.organizationState.set(
            organization
          );
        },

        error: error => {

          this.handleError(
            error
          );
        }
      });
  }

  clearError(): void {

    this.errorState.set(null);
  }

  clearState(): void {

    this.organizationsState.set([]);
    this.organizationState.set(null);
    this.loadingState.set(false);
    this.savingState.set(false);
    this.errorState.set(null);
  }

  private handleError(
    error: unknown
  ): void {

    if (
      error instanceof HttpErrorResponse
    ) {

      this.errorState.set(
        error.message
      );

      return;
    }

    this.errorState.set(
      'Unable to process request.'
    );
  }
}