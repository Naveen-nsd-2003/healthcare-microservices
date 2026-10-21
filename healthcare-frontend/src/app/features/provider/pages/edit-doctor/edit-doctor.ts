import {
  Component,
  OnDestroy,
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
  MatSelectModule
} from '@angular/material/select';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  Doctor
} from '../../models/doctor';

import {
  UpdateDoctorRequest
} from '../../models/update-doctor-request';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

import {
  DoctorManagementStateService
} from '../../services/doctor-management-state.service';

@Component({
  selector: 'app-edit-doctor',
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
  templateUrl: './edit-doctor.html',
  styleUrl: './edit-doctor.scss'
})
export class EditDoctor
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly doctorState =
    inject(DoctorManagementStateService);

  readonly departmentState =
    inject(DepartmentManagementStateService);

  private doctorId: string | null =
    null;

  readonly availableDepartments =
    computed(() => {

      const selectedDoctor =
        this.doctorState.selectedDoctor();

      return this.departmentState
        .departments()
        .filter(department => {

          return department.status === 'ACTIVE'
            || department.id
              === selectedDoctor?.departmentId;
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

    this.doctorId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('doctorId');

    if (!this.doctorId) {

      this.notificationService.showError(
        'Doctor identifier is missing.'
      );

      this.goBackToDoctors();

      return;
    }

    this.departmentState.loadMyDepartments();

    this.loadDoctor();
  }

  ngOnDestroy(): void {

    this.doctorState.clearSelectedDoctor();
    this.doctorState.clearError();
  }

  loadDoctor(): void {

    if (!this.doctorId) {
      return;
    }

    this.doctorState.loadMyDoctorById(
      this.doctorId,
      doctor => {

        this.populateForm(doctor);
      }
    );
  }

  reloadDepartments(): void {

    this.departmentState.loadMyDepartments();
  }

  goBackToDoctors(): void {

    this.router.navigate([
      '/provider/doctors'
    ]);
  }

  goBackToDetails(): void {

    const doctor =
      this.doctorState.selectedDoctor();

    const targetDoctorId =
      doctor?.id
      ?? this.doctorId;

    if (!targetDoctorId) {

      this.goBackToDoctors();

      return;
    }

    this.router.navigate([
      '/provider/doctors',
      targetDoctorId
    ]);
  }

  goToDepartments(): void {

    this.router.navigate([
      '/provider/departments'
    ]);
  }

  updateDoctor(): void {

    this.doctorState.clearError();

    if (this.doctorForm.invalid) {

      this.doctorForm.markAllAsTouched();

      this.notificationService.showError(
        'Please review the highlighted doctor fields.'
      );

      return;
    }

    if (this.doctorState.updating()) {
      return;
    }

    const doctor =
      this.doctorState.selectedDoctor();

    const targetDoctorId =
      doctor?.id
      ?? this.doctorId;

    if (!targetDoctorId) {

      this.notificationService.showError(
        'Doctor information is unavailable.'
      );

      return;
    }

    const request =
      this.buildRequest();

    this.doctorState.updateMyDoctor(
      targetDoctorId,
      request,
      updatedDoctor => {

        this.notificationService.showSuccess(
          'Doctor updated successfully.'
        );

        this.router.navigate([
          '/provider/doctors',
          updatedDoctor.id
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
    doctor: Doctor
  ): void {

    this.doctorForm.setValue({
      firstName:
        doctor.firstName,

      lastName:
        doctor.lastName,

      qualification:
        doctor.qualification,

      specialization:
        doctor.specialization,

      yearsOfExperience:
        doctor.yearsOfExperience,

      consultationFee:
        doctor.consultationFee,

      biography:
        doctor.biography ?? '',

      departmentId:
        doctor.departmentId
    });

    this.doctorForm.markAsPristine();
    this.doctorForm.markAsUntouched();
  }

  private buildRequest():
    UpdateDoctorRequest {

    const formValue =
      this.doctorForm.getRawValue();

    const biography =
      formValue.biography.trim();

    return {
      firstName:
        formValue.firstName.trim(),

      lastName:
        formValue.lastName.trim(),

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