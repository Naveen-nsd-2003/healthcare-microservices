import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  Router,
  RouterLink
} from '@angular/router';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatCardModule
} from '@angular/material/card';

import {
  MatFormFieldModule
} from '@angular/material/form-field';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatInputModule
} from '@angular/material/input';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  AuthService
} from '../../../../core/auth/auth.service';

import {
  ForgotPasswordRequest
} from '../../../../core/models/auth/forgot-password-request';

import {
  ApiError
} from '../../../../core/models/api-error';

@Component({
  selector: 'app-forgot-password',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss'
})
export class ForgotPassword {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  readonly submitting =
    signal(false);

  readonly errorMessage =
    signal<string | null>(null);

  readonly successMessage =
    signal<string | null>(null);

  readonly forgotPasswordForm =
    this.formBuilder.nonNullable.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(150)
        ]
      ]
    });

  requestPasswordReset(): void {

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.forgotPasswordForm.invalid) {

      this.forgotPasswordForm.markAllAsTouched();

      return;
    }

    if (this.submitting()) {
      return;
    }

    const normalizedEmail =
      this.forgotPasswordForm
        .controls
        .email
        .getRawValue()
        .trim()
        .toLowerCase();

    const request: ForgotPasswordRequest = {
      email: normalizedEmail
    };

    this.submitting.set(true);

    this.authService
      .forgotPassword(request)
      .subscribe({
        next: message => {

          this.submitting.set(false);

          this.successMessage.set(
            message
          );

          setTimeout(
            () => {

              this.router.navigate(
                ['/reset-password'],
                {
                  queryParams: {
                    email: normalizedEmail,
                    otpSent: true
                  }
                }
              );
            },
            1200
          );
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.submitting.set(false);

          this.errorMessage.set(
            this.resolveErrorMessage(
              error
            )
          );
        }
      });
  }

  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 0) {

      return 'Unable to connect to the healthcare server. '
        + 'Please verify that the API Gateway is running.';
    }

    const apiError =
      error.error as ApiError | string | null;

    if (
      typeof apiError === 'string'
      && apiError.trim().length > 0
    ) {

      return apiError;
    }

    if (
      typeof apiError === 'object'
      && apiError?.message
      && apiError.message.trim().length > 0
    ) {

      return apiError.message;
    }

    if (error.status === 400) {

      return 'Enter a valid registered email address.';
    }

    if (error.status === 404) {

      return 'No account was found for the supplied email address.';
    }

    return 'Unable to send the password reset OTP. '
      + 'Please try again.';
  }
}