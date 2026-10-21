import {
  Component,
  OnDestroy,
  OnInit,
  effect,
  inject,
  signal
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
  MedicalRecord
} from '../../models/medical-record';
import {
  UpdateMedicalRecordRequest
} from '../../models/update-medical-record-request';
import {
  MedicalRecordStateService
} from '../../services/medical-record-state.service';

@Component({
  selector: 'app-edit-medical-record',
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
  templateUrl: './edit-medical-record.html',
  styleUrl: './edit-medical-record.scss'
})
export class EditMedicalRecord
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly medicalRecordState =
    inject(MedicalRecordStateService);

  private medicalRecordId: string | null =
    null;

  private readonly formInitialized =
    signal(false);

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

  private readonly populateFormEffect =
    effect(() => {
      const medicalRecord =
        this.medicalRecordState
          .selectedRecord();

      if (
        !medicalRecord
        || !this.medicalRecordId
        || medicalRecord.id
          !== this.medicalRecordId
        || this.formInitialized()
      ) {
        return;
      }

      this.populateForm(
        medicalRecord
      );

      this.formInitialized.set(true);
    });

  ngOnInit(): void {
    this.medicalRecordId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('medicalRecordId');

    if (!this.medicalRecordId) {
      this.notificationService.showError(
        'Medical record identifier is missing.'
      );

      this.goBackToMedicalRecords();

      return;
    }

    this.loadMedicalRecord();
  }

  ngOnDestroy(): void {
    this.medicalRecordState
      .clearSelectedRecord();
  }

  loadMedicalRecord(): void {
    if (!this.medicalRecordId) {
      return;
    }

    this.formInitialized.set(false);

    this.medicalRecordState
      .loadProviderMedicalRecordById(
        this.medicalRecordId
      );
  }

  updateMedicalRecord(): void {
    this.medicalRecordState.clearError();

    if (
      this.medicalRecordState.updating()
    ) {
      return;
    }

    if (!this.medicalRecordId) {
      this.notificationService.showError(
        'Medical record identifier is missing.'
      );

      return;
    }

    const medicalRecord =
      this.medicalRecordState
        .selectedRecord();

    if (!medicalRecord) {
      this.notificationService.showError(
        'Medical record information is unavailable.'
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
      && followUpDate
        !== medicalRecord.followUpDate
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
        'A new follow-up date cannot be in the past.'
      );

      return;
    }

    const request:
      UpdateMedicalRecordRequest = {
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
      .updateProviderMedicalRecord(
        this.medicalRecordId,
        request,
        updatedMedicalRecord => {
          this.notificationService.showSuccess(
            'Medical record updated successfully.'
          );

          this.router.navigate([
            '/provider/medical-records',
            updatedMedicalRecord.id
          ]);
        },
        message => {
          this.notificationService.showError(
            message
          );
        }
      );
  }

  goBack(): void {
    if (this.medicalRecordId) {
      this.router.navigate([
        '/provider/medical-records',
        this.medicalRecordId
      ]);

      return;
    }

    this.goBackToMedicalRecords();
  }

  goBackToMedicalRecords(): void {
    this.router.navigate([
      '/provider/medical-records'
    ]);
  }

  retryMedicalRecord(): void {
    this.loadMedicalRecord();
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
      return 'Not available';
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

  private populateForm(
    medicalRecord: MedicalRecord
  ): void {
    this.medicalRecordForm.reset({
      chiefComplaint:
        medicalRecord.chiefComplaint,
      diagnosis:
        medicalRecord.diagnosis,
      clinicalObservations:
        medicalRecord.clinicalObservations
        ?? '',
      treatmentPlan:
        medicalRecord.treatmentPlan,
      prescriptionNotes:
        medicalRecord.prescriptionNotes
        ?? '',
      followUpDate:
        medicalRecord.followUpDate
        ?? ''
    });

    this.medicalRecordForm
      .markAsPristine();

    this.medicalRecordForm
      .markAsUntouched();
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