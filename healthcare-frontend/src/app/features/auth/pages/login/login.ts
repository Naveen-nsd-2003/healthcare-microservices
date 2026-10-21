import {
  Component,
  computed,
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
  LoginRequest
} from '../../../../core/models/login-request';
import { Router,RouterLink } from '@angular/router';
@Component({
  selector: 'app-login',
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
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  readonly submitting =
    signal(false);

  readonly passwordVisible =
    signal(false);

  readonly errorMessage =
    signal<string | null>(null);

  readonly loginForm =
    this.formBuilder.nonNullable.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],
      password: [
        '',
        [
          Validators.required
        ]
      ]
    });

  readonly canSubmit =
    computed(
      () => !this.submitting()
    );

  togglePasswordVisibility(): void {

    this.passwordVisible.update(
      visible => !visible
    );
  }

  login(): void {

    this.errorMessage.set(null);

    if (this.loginForm.invalid) {

      this.loginForm.markAllAsTouched();

      return;
    }

    if (this.submitting()) {
      return;
    }

    const request: LoginRequest = {
      email:
        this.loginForm.controls.email
          .getRawValue()
          .trim()
          .toLowerCase(),

      password:
        this.loginForm.controls.password
          .getRawValue()
    };

    this.submitting.set(true);

    this.authService
      .login(request)
      .subscribe({
        next: response => {

  this.submitting.set(false);

  if (response.firstLogin) {

    this.router.navigate([
      '/change-password'
    ]);

    return;
  }

  this.authService
    .navigateToRoleDashboard(
      response.role
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
      error.error as ApiError | null;

    if (
      apiError?.message
      && apiError.message.trim().length > 0
    ) {

      return apiError.message;
    }

    if (
      error.status === 401
      || error.status === 403
    ) {

      return 'The email or password is incorrect.';
    }

    return 'Login failed. Please try again.';
  }
}