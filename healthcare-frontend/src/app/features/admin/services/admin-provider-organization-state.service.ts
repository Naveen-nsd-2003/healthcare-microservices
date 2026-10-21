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
  ProviderOrganization
} from '../../provider/models/provider-organization';

import {
  AdminProviderOrganizationApiService
} from './admin-provider-organization-api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminProviderOrganizationState {

  private readonly api =
    inject(
      AdminProviderOrganizationApiService
    );

  private readonly organizationsState =
    signal<ProviderOrganization[]>(
      []
    );

  private readonly organizationState =
    signal<ProviderOrganization | null>(
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
      .getAllProviders()
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
    providerId: string
  ): void {

    this.loadingState.set(true);
    this.errorState.set(null);

    this.api
      .getProviderById(
        providerId
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
    providerId: string,
    reviewRemarks:
      string | null
  ): void {

    this.savingState.set(true);

    this.api
      .approveProvider(
        providerId,
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
    providerId: string,
    rejectionReason: string,
    reviewRemarks:
      string | null
  ): void {

    this.savingState.set(true);

    this.api
      .rejectProvider(
        providerId,
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
    providerId: string,
    status: string
  ): void {

    this.savingState.set(true);

    this.api
      .updateStatus(
        providerId,
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
