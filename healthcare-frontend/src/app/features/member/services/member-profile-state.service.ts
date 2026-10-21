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
  MemberProfile
} from '../models/member-profile';

import {
  MemberProfileRequest
} from '../models/member-profile-request';

import {
  MemberProfileApiService
} from './member-profile-api.service';

@Injectable({
  providedIn: 'root'
})
export class MemberProfileStateService {

  private readonly profileApi =
    inject(MemberProfileApiService);

  private readonly profileState =
    signal<MemberProfile | null>(null);

  private readonly loadingState =
    signal(false);

  private readonly submittingState =
    signal(false);

  private readonly profileMissingState =
    signal(false);

  private readonly errorMessageState =
    signal<string | null>(null);

  readonly profile =
    this.profileState.asReadonly();

  readonly loading =
    this.loadingState.asReadonly();

  readonly submitting =
    this.submittingState.asReadonly();

  readonly profileMissing =
    this.profileMissingState.asReadonly();

  readonly errorMessage =
    this.errorMessageState.asReadonly();

  readonly hasProfile =
    computed(
      () =>
        this.profileState() !== null
    );

  readonly profileIsActive =
    computed(
      () =>
        this.profileState()?.status === 'ACTIVE'
    );

  loadProfile(
    afterSuccess?: (
      profile: MemberProfile
    ) => void,
    afterMissing?: () => void
  ): void {

    this.loadingState.set(true);
    this.errorMessageState.set(null);
    this.profileMissingState.set(false);

    this.profileApi
      .getMyProfile()
      .pipe(
        finalize(
          () =>
            this.loadingState.set(false)
        )
      )
      .subscribe({
        next: profile => {

          this.profileState.set(profile);
          this.profileMissingState.set(false);
          this.errorMessageState.set(null);

          afterSuccess?.(profile);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          if (error.status === 404) {

            this.profileState.set(null);
            this.profileMissingState.set(true);
            this.errorMessageState.set(null);

            afterMissing?.();

            return;
          }

          this.profileState.set(null);
          this.profileMissingState.set(false);

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  createProfile(
    request: MemberProfileRequest,
    afterSuccess?: (
      profile: MemberProfile
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

    this.profileApi
      .createMyProfile(request)
      .pipe(
        finalize(
          () =>
            this.submittingState.set(false)
        )
      )
      .subscribe({
        next: profile => {

          this.profileState.set(profile);
          this.profileMissingState.set(false);
          this.errorMessageState.set(null);

          afterSuccess?.(profile);
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

  updateProfile(
    request: MemberProfileRequest,
    afterSuccess?: (
      profile: MemberProfile
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

    this.profileApi
      .updateMyProfile(request)
      .pipe(
        finalize(
          () =>
            this.submittingState.set(false)
        )
      )
      .subscribe({
        next: profile => {

          this.profileState.set(profile);
          this.profileMissingState.set(false);
          this.errorMessageState.set(null);

          afterSuccess?.(profile);
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

  clearError(): void {

    this.errorMessageState.set(null);
  }

  clearState(): void {

    this.profileState.set(null);
    this.loadingState.set(false);
    this.submittingState.set(false);
    this.profileMissingState.set(false);
    this.errorMessageState.set(null);
  }

  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 0) {

      return 'Unable to connect to the Member Service. '
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

      return 'Please review the profile information '
        + 'and correct the invalid fields.';
    }

    if (error.status === 401) {

      return 'Your session has expired. '
        + 'Please sign in again.';
    }

    if (error.status === 403) {

      return 'You are not authorized to manage '
        + 'this Member profile.';
    }

    if (error.status === 404) {

      return 'The Member profile was not found.';
    }

    if (error.status === 409) {

      return 'A Member profile already exists '
        + 'for this account.';
    }

    if (error.status >= 500) {

      return 'The Member Service is temporarily '
        + 'unavailable. Please try again.';
    }

    return 'Unable to process the Member profile. '
      + 'Please try again.';
  }
}