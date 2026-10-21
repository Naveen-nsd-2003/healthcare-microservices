import {
  Component,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router
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
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  CreateDepartmentRequest
} from '../../models/create-department-request';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

@Component({
  selector: 'app-create-department',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './create-department.html',
  styleUrl: './create-department.scss'
})
export class CreateDepartment {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly departmentState =
    inject(DepartmentManagementStateService);

  readonly departmentForm =
    this.formBuilder.nonNullable.group({
      departmentName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150)
        ]
      ],

      departmentCode: [
        '',
        [
          Validators.required,
          Validators.minLength(2),
          Validators.maxLength(50),
          Validators.pattern(
            /^[A-Za-z0-9-]+$/
          )
        ]
      ],

      description: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ],

      contactNumber: [
        '',
        [
          Validators.pattern(
            /^[0-9+()\- ]{10,20}$/
          )
        ]
      ],

      location: [
        '',
        [
          Validators.maxLength(150)
        ]
      ]
    });

  goBack(): void {

    this.router.navigate([
      '/provider/departments'
    ]);
  }

  createDepartment(): void {

    this.departmentState.clearError();

    if (this.departmentForm.invalid) {

      this.departmentForm.markAllAsTouched();

      this.notificationService.showError(
        'Please review the highlighted department fields.'
      );

      return;
    }

    if (this.departmentState.creating()) {
      return;
    }

    const request =
      this.buildRequest();

    this.departmentState.createDepartment(
      request,
      department => {

        this.notificationService.showSuccess(
          'Department created successfully.'
        );

        this.router.navigate([
          '/provider/departments',
          department.id
        ]);
      },
      message => {

        this.notificationService.showError(
          message
        );
      }
    );
  }

  private buildRequest():
    CreateDepartmentRequest {

    const formValue =
      this.departmentForm.getRawValue();

    const contactNumber =
      formValue.contactNumber.trim();

    const location =
      formValue.location.trim();

    return {
      departmentName:
        formValue.departmentName.trim(),

      departmentCode:
        formValue.departmentCode
          .trim()
          .toUpperCase(),

      description:
        formValue.description.trim(),

      contactNumber:
        contactNumber.length > 0
          ? contactNumber
          : null,

      location:
        location.length > 0
          ? location
          : null
    };
  }
}