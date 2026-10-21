import {
  Component,
  OnInit,
  computed,
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
  MatSelectModule
} from '@angular/material/select';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  CreateDoctorRequest
} from '../../models/create-doctor-request';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

import {
  DoctorManagementStateService
} from '../../services/doctor-management-state.service';

@Component({
  selector: 'app-create-doctor',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './create-doctor.html',
  styleUrl: './create-doctor.scss'
})
export class CreateDoctor
  implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly doctorState =
    inject(DoctorManagementStateService);

  readonly departmentState =
    inject(DepartmentManagementStateService);

  readonly activeDepartments =
    computed(() => {

      return this.departmentState
        .departments()
        .filter(department => {

          return department.status === 'ACTIVE';
        })
        .sort(
          (
            firstDepartment,
            secondDepartment
          ) => {

            return firstDepartment.departmentName
              .localeCompare(
                secondDepartment.departmentName
              );
          }
        );
    });

  readonly doctorForm =
    this.formBuilder.nonNullable.group({
      firstName: [
        '',
        [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z][A-Za-z .'-]*$/
          )
        ]
      ],

      lastName: [
        '',
        [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z][A-Za-z .'-]*$/
          )
        ]
      ],

      medicalRegistrationNumber: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z0-9./_-]+$/
          )
        ]
      ],

      qualification: [
        '',
        [
          Validators.required,
          Validators.maxLength(200)
        ]
      ],

      specialization: [
        '',
        [
          Validators.required,
          Validators.maxLength(150)
        ]
      ],

      yearsOfExperience: [
        0,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(70)
        ]
      ],

      consultationFee: [
        0,
        [
          Validators.required,
          Validators.min(0)
        ]
      ],

      biography: [
        '',
        [
          Validators.maxLength(1500)
        ]
      ],

      departmentId: [
        '',
        [
          Validators.required
        ]
      ]
    });

  ngOnInit(): void {

    this.departmentState.loadMyDepartments();
  }

  goBack(): void {

    this.router.navigate([
      '/provider/doctors'
    ]);
  }

  goToDepartments(): void {

    this.router.navigate([
      '/provider/departments'
    ]);
  }

  reloadDepartments(): void {

    this.departmentState.loadMyDepartments();
  }

  createDoctor(): void {

    this.doctorState.clearError();

    if (this.doctorForm.invalid) {

      this.doctorForm.markAllAsTouched();

      this.notificationService.showError(
        'Please review the highlighted doctor fields.'
      );

      return;
    }

    if (this.doctorState.creating()) {
      return;
    }

    const request =
      this.buildRequest();

    this.doctorState.createDoctor(
      request,
      doctor => {

        this.notificationService.showSuccess(
          'Doctor created successfully.'
        );

        this.router.navigate([
          '/provider/doctors',
          doctor.id
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
    CreateDoctorRequest {

    const formValue =
      this.doctorForm.getRawValue();

    const biography =
      formValue.biography.trim();

    return {
      firstName:
        formValue.firstName.trim(),

      lastName:
        formValue.lastName.trim(),

      medicalRegistrationNumber:
        formValue.medicalRegistrationNumber
          .trim()
          .toUpperCase(),

      qualification:
        formValue.qualification.trim(),

      specialization:
        formValue.specialization.trim(),

      yearsOfExperience:
        Number(
          formValue.yearsOfExperience
        ),

      consultationFee:
        Number(
          formValue.consultationFee
        ),

      biography:
        biography.length > 0
          ? biography
          : null,

      departmentId:
        formValue.departmentId
    };
  }
}