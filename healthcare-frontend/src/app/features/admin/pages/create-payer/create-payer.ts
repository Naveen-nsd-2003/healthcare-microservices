import {
  Component,
  OnDestroy,
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
  Router
} from '@angular/router';

import {
  MatButtonModule
} from '@angular/material/button';

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
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  CreateStaffRequest
} from '../../models/create-staff-request';

import {
  AdminStateService
} from '../../services/admin-state.service';

@Component({
  selector: 'app-create-payer',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './create-payer.html',
  styleUrl: './create-payer.scss'
})
export class CreatePayer
  implements OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly adminState =
    inject(AdminStateService);

  readonly accountCreated =
    signal(false);

  readonly createdAccountEmail =
    signal('');

  readonly createdAccountName =
    signal('');

  readonly payerForm =
    this.formBuilder.nonNullable.group({
      username: [
        '',
        [
          Validators.required,
          Validators.minLength(4),
          Validators.maxLength(50),
          this.nonWhitespaceValidator()
        ]
      ],
      firstName: [
        '',
        [
          Validators.required,
          Validators.maxLength(50),
          this.nonWhitespaceValidator()
        ]
      ],
      lastName: [
        '',
        [
          Validators.required,
          Validators.maxLength(50),
          this.nonWhitespaceValidator()
        ]
      ],
      email: [
        '',
        [
          Validators.required,
          Validators.email,
          Validators.maxLength(150),
          this.nonWhitespaceValidator()
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

  ngOnDestroy(): void {
    this.adminState.clearError();
  }

  createPayer(): void {
    this.adminState.clearError();

    if (
      this.adminState.payerCreating()
    ) {
      return;
    }

    this.trimFormValues();

    if (this.payerForm.invalid) {
      this.payerForm.markAllAsTouched();

      this.notificationService.showError(
        'Review the Payer account information and '
        + 'correct the highlighted fields.'
      );

      return;
    }

    const formValue =
      this.payerForm.getRawValue();

    const username =
      formValue.username.trim();

    const firstName =
      formValue.firstName.trim();

    const lastName =
      formValue.lastName.trim();

    const email =
      formValue.email
        .trim()
        .toLowerCase();

    const phoneNumber =
      formValue.phoneNumber.trim();

    if (
      username.length === 0
      || firstName.length === 0
      || lastName.length === 0
      || email.length === 0
    ) {
      this.payerForm.markAllAsTouched();

      this.notificationService.showError(
        'Username, first name, last name, and email '
        + 'are required.'
      );

      return;
    }

    const request:
      CreateStaffRequest = {
        username,
        firstName,
        lastName,
        email,
        phoneNumber:
          phoneNumber.length > 0
            ? phoneNumber
            : null
      };

    this.adminState.createPayer(
      request,
      message => {
        this.createdAccountEmail.set(
          email
        );

        this.createdAccountName.set(
          `${firstName} ${lastName}`.trim()
        );

        this.accountCreated.set(
          true
        );

        this.resetForm();

        this.notificationService.showSuccess(
          message
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  createAnotherPayer(): void {
    if (
      this.adminState.payerCreating()
    ) {
      return;
    }

    this.adminState.clearError();

    this.accountCreated.set(
      false
    );

    this.createdAccountEmail.set('');

    this.createdAccountName.set('');

    this.resetForm();

    this.payerForm.markAsPristine();

    this.payerForm.markAsUntouched();
  }

  goToDashboard(): void {
    this.router.navigate([
      '/admin/dashboard'
    ]);
  }

  goBack(): void {
    this.goToDashboard();
  }

  private resetForm(): void {
    this.payerForm.reset({
      username: '',
      firstName: '',
      lastName: '',
      email: '',
      phoneNumber: ''
    });
  }

  private trimFormValues(): void {
    const formValue =
      this.payerForm.getRawValue();

    this.payerForm.patchValue({
      username:
        formValue.username.trim(),
      firstName:
        formValue.firstName.trim(),
      lastName:
        formValue.lastName.trim(),
      email:
        formValue.email
          .trim()
          .toLowerCase(),
      phoneNumber:
        formValue.phoneNumber.trim()
    });
  }

  private nonWhitespaceValidator():
    ValidatorFn {

    return (
      control: AbstractControl
    ): ValidationErrors | null => {
      const value =
        control.value;

      if (
        value === null
        || value === undefined
      ) {
        return null;
      }

      const normalizedValue =
        String(value).trim();

      if (normalizedValue.length === 0) {
        return {
          whitespace: true
        };
      }

      return null;
    };
  }
}