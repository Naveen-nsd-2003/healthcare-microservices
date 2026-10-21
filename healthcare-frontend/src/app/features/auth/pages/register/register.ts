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
  ApiError
} from '../../../../core/models/api-error';

import {
  RegisterRequest
} from '../../../../core/models/auth/register-request';

@Component({
  selector: 'app-register',
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
  templateUrl: './register.html',
  styleUrl: './register.scss'
})
export class Register {

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

  readonly registrationForm =
    this.formBuilder.nonNullable.group({
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(50)
        ]
      ],

      firstName: [
        '',
        [
          Validators.required,
          Validators.maxLength(50)
        ]
      ],

      lastName: [
        '',
        [
          Validators.required,
          Validators.maxLength(50)
        ]
      ],

      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(150)
        ]
      ],

      phoneNumber: [
        '',
        [
          Validators.pattern(
            /^[0-9+()\- ]{10,20}$/
          )
        ]
      ]
    });

  register(): void {

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.registrationForm.invalid) {

      this.registrationForm.markAllAsTouched();

      return;
    }

    if (this.submitting()) {
      return;
    }

    const formValue =
      this.registrationForm.getRawValue();

    const normalizedEmail =
      formValue.email
        .trim()
        .toLowerCase();

    const normalizedPhoneNumber =
      formValue.phoneNumber.trim();

    const request: RegisterRequest = {
      username:
        formValue.username.trim(),

      firstName:
        formValue.firstName.trim(),

      lastName:
        formValue.lastName.trim(),

      email:
        normalizedEmail,

      phoneNumber:
        normalizedPhoneNumber.length > 0
          ? normalizedPhoneNumber
          : null
    };

    this.submitting.set(true);

    this.authService
      .register(request)
      .subscribe({
        next: message => {

          this.submitting.set(false);

          this.successMessage.set(
            message
          );

          setTimeout(
            () => {

              this.router.navigate(
                ['/verify-otp'],
                {
                  queryParams: {
                    email: normalizedEmail,
                    registrationSuccess: true
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

    if (error.status === 409) {

      return 'An account already exists with the supplied '
        + 'username or email address.';
    }

    if (error.status === 400) {

      return 'Please review the registration information '
        + 'and correct the invalid fields.';
    }

    return 'Registration failed. Please try again.';
  }
}