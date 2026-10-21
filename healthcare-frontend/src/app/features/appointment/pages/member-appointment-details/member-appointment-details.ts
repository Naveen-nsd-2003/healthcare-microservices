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
  CancelAppointmentRequest
} from '../../models/cancel-appointment-request';

import {
  AppointmentStateService
} from '../../services/appointment-state.service';

@Component({
  selector: 'app-member-appointment-details',
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
  templateUrl: './member-appointment-details.html',
  styleUrl: './member-appointment-details.scss'
})
export class MemberAppointmentDetails
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly appointmentState =
    inject(AppointmentStateService);

  private appointmentId: string | null =
    null;

  readonly cancellationVisible =
    signal(false);

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

  readonly canCancelAppointment =
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
      ) > Date.now();
    });

  ngOnInit(): void {

    this.appointmentId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('appointmentId');

    if (!this.appointmentId) {

      this.notificationMessage.showError(
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

    this.cancellationVisible.set(false);

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.appointmentState
      .loadMyAppointmentById(
        this.appointmentId
      );
  }

  goBack(): void {

    this.router.navigate([
      '/member/appointments'
    ]);
  }

  viewDoctor(): void {

    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (!appointment) {
      return;
    }

    this.router.navigate([
      '/member/doctors',
      appointment.doctorId
    ]);
  }

  viewProvider(): void {

    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (!appointment) {
      return;
    }

    this.router.navigate([
      '/member/providers',
      appointment.providerId
    ]);
  }

  showCancellationForm(): void {

    if (!this.canCancelAppointment()) {

      this.notificationMessage.showError(
        'This appointment can no longer be cancelled.'
      );

      return;
    }

    this.appointmentState.clearError();

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.cancellationVisible.set(true);
  }

  hideCancellationForm(): void {

    if (this.appointmentState.cancelling()) {
      return;
    }

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.appointmentState.clearError();
    this.cancellationVisible.set(false);
  }

  submitCancellation(): void {

    this.appointmentState.clearError();

    if (this.cancellationForm.invalid) {

      this.cancellationForm.markAllAsTouched();

      this.notificationMessage.showError(
        'Enter a valid cancellation reason.'
      );

      return;
    }

    if (!this.canCancelAppointment()) {

      this.notificationMessage.showError(
        'This appointment can no longer be cancelled.'
      );

      this.cancellationVisible.set(false);

      return;
    }

    const appointment =
      this.appointmentState
        .selectedAppointment();

    if (!appointment) {

      this.notificationMessage.showError(
        'Appointment information is unavailable.'
      );

      return;
    }

    if (this.appointmentState.cancelling()) {
      return;
    }

    const formValue =
      this.cancellationForm.getRawValue();

    const request:
      CancelAppointmentRequest = {
        cancellationReason:
          formValue.cancellationReason.trim()
      };

    this.appointmentState
      .cancelMyAppointment(
        appointment.id,
        request,
        updatedAppointment => {

          this.cancellationForm.reset({
            cancellationReason: ''
          });

          this.cancellationVisible.set(false);

          this.notificationMessage.showSuccess(
            'Appointment cancelled successfully.'
          );

          this.appointmentId =
            updatedAppointment.id;
        },
        message => {

          this.notificationMessage.showError(
            message
          );
        }
      );
  }

  getStatusClass(
    status: AppointmentStatus
  ): string {

    switch (status) {

      case 'BOOKED':
        return 'status-badge--booked';

      case 'CONFIRMED':
        return 'status-badge--confirmed';

      case 'COMPLETED':
        return 'status-badge--completed';

      case 'CANCELLED':
        return 'status-badge--cancelled';

      case 'NO_SHOW':
        return 'status-badge--no-show';

      default:
        return '';
    }
  }

  getStatusIcon(
    status: AppointmentStatus
  ): string {

    switch (status) {

      case 'BOOKED':
        return 'event';

      case 'CONFIRMED':
        return 'event_available';

      case 'COMPLETED':
        return 'check_circle';

      case 'CANCELLED':
        return 'event_busy';

      case 'NO_SHOW':
        return 'person_off';

      default:
        return 'calendar_month';
    }
  }

  getStatusLabel(
    status: AppointmentStatus
  ): string {

    switch (status) {

      case 'BOOKED':
        return 'Booked';

      case 'CONFIRMED':
        return 'Confirmed';

      case 'CANCELLED':
        return 'Cancelled';

      case 'COMPLETED':
        return 'Completed';

      case 'NO_SHOW':
        return 'No show';

      default:
        return status;
    }
  }

  getStatusDescription(
    appointment: Appointment
  ): string {

    switch (appointment.status) {

      case 'BOOKED':
        return 'Your appointment has been booked and '
          + 'is waiting for Provider confirmation.';

      case 'CONFIRMED':
        return 'The healthcare Provider has confirmed '
          + 'your appointment.';

      case 'COMPLETED':
        return 'This healthcare appointment has been completed.';

      case 'CANCELLED':
        return 'This appointment was cancelled and '
          + 'is no longer scheduled.';

      case 'NO_SHOW':
        return 'This appointment was marked as a no-show.';

      default:
        return 'Appointment status information is available.';
    }
  }

  getTimeLabel(
    timeValue: string
  ): string {

    if (!timeValue) {
      return '';
    }

    const timeParts =
      timeValue.split(':');

    if (timeParts.length < 2) {
      return timeValue;
    }

    const hours =
      Number(timeParts[0]);

    const minutes =
      Number(timeParts[1]);

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

    const displayMinutes =
      String(minutes).padStart(
        2,
        '0'
      );

    return (
      displayHours
      + ':'
      + displayMinutes
      + ' '
      + period
    );
  }

  getTimeRange(
    appointment: Appointment
  ): string {

    return (
      this.getTimeLabel(
        appointment.startTime
      )
      + ' - '
      + this.getTimeLabel(
        appointment.endTime
      )
    );
  }

  getDurationLabel(
    minutes: number
  ): string {

    if (minutes === 1) {
      return '1 minute';
    }

    return minutes + ' minutes';
  }

  getCancelledByLabel(
    appointment: Appointment
  ): string {

    if (appointment.cancelledBy === 'MEMBER') {
      return 'Cancelled by Member';
    }

    if (appointment.cancelledBy === 'PROVIDER') {
      return 'Cancelled by Provider';
    }

    return 'Not available';
  }

  isUpcoming(
    appointment: Appointment
  ): boolean {

    if (
      appointment.status === 'CANCELLED'
      || appointment.status === 'COMPLETED'
      || appointment.status === 'NO_SHOW'
    ) {
      return false;
    }

    return this.getAppointmentDateTimeValue(
      appointment
    ) >= Date.now();
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

    if (Number.isNaN(timestamp)) {
      return 0;
    }

    return timestamp;
  }
}