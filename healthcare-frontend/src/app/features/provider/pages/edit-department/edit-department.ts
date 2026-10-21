import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  ActivatedRoute,
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
  Department
} from '../../models/department';

import {
  UpdateDepartmentRequest
} from '../../models/update-department-request';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

@Component({
  selector: 'app-edit-department',
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
  templateUrl: './edit-department.html',
  styleUrl: './edit-department.scss'
})
export class EditDepartment
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly departmentState =
    inject(DepartmentManagementStateService);

  private departmentId: string | null =
    null;

  readonly departmentForm =
    this.formBuilder.nonNullable.group({
      departmentName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150)
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

  ngOnInit(): void {

    this.departmentId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('departmentId');

    if (!this.departmentId) {

      this.notificationService.showError(
        'Department identifier is missing.'
      );

      this.goBackToDepartments();

      return;
    }

    this.loadDepartment();
  }

  ngOnDestroy(): void {

    this.departmentState
      .clearSelectedDepartment();
  }

  loadDepartment(): void {

    if (!this.departmentId) {
      return;
    }

    this.departmentState
      .loadMyDepartmentById(
        this.departmentId,
        department => {

          this.populateForm(department);
        }
      );
  }

  goBackToDepartments(): void {

    this.router.navigate([
      '/provider/departments'
    ]);
  }

  goBackToDetails(): void {

    const department =
      this.departmentState
        .selectedDepartment();

    const targetDepartmentId =
      department?.id
      ?? this.departmentId;

    if (!targetDepartmentId) {

      this.goBackToDepartments();

      return;
    }

    this.router.navigate([
      '/provider/departments',
      targetDepartmentId
    ]);
  }

  updateDepartment(): void {

    this.departmentState.clearError();

    if (this.departmentForm.invalid) {

      this.departmentForm.markAllAsTouched();

      this.notificationService.showError(
        'Please review the highlighted department fields.'
      );

      return;
    }

    if (this.departmentState.updating()) {
      return;
    }

    const department =
      this.departmentState
        .selectedDepartment();

    const targetDepartmentId =
      department?.id
      ?? this.departmentId;

    if (!targetDepartmentId) {

      this.notificationService.showError(
        'Department information is unavailable.'
      );

      return;
    }

    const request =
      this.buildUpdateRequest();

    this.departmentState
      .updateMyDepartment(
        targetDepartmentId,
        request,
        updatedDepartment => {

          this.notificationService.showSuccess(
            'Department updated successfully.'
          );

          this.router.navigate([
            '/provider/departments',
            updatedDepartment.id
          ]);
        },
        message => {

          this.notificationService.showError(
            message
          );
        }
      );
  }

  private populateForm(
    department: Department
  ): void {

    this.departmentForm.setValue({
      departmentName:
        department.departmentName,

      description:
        department.description,

      contactNumber:
        department.contactNumber ?? '',

      location:
        department.location ?? ''
    });

    this.departmentForm.markAsPristine();
    this.departmentForm.markAsUntouched();
  }

  private buildUpdateRequest():
    UpdateDepartmentRequest {

    const formValue =
      this.departmentForm.getRawValue();

    const contactNumber =
      formValue.contactNumber.trim();

    const location =
      formValue.location.trim();

    return {
      departmentName:
        formValue.departmentName.trim(),

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