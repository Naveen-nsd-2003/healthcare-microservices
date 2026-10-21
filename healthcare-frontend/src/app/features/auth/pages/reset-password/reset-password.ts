import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  ActivatedRoute,
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
  ResetPasswordRequest
} from '../../../../core/models/auth/reset-password-request';

import {
  ApiError
} from '../../../../core/models/api-error';

@Component({
  selector: 'app-reset-password',
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
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss'
})
export class ResetPassword implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  readonly submitting =
    signal(false);

  readonly otpSent =
    signal(false);

  readonly newPasswordVisible =
    signal(false);

  readonly confirmPasswordVisible =
    signal(false);

  readonly errorMessage =
    signal<string | null>(null);

  readonly successMessage =
    signal<string | null>(null);

  readonly resetPasswordForm =
    this.formBuilder.nonNullable.group(
      {
        email: [
          '',
          [
            Validators.required,
            Validators.email,
            Validators.maxLength(150)
          ]
        ],

        otp: [
          '',
          [
            Validators.required,
            Validators.pattern(/^[0-9]{6}$/)
          ]
        ],

        newPassword: [
          '',
          [
            Validators.required,
            Validators.minLength(8)
          ]
        ],

        confirmPassword: [
          '',
          [
            Validators.required
          ]
        ]
      },
      {
        validators: [
          this.passwordsMatchValidator()
        ]
      }
    );

  ngOnInit(): void {

    const email =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('email');

    const otpSentValue =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('otpSent');

    this.otpSent.set(
      otpSentValue === 'true'
    );

    if (email) {

      this.resetPasswordForm
        .controls
        .email
        .setValue(
          email.trim().toLowerCase()
        );

      this.resetPasswordForm
        .controls
        .email
        .disable();
    }
  }

  toggleNewPasswordVisibility(): void {

    this.newPasswordVisible.update(
      visible => !visible
    );
  }

  toggleConfirmPasswordVisibility(): void {

    this.confirmPasswordVisible.update(
      visible => !visible
    );
  }

  resetPassword(): void {

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.resetPasswordForm.invalid) {

      this.resetPasswordForm.markAllAsTouched();

      return;
    }

    if (this.submitting()) {
      return;
    }

    const formValue =
      this.resetPasswordForm.getRawValue();

    const request: ResetPasswordRequest = {
      email:
        formValue.email
          .trim()
          .toLowerCase(),

      otp:
        formValue.otp.trim(),

      newPassword:
        formValue.newPassword
    };

    this.submitting.set(true);

    this.authService
      .resetPassword(request)
      .subscribe({
        next: message => {

          this.submitting.set(false);

          this.successMessage.set(
            message
          );

          setTimeout(
            () => {

              this.router.navigate(
                ['/login'],
                {
                  queryParams: {
                    passwordReset: true,
                    email: request.email
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

  private passwordsMatchValidator():
    ValidatorFn {

    return (
      control: AbstractControl
    ): ValidationErrors | null => {

      const newPassword =
        control.get('newPassword')?.value;

      const confirmPassword =
        control.get('confirmPassword')?.value;

      if (
        !newPassword
        || !confirmPassword
      ) {

        return null;
      }

      return newPassword === confirmPassword
        ? null
        : {
            passwordsDoNotMatch: true
          };
    };
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

      return 'The OTP is invalid or has expired. '
        + 'Please verify the OTP and password.';
    }

    return 'Unable to reset the password. '
      + 'Please try again.';
  }
}