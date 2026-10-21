import {
  Component,
  inject,
  OnInit,
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
  VerifyOtpRequest
} from '../../../../core/models/auth/verify-otp-request';

import {
  ApiError
} from '../../../../core/models/api-error';

@Component({
  selector: 'app-verify-otp',
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
  templateUrl: './verify-otp.html',
  styleUrl: './verify-otp.scss'
})
export class VerifyOtp implements OnInit {

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

  readonly errorMessage =
    signal<string | null>(null);

  readonly successMessage =
    signal<string | null>(null);

  readonly registrationSuccess =
    signal(false);

  readonly otpForm =
    this.formBuilder.nonNullable.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],

      otp: [
        '',
        [
          Validators.required,
          Validators.pattern(/^[0-9]{6}$/)
        ]
      ]
    });

  ngOnInit(): void {

    const email =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('email');

    const registrationSuccessValue =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('registrationSuccess');

    this.registrationSuccess.set(
      registrationSuccessValue === 'true'
    );

    if (email) {

      this.otpForm.controls.email.setValue(
        email.trim().toLowerCase()
      );

      this.otpForm.controls.email.disable();
    }
  }

  verifyOtp(): void {

    this.errorMessage.set(null);
    this.successMessage.set(null);

    if (this.otpForm.invalid) {

      this.otpForm.markAllAsTouched();

      return;
    }

    if (this.submitting()) {
      return;
    }

    const formValue =
      this.otpForm.getRawValue();

    const request: VerifyOtpRequest = {
      email:
        formValue.email
          .trim()
          .toLowerCase(),

      otp:
        formValue.otp.trim()
    };

    this.submitting.set(true);

    this.authService
      .verifyOtp(request)
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
                    otpVerified: true,
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
        + 'Please enter the latest OTP.';
    }

    return 'OTP verification failed. Please try again.';
  }
}