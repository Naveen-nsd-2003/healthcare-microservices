import {
  Component,
  OnDestroy,
  OnInit,
  inject,
  signal
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
  MatDialog
} from '@angular/material/dialog';

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
  filter
} from 'rxjs';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  ConfirmationDialog
} from '../../../../shared/dialogs/confirmation-dialog/confirmation-dialog';

import {
  ConfirmationDialogData
} from '../../../../shared/models/confirmation-dialog-data';

import {
  AppointmentSlot
} from '../../models/appointment-slot';

import {
  BookAppointmentRequest
} from '../../models/book-appointment-request';

import {
  AppointmentStateService
} from '../../services/appointment-state.service';

import {
  ProviderDiscoveryStateService
} from '../../../provider/services/provider-discovery-state.service';

@Component({
  selector: 'app-book-appointment',
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
  templateUrl: './book-appointment.html',
  styleUrl: './book-appointment.scss'
})
export class BookAppointment
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly dialog =
    inject(MatDialog);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly appointmentState =
    inject(AppointmentStateService);

  readonly providerState =
    inject(ProviderDiscoveryStateService);

  private doctorId: string | null =
    null;

  readonly selectedSlot =
    signal<AppointmentSlot | null>(null);

  readonly minimumDate =
    this.getTodayDateValue();

  readonly appointmentForm =
    this.formBuilder.nonNullable.group({
      appointmentDate: [
        this.minimumDate,
        [
          Validators.required
        ]
      ],

      reasonForVisit: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ]
    });

  ngOnInit(): void {

    this.doctorId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('doctorId');

    if (!this.doctorId) {

      this.notificationMessage.showError(
        'Select a doctor before booking an appointment.'
      );

      this.goBackToProviders();

      return;
    }

    this.providerState.loadDoctorDetails(
      this.doctorId
    );

    this.loadSlotsForSelectedDate();
  }

  ngOnDestroy(): void {

    this.appointmentState.clearAvailableSlots();
    this.appointmentState.clearError();
    this.providerState.clearDoctorDetails();
  }

  goBackToDoctor(): void {

    if (!this.doctorId) {

      this.goBackToProviders();

      return;
    }

    this.router.navigate([
      '/member/doctors',
      this.doctorId
    ]);
  }

  goBackToProviders(): void {

    this.router.navigate([
      '/member/providers'
    ]);
  }

  onDateChanged(): void {

    this.selectedSlot.set(null);
    this.appointmentState.clearAvailableSlots();
    this.appointmentState.clearError();

    const appointmentDate =
      this.appointmentForm.controls
        .appointmentDate.value;

    if (!appointmentDate) {
      return;
    }

    if (appointmentDate < this.minimumDate) {

      this.notificationMessage.showError(
        'Appointment date cannot be in the past.'
      );

      return;
    }

    this.loadSlotsForSelectedDate();
  }

  loadSlotsForSelectedDate(): void {

    if (!this.doctorId) {
      return;
    }

    const appointmentDate =
      this.appointmentForm.controls
        .appointmentDate.value;

    if (!appointmentDate) {
      return;
    }

    if (appointmentDate < this.minimumDate) {
      return;
    }

    this.selectedSlot.set(null);

    this.appointmentState.loadAvailableSlots(
      this.doctorId,
      appointmentDate
    );
  }

  selectSlot(
    slot: AppointmentSlot
  ): void {

    if (this.appointmentState.booking()) {
      return;
    }

    this.selectedSlot.set(slot);
  }

  isSlotSelected(
    slot: AppointmentSlot
  ): boolean {

    const currentSlot =
      this.selectedSlot();

    if (!currentSlot) {
      return false;
    }

    return currentSlot.startTime
      === slot.startTime
      && currentSlot.endTime
      === slot.endTime;
  }

  reviewAndBook(): void {

    this.appointmentState.clearError();

    if (this.appointmentForm.invalid) {

      this.appointmentForm.markAllAsTouched();

      this.notificationMessage.showError(
        'Please select an appointment date and '
        + 'enter the reason for your visit.'
      );

      return;
    }

    const appointmentDate =
      this.appointmentForm.controls
        .appointmentDate.value;

    if (appointmentDate < this.minimumDate) {

      this.notificationMessage.showError(
        'Appointment date cannot be in the past.'
      );

      return;
    }

    const slot =
      this.selectedSlot();

    if (!slot) {

      this.notificationMessage.showError(
        'Select an available appointment time.'
      );

      return;
    }

    if (!this.doctorId) {

      this.notificationMessage.showError(
        'Doctor information is unavailable.'
      );

      return;
    }

    if (this.appointmentState.booking()) {
      return;
    }

    const doctor =
      this.providerState.selectedDoctor();

    const doctorName =
      doctor?.fullName
      ?? this.appointmentState
        .availableSlots()
        ?.doctorName
      ?? 'the selected doctor';

    const dialogData:
      ConfirmationDialogData = {
        title: 'Book appointment?',

        message:
          'Book an appointment with '
          + doctorName
          + ' on '
          + this.formatDateForDisplay(
            appointmentDate
          )
          + ' at '
          + this.getTimeLabel(
            slot.startTime
          )
          + '?',

        confirmText: 'Book appointment',

        cancelText: 'Review booking',

        icon: 'event_available',

        type: 'default'
      };

    const dialogReference =
      this.dialog.open(
        ConfirmationDialog,
        {
          data: dialogData,
          width: '440px',
          maxWidth:
            'calc(100vw - 32px)',
          panelClass:
            'healthcare-dialog-panel',
          disableClose: true,
          autoFocus: false,
          restoreFocus: true
        }
      );

    dialogReference
      .afterClosed()
      .pipe(
        filter(
          confirmed =>
            confirmed === true
        )
      )
      .subscribe(() => {
        this.submitBooking();
      });
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
      minutes
        .toString()
        .padStart(
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

  getSlotTimeRange(
    slot: AppointmentSlot
  ): string {

    return (
      this.getTimeLabel(slot.startTime)
      + ' - '
      + this.getTimeLabel(slot.endTime)
    );
  }

  private submitBooking(): void {

    if (!this.doctorId) {
      return;
    }

    const formValue =
      this.appointmentForm.getRawValue();

    const slot =
      this.selectedSlot();

    if (!slot) {
      return;
    }

    const request:
      BookAppointmentRequest = {
        doctorId:
          this.doctorId,

        appointmentDate:
          formValue.appointmentDate,

        startTime:
          slot.startTime,

        reasonForVisit:
          formValue.reasonForVisit.trim()
      };

    this.appointmentState.bookAppointment(
      request,
      appointment => {

        this.notificationMessage.showSuccess(
          'Appointment booked successfully.'
        );

        this.router.navigate([
          '/member/appointments',
          appointment.id
        ]);
      },
      message => {

        this.notificationMessage.showError(
          message
        );

        this.loadSlotsForSelectedDate();
      }
    );
  }

  private getTodayDateValue(): string {

    const today =
      new Date();

    const year =
      today.getFullYear();

    const month =
      String(
        today.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const day =
      String(
        today.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${year}-${month}-${day}`;
  }

  private formatDateForDisplay(
    dateValue: string
  ): string {

    const dateParts =
      dateValue
        .split('-')
        .map(Number);

    if (
      dateParts.length !== 3
      || dateParts.some(value => {
        return Number.isNaN(value);
      })
    ) {
      return dateValue;
    }

    const localDate =
      new Date(
        dateParts[0],
        dateParts[1] - 1,
        dateParts[2]
      );

    return localDate.toLocaleDateString(
      'en-IN',
      {
        day: 'numeric',
        month: 'short',
        year: 'numeric'
      }
    );
  }
}