import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  CurrencyPipe,
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
  Appointment
} from '../../models/appointment';

import {
  AppointmentStatus
} from '../../models/appointment-status';

import {
  CompleteAppointmentRequest
} from '../../models/complete-appointment-request';

import {
  ProviderCancelAppointmentRequest
} from '../../models/provider-cancel-appointment-request';

import {
  ProviderAppointmentStateService
} from '../../services/provider-appointment-state.service';

type ProviderActionForm =
  | 'NONE'
  | 'CANCEL'
  | 'COMPLETE';

@Component({
  selector: 'app-provider-appointment-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './provider-appointment-details.html',
  styleUrl: './provider-appointment-details.scss'
})
export class ProviderAppointmentDetails
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

  private appointmentId: string | null =
    null;

  readonly visibleForm =
    signal<ProviderActionForm>('NONE');

  readonly cancellationForm =
    this.formBuilder.nonNullable.group({
      cancellationReason: [
        '',
        [
          Validators.required,
          Validators.maxLength(500)
        ]
      ]
    });

  readonly completionForm =
    this.formBuilder.nonNullable.group({
      providerNotes: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ]
    });

  readonly canConfirm =
    computed(() => {
      return this.appointmentState
        .selectedAppointment()
        ?.status === 'BOOKED';
    });

  readonly canComplete =
    computed(() => {
      return this.appointmentState
        .selectedAppointment()
        ?.status === 'CONFIRMED';
    });

  readonly canCancel =
    computed(() => {
      const status =
        this.appointmentState
          .selectedAppointment()
          ?.status;

      return status === 'BOOKED'
        || status === 'CONFIRMED';
    });

  readonly canMarkNoShow =
    computed(() => {
      const appointment =
        this.appointmentState
          .selectedAppointment();

      if (!appointment) {
        return false;
      }

      if (
        appointment.status !== 'BOOKED'
        && appointment.status !== 'CONFIRMED'
      ) {
        return false;
      }

      return this.getAppointmentDateTimeValue(
        appointment
      ) < Date.now();
    });

  readonly isFinalStatus =
    computed(() => {
      const status =
        this.appointmentState
          .selectedAppointment()
          ?.status;

      return status === 'COMPLETED'
        || status === 'CANCELLED'
        || status === 'NO_SHOW';
    });

  ngOnInit(): void {
    this.appointmentId =
      this.activatedRoute.snapshot.paramMap.get(
        'appointmentId'
      );

    if (!this.appointmentId) {
      this.notificationService.showError(
        'Appointment identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.loadAppointment();
  }

  ngOnDestroy(): void {
    this.appointmentState
      .clearSelectedAppointment();
  }

  loadAppointment(): void {
    if (!this.appointmentId) {
      return;
    }

    this.closeActionForm();

    this.appointmentState
      .loadProviderAppointmentById(
        this.appointmentId
      );
  }

  goBack(): void {
    this.router.navigate([
      '/provider/appointments'
    ]);
  }

  confirmAppointment(): void {
    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (
      !appointment
      || !this.canConfirm()
      || this.appointmentState.actionInProgress()
    ) {
      return;
    }

    this.appointmentState.confirmAppointment(
      appointment.id,
      () => {
        this.notificationService.showSuccess(
          'Appointment confirmed successfully.'
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  showCancellationForm(): void {
    if (!this.canCancel()) {
      return;
    }

    this.appointmentState.clearError();

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.visibleForm.set('CANCEL');
  }

  showCompletionForm(): void {
    if (!this.canComplete()) {
      return;
    }

    this.appointmentState.clearError();

    this.completionForm.reset({
      providerNotes: ''
    });

    this.visibleForm.set('COMPLETE');
  }

  closeActionForm(): void {
    if (this.appointmentState.actionInProgress()) {
      return;
    }

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.completionForm.reset({
      providerNotes: ''
    });

    this.appointmentState.clearError();
    this.visibleForm.set('NONE');
  }

  submitCancellation(): void {
    this.appointmentState.clearError();

    if (this.cancellationForm.invalid) {
      this.cancellationForm.markAllAsTouched();

      this.notificationService.showError(
        'Enter a valid cancellation reason.'
      );

      return;
    }

    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (
      !appointment
      || !this.canCancel()
      || this.appointmentState.actionInProgress()
    ) {
      return;
    }

    const formValue =
      this.cancellationForm.getRawValue();

    const request:
      ProviderCancelAppointmentRequest = {
        cancellationReason:
          formValue.cancellationReason.trim()
      };

    this.appointmentState
      .cancelProviderAppointment(
        appointment.id,
        request,
        () => {
          this.visibleForm.set('NONE');

          this.cancellationForm.reset({
            cancellationReason: ''
          });

          this.notificationService.showSuccess(
            'Appointment cancelled successfully.'
          );
        },
        message => {
          this.notificationService.showError(
            message
          );
        }
      );
  }

  submitCompletion(): void {
    this.appointmentState.clearError();

    if (this.completionForm.invalid) {
      this.completionForm.markAllAsTouched();

      this.notificationService.showError(
        'Enter valid Provider notes.'
      );

      return;
    }

    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (
      !appointment
      || !this.canComplete()
      || this.appointmentState.actionInProgress()
    ) {
      return;
    }

    const formValue =
      this.completionForm.getRawValue();

    const request:
      CompleteAppointmentRequest = {
        providerNotes:
          formValue.providerNotes.trim()
      };

    this.appointmentState.completeAppointment(
      appointment.id,
      request,
      () => {
        this.visibleForm.set('NONE');

        this.completionForm.reset({
          providerNotes: ''
        });

        this.notificationService.showSuccess(
          'Appointment completed successfully.'
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  markAsNoShow(): void {
    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (
      !appointment
      || !this.canMarkNoShow()
      || this.appointmentState.actionInProgress()
    ) {
      return;
    }

    const confirmed =
      window.confirm(
        'Mark this appointment as no-show?'
      );

    if (!confirmed) {
      return;
    }

    this.appointmentState
      .markAppointmentAsNoShow(
        appointment.id,
        () => {
          this.notificationService.showSuccess(
            'Appointment marked as no-show.'
          );
        },
        message => {
          this.notificationService.showError(
            message
          );
        }
      );
  }

  createMedicalRecord(): void {
    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (
      !appointment
      || appointment.status !== 'COMPLETED'
    ) {
      return;
    }

    this.router.navigate(
      [
        '/provider/medical-records/new'
      ],
      {
        queryParams: {
          appointmentId: appointment.id
        }
      }
    );
  }

  getStatusLabel(
    status: AppointmentStatus
  ): string {
    if (status === 'NO_SHOW') {
      return 'No show';
    }

    return status.charAt(0)
      + status.slice(1).toLowerCase();
  }

  getTimeRange(
    appointment: Appointment
  ): string {
    return this.getTimeLabel(
      appointment.startTime
    )
      + ' - '
      + this.getTimeLabel(
        appointment.endTime
      );
  }

  getTimeLabel(
    timeValue: string
  ): string {
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

    return displayHours
      + ':'
      + String(minutes).padStart(2, '0')
      + ' '
      + period;
  }

  private getAppointmentDateTimeValue(
    appointment: Appointment
  ): number {
    const timestamp =
      new Date(
        appointment.appointmentDate
        + 'T'
        + appointment.startTime
      ).getTime();

    return Number.isNaN(timestamp)
      ? 0
      : timestamp;
  }
}