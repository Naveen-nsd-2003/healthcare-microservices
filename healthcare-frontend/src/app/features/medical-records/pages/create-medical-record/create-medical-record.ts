// import {
//   Component,
//   OnDestroy,
//   OnInit,
//   computed,
//   inject
// } from '@angular/core';

// import {
//   DatePipe
// } from '@angular/common';

// import {
//   FormBuilder,
//   ReactiveFormsModule,
//   Validators
// } from '@angular/forms';

// import {
//   ActivatedRoute,
//   Router
// } from '@angular/router';

// import {
//   MatButtonModule
// } from '@angular/material/button';

// import {
//   MatCardModule
// } from '@angular/material/card';

// import {
//   MatFormFieldModule
// } from '@angular/material/form-field';

// import {
//   MatIconModule
// } from '@angular/material/icon';

// import {
//   MatInputModule
// } from '@angular/material/input';

// import {
//   MatProgressSpinnerModule
// } from '@angular/material/progress-spinner';

// import {
//   NotificationMessageService
// } from '../../../../core/services/notification-message.service';

// import {
//   ProviderAppointmentStateService
// } from '../../../appointment/services/provider-appointment-state.service';

// import {
//   CreateMedicalRecordRequest
// } from '../../models/create-medical-record-request';

// import {
//   MedicalRecordStateService
// } from '../../services/medical-record-state.service';

// @Component({
//   selector: 'app-create-medical-record',
//   standalone: true,
//   imports: [
//     DatePipe,
//     ReactiveFormsModule,
//     MatButtonModule,
//     MatCardModule,
//     MatFormFieldModule,
//     MatIconModule,
//     MatInputModule,
//     MatProgressSpinnerModule
//   ],
//   templateUrl: './create-medical-record.html',
//   styleUrl: './create-medical-record.scss'
// })
// export class CreateMedicalRecord
//   implements OnInit, OnDestroy {

//   private readonly formBuilder =
//     inject(FormBuilder);

//   private readonly activatedRoute =
//     inject(ActivatedRoute);

//   private readonly router =
//     inject(Router);

//   private readonly notificationService =
//     inject(NotificationMessageService);

//   readonly appointmentState =
//     inject(ProviderAppointmentStateService);

//   readonly medicalRecordState =
//     inject(MedicalRecordStateService);

//   private appointmentId: string | null =
//     null;

//   readonly minimumFollowUpDate =
//     this.getCurrentLocalDate();

//   readonly medicalRecordForm =
//     this.formBuilder.nonNullable.group({
//       chiefComplaint: [
//         '',
//         [
//           Validators.required,
//           Validators.maxLength(1000)
//         ]
//       ],
//       diagnosis: [
//         '',
//         [
//           Validators.required,
//           Validators.maxLength(2000)
//         ]
//       ],
//       clinicalObservations: [
//         '',
//         [
//           Validators.maxLength(3000)
//         ]
//       ],
//       treatmentPlan: [
//         '',
//         [
//           Validators.required,
//           Validators.maxLength(3000)
//         ]
//       ],
//       prescriptionNotes: [
//         '',
//         [
//           Validators.maxLength(3000)
//         ]
//       ],
//       followUpDate: [
//         ''
//       ]
//     });

//   readonly appointmentIsCompleted =
//     computed(
//       () =>
//         this.appointmentState
//           .selectedAppointment()
//           ?.status === 'COMPLETED'
//     );

//   ngOnInit(): void {

//     this.appointmentId =
//       this.activatedRoute.snapshot
//         .queryParamMap
//         .get('appointmentId');

//     if (!this.appointmentId) {
//       this.notificationService.showError(
//         'Appointment identifier is missing.'
//       );

//       this.goBackToAppointments();

//       return;
//     }

//     this.appointmentState
//       .loadProviderAppointmentById(
//         this.appointmentId
//       );
//   }

//   ngOnDestroy(): void {

//     this.medicalRecordState
//       .clearError();

//     this.appointmentState
//       .clearSelectedAppointment();
//   }

//   submitMedicalRecord(): void {

//     this.medicalRecordState.clearError();

//     if (!this.appointmentId) {
//       this.notificationService.showError(
//         'Appointment identifier is missing.'
//       );

//       return;
//     }

//     const appointment =
//       this.appointmentState
//         .selectedAppointment();

//     if (!appointment) {
//       this.notificationService.showError(
//         'Appointment information is unavailable.'
//       );

//       return;
//     }

//     if (appointment.status !== 'COMPLETED') {
//       this.notificationService.showError(
//         'A medical record can only be created '
//         + 'for a completed appointment.'
//       );

//       return;
//     }

//     if (this.medicalRecordForm.invalid) {
//       this.medicalRecordForm.markAllAsTouched();

//       this.notificationService.showError(
//         'Review the medical record information '
//         + 'and correct the highlighted fields.'
//       );

//       return;
//     }

//     const formValue =
//       this.medicalRecordForm.getRawValue();

//     const followUpDate =
//       formValue.followUpDate.trim();

//     if (
//       followUpDate
//       && followUpDate < this.minimumFollowUpDate
//     ) {
//       this.medicalRecordForm.controls
//         .followUpDate
//         .setErrors({
//           pastDate: true
//         });

//       this.medicalRecordForm.controls
//         .followUpDate
//         .markAsTouched();

//       this.notificationService.showError(
//         'Follow-up date cannot be in the past.'
//       );

//       return;
//     }

//     const request:
//       CreateMedicalRecordRequest = {
//         appointmentId: this.appointmentId,
//         chiefComplaint:
//           formValue.chiefComplaint.trim(),
//         diagnosis:
//           formValue.diagnosis.trim(),
//         clinicalObservations:
//           this.toNullableValue(
//             formValue.clinicalObservations
//           ),
//         treatmentPlan:
//           formValue.treatmentPlan.trim(),
//         prescriptionNotes:
//           this.toNullableValue(
//             formValue.prescriptionNotes
//           ),
//         followUpDate:
//           followUpDate || null
//       };

//     this.medicalRecordState
//       .createMedicalRecord(
//         request,
//         medicalRecord => {
//           this.notificationService.showSuccess(
//             'Medical record created successfully.'
//           );

//           this.router.navigate([
//             '/provider/medical-records',
//             medicalRecord.id
//           ]);
//         },
//         message => {
//           this.notificationService.showError(
//             message
//           );
//         }
//       );
//   }

//   goBack(): void {

//     if (this.appointmentId) {
//       this.router.navigate([
//         '/provider/appointments',
//         this.appointmentId
//       ]);

//       return;
//     }

//     this.goBackToAppointments();
//   }

//   retryAppointment(): void {

//     if (!this.appointmentId) {
//       return;
//     }

//     this.appointmentState
//       .loadProviderAppointmentById(
//         this.appointmentId
//       );
//   }

//   getTimeRange(
//     startTime: string,
//     endTime: string
//   ): string {

//     return this.getTimeLabel(startTime)
//       + ' - '
//       + this.getTimeLabel(endTime);
//   }

//   getTimeLabel(
//     timeValue: string
//   ): string {

//     const parts =
//       timeValue.split(':');

//     if (parts.length < 2) {
//       return timeValue;
//     }

//     const hours =
//       Number(parts[0]);

//     const minutes =
//       Number(parts[1]);

//     if (
//       Number.isNaN(hours)
//       || Number.isNaN(minutes)
//     ) {
//       return timeValue;
//     }

//     const period =
//       hours >= 12
//         ? 'PM'
//         : 'AM';

//     const displayHours =
//       hours % 12 === 0
//         ? 12
//         : hours % 12;

//     return displayHours
//       + ':'
//       + String(minutes).padStart(2, '0')
//       + ' '
//       + period;
//   }

//   private goBackToAppointments(): void {

//     this.router.navigate([
//       '/provider/appointments'
//     ]);
//   }

//   private toNullableValue(
//     value: string
//   ): string | null {

//     const trimmedValue =
//       value.trim();

//     return trimmedValue.length > 0
//       ? trimmedValue
//       : null;
//   }

//   private getCurrentLocalDate(): string {

//     const currentDate =
//       new Date();

//     const year =
//       currentDate.getFullYear();

//     const month =
//       String(
//         currentDate.getMonth() + 1
//       ).padStart(2, '0');

//     const day =
//       String(
//         currentDate.getDate()
//       ).padStart(2, '0');

//     return `${year}-${month}-${day}`;
//   }
// }

import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

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
  ProviderAppointmentStateService
} from '../../../appointment/services/provider-appointment-state.service';

import {
  CreateMedicalRecordRequest
} from '../../models/create-medical-record-request';

import {
  MedicalRecordStateService
} from '../../services/medical-record-state.service';

@Component({
  selector: 'app-create-medical-record',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './create-medical-record.html',
  styleUrl: './create-medical-record.scss'
})
export class CreateMedicalRecord
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly appointmentState =
    inject(ProviderAppointmentStateService);

  readonly medicalRecordState =
    inject(MedicalRecordStateService);

  private appointmentId: string | null =
    null;

  readonly minimumFollowUpDate =
    this.getCurrentLocalDate();

  readonly medicalRecordForm =
    this.formBuilder.nonNullable.group({
      chiefComplaint: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ],

      diagnosis: [
        '',
        [
          Validators.required,
          Validators.maxLength(2000)
        ]
      ],

      clinicalObservations: [
        '',
        [
          Validators.maxLength(3000)
        ]
      ],

      treatmentPlan: [
        '',
        [
          Validators.required,
          Validators.maxLength(3000)
        ]
      ],

      prescriptionNotes: [
        '',
        [
          Validators.maxLength(3000)
        ]
      ],

      followUpDate: [
        ''
      ]
    });

  ngOnInit(): void {

    this.appointmentId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('appointmentId');

    if (!this.appointmentId) {

      this.notificationService.showError(
        'Appointment identifier is missing.'
      );

      this.goBackToAppointments();

      return;
    }

    this.loadAppointment();
  }

  ngOnDestroy(): void {

    this.medicalRecordState
      .clearError();

    this.appointmentState
      .clearSelectedAppointment();
  }

  loadAppointment(): void {

    if (!this.appointmentId) {
      return;
    }

    this.appointmentState
      .loadProviderAppointmentById(
        this.appointmentId
      );
  }

  submitMedicalRecord(): void {

    this.medicalRecordState.clearError();

    if (
      this.medicalRecordState.creating()
    ) {
      return;
    }

    if (!this.appointmentId) {

      this.notificationService.showError(
        'Appointment identifier is missing.'
      );

      return;
    }

    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (!appointment) {

      this.notificationService.showError(
        'Appointment information is unavailable.'
      );

      return;
    }

    if (
      appointment.status !== 'COMPLETED'
    ) {

      this.notificationService.showError(
        'A medical record can only be created '
        + 'for a completed appointment.'
      );

      return;
    }

    if (this.medicalRecordForm.invalid) {

      this.medicalRecordForm
        .markAllAsTouched();

      this.notificationService.showError(
        'Review the medical record information '
        + 'and correct the highlighted fields.'
      );

      return;
    }

    const formValue =
      this.medicalRecordForm
        .getRawValue();

    const chiefComplaint =
      formValue.chiefComplaint.trim();

    const diagnosis =
      formValue.diagnosis.trim();

    const treatmentPlan =
      formValue.treatmentPlan.trim();

    const followUpDate =
      formValue.followUpDate.trim();

    if (chiefComplaint.length === 0) {

      this.medicalRecordForm.controls
        .chiefComplaint
        .setErrors({
          required: true
        });

      this.medicalRecordForm.controls
        .chiefComplaint
        .markAsTouched();

      this.notificationService.showError(
        'Chief complaint is required.'
      );

      return;
    }

    if (diagnosis.length === 0) {

      this.medicalRecordForm.controls
        .diagnosis
        .setErrors({
          required: true
        });

      this.medicalRecordForm.controls
        .diagnosis
        .markAsTouched();

      this.notificationService.showError(
        'Diagnosis is required.'
      );

      return;
    }

    if (treatmentPlan.length === 0) {

      this.medicalRecordForm.controls
        .treatmentPlan
        .setErrors({
          required: true
        });

      this.medicalRecordForm.controls
        .treatmentPlan
        .markAsTouched();

      this.notificationService.showError(
        'Treatment plan is required.'
      );

      return;
    }

    if (
      followUpDate.length > 0
      && followUpDate
        < this.minimumFollowUpDate
    ) {

      this.medicalRecordForm.controls
        .followUpDate
        .setErrors({
          pastDate: true
        });

      this.medicalRecordForm.controls
        .followUpDate
        .markAsTouched();

      this.notificationService.showError(
        'Follow-up date cannot be in the past.'
      );

      return;
    }

    const request:
      CreateMedicalRecordRequest = {
        appointmentId:
          this.appointmentId,

        chiefComplaint,

        diagnosis,

        clinicalObservations:
          this.toNullableValue(
            formValue.clinicalObservations
          ),

        treatmentPlan,

        prescriptionNotes:
          this.toNullableValue(
            formValue.prescriptionNotes
          ),

        followUpDate:
          followUpDate.length > 0
            ? followUpDate
            : null
      };

    this.medicalRecordState
      .createMedicalRecord(
        request,
        medicalRecord => {

          this.notificationService
            .showSuccess(
              'Medical record created successfully.'
            );

          this.router.navigate([
            '/provider/medical-records',
            medicalRecord.id
          ]);
        },
        message => {

          this.notificationService
            .showError(
              message
            );
        }
      );
  }

  goBack(): void {

    if (this.appointmentId) {

      this.router.navigate([
        '/provider/appointments',
        this.appointmentId
      ]);

      return;
    }

    this.goBackToAppointments();
  }

  goBackToAppointments(): void {

    this.router.navigate([
      '/provider/appointments'
    ]);
  }

  retryAppointment(): void {

    this.loadAppointment();
  }

  getTimeRange(
    startTime: string,
    endTime: string
  ): string {

    return this.getTimeLabel(
      startTime
    )
      + ' - '
      + this.getTimeLabel(
        endTime
      );
  }

  getTimeLabel(
    timeValue: string
  ): string {

    if (!timeValue) {
      return '';
    }

    const parts =
      timeValue.split(':');

    if (parts.length < 2) {
      return timeValue;
    }

    const hours =
      Number(parts[0]);

    const minutes =
      Number(parts[1]);

    if (
      Number.isNaN(hours)
      || Number.isNaN(minutes)
      || hours < 0
      || hours > 23
      || minutes < 0
      || minutes > 59
    ) {
      return timeValue;
    }

    const period =
      hours >= 12
        ? 'PM'
        : 'AM';

    const displayHours =
      hours % 12 === 0
        ? 12
        : hours % 12;

    const displayMinutes =
      String(minutes).padStart(
        2,
        '0'
      );

    return displayHours
      + ':'
      + displayMinutes
      + ' '
      + period;
  }

  private toNullableValue(
    value: string
  ): string | null {

    const trimmedValue =
      value.trim();

    if (trimmedValue.length === 0) {
      return null;
    }

    return trimmedValue;
  }

  private getCurrentLocalDate(): string {

    const currentDate =
      new Date();

    const year =
      currentDate.getFullYear();

    const month =
      String(
        currentDate.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const day =
      String(
        currentDate.getDate()
      ).padStart(
        2,
        '0'
      );

    return year
      + '-'
      + month
      + '-'
      + day;
  }
}