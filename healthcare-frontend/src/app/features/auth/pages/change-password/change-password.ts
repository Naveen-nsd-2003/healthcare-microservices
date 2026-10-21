import {
  Component,
  inject,
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
  ChangePasswordRequest
} from '../../../../core/models/auth/change-password-request';

import {
  ApiError
} from '../../../../core/models/api-error';

@Component({
  selector: 'app-change-password',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './change-password.html',
  styleUrl: './change-password.scss'
})
export class ChangePassword {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  readonly submitting =
    signal(false);

  readonly currentPasswordVisible =
    signal(false);

  readonly newPasswordVisible =
    signal(false);

  readonly confirmPasswordVisible =
    signal(false);

  readonly errorMessage =
    signal<string | null>(null);

  readonly successMessage =
    signal<string | null>(null);

  readonly passwordForm =
    this.formBuilder.nonNullable.group(
      {
        currentPassword: [
          '',
          [
            Validators.required
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
          this.passwordsMatchValidator(),
          this.passwordMustChangeValidator()
        ]
      }
    );

  toggleCurrentPasswordVisibility(): void {

    this.currentPasswordVisible.update(
      visible => !visible
    );
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

  changePassword(): void {

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.passwordForm.invalid) {

      this.passwordForm.markAllAsTouched();

      return;
    }

    if (this.submitting()) {
      return;
    }

    const formValue =
      this.passwordForm.getRawValue();

    const request: ChangePasswordRequest = {
      currentPassword:
        formValue.currentPassword,

      newPassword:
        formValue.newPassword
    };

    this.submitting.set(true);

    this.authService
      .changePassword(request)
      .subscribe({
        next: message => {

          this.submitting.set(false);

          this.successMessage.set(
            message
          );

          setTimeout(
            () => {

              this.authService
                .completePasswordChange();
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

  private passwordMustChangeValidator():
    ValidatorFn {

    return (
      control: AbstractControl
    ): ValidationErrors | null => {

      const currentPassword =
        control.get('currentPassword')?.value;

      const newPassword =
        control.get('newPassword')?.value;

      if (
        !currentPassword
        || !newPassword
      ) {

        return null;
      }

      return currentPassword !== newPassword
        ? null
        : {
            passwordNotChanged: true
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

    if (
      error.status === 401
      || error.status === 403
    ) {

      return 'The current password is incorrect '
        + 'or the temporary session has expired.';
    }

    if (error.status === 400) {

      return 'Please review the password information '
        + 'and correct the invalid fields.';
    }

    return 'Unable to change the password. '
      + 'Please try again.';
  }
}