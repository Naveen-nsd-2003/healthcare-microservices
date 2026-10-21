import {
  Component,
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
  Appointment
} from '../../models/appointment';

import {
  AppointmentStatus
} from '../../models/appointment-status';

import {
  AppointmentStateService
} from '../../services/appointment-state.service';

type AppointmentStatusFilter =
  | 'ALL'
  | AppointmentStatus;

type AppointmentPeriodFilter =
  | 'ALL'
  | 'UPCOMING'
  | 'PAST';

type AppointmentSortOption =
  | 'DATE_ASCENDING'
  | 'DATE_DESCENDING'
  | 'DOCTOR_ASCENDING';

@Component({
  selector: 'app-member-appointments',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './member-appointments.html',
  styleUrl: './member-appointments.scss'
})
export class MemberAppointments
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly appointmentState =
    inject(AppointmentStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<AppointmentStatusFilter>('ALL');

  readonly selectedPeriod =
    signal<AppointmentPeriodFilter>('ALL');

  readonly selectedSort =
    signal<AppointmentSortOption>(
      'DATE_ASCENDING'
    );

  readonly filteredAppointments =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const selectedStatus =
        this.selectedStatus();

      const selectedPeriod =
        this.selectedPeriod();

      const selectedSort =
        this.selectedSort();

      const filtered =
        this.appointmentState
          .appointments()
          .filter(appointment => {

            const matchesSearch =
              search.length === 0
              || appointment.doctorName
                .toLowerCase()
                .includes(search)
              || appointment.doctorSpecialization
                .toLowerCase()
                .includes(search)
              || appointment.providerOrganizationName
                .toLowerCase()
                .includes(search)
              || appointment.departmentName
                .toLowerCase()
                .includes(search)
              || appointment.reasonForVisit
                .toLowerCase()
                .includes(search);

            const matchesStatus =
              selectedStatus === 'ALL'
              || appointment.status
                === selectedStatus;

            const upcoming =
              this.isUpcomingAppointment(
                appointment
              );

            const matchesPeriod =
              selectedPeriod === 'ALL'
              || (
                selectedPeriod === 'UPCOMING'
                && upcoming
              )
              || (
                selectedPeriod === 'PAST'
                && !upcoming
              );

            return matchesSearch
              && matchesStatus
              && matchesPeriod;
          });

      return [...filtered].sort(
        (
          firstAppointment,
          secondAppointment
        ) => {

          switch (selectedSort) {

            case 'DATE_DESCENDING':
              return this.getDateTimeValue(
                secondAppointment
              )
                - this.getDateTimeValue(
                  firstAppointment
                );

            case 'DOCTOR_ASCENDING':
              return firstAppointment.doctorName
                .localeCompare(
                  secondAppointment.doctorName
                );

            case 'DATE_ASCENDING':
            default:
              return this.getDateTimeValue(
                firstAppointment
              )
                - this.getDateTimeValue(
                  secondAppointment
                );
          }
        }
      );
    });

  readonly filtersApplied =
    computed(() => {

      return this.searchText()
        .trim()
        .length > 0
        || this.selectedStatus() !== 'ALL'
        || this.selectedPeriod() !== 'ALL'
        || this.selectedSort() !== 'DATE_ASCENDING';
    });

  ngOnInit(): void {

    this.loadAppointments();
  }

  loadAppointments(): void {

    this.appointmentState
      .loadMyAppointments();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: AppointmentStatusFilter
  ): void {

    this.selectedStatus.set(status);
  }

  updatePeriod(
    period: AppointmentPeriodFilter
  ): void {

    this.selectedPeriod.set(period);
  }

  updateSort(
    sort: AppointmentSortOption
  ): void {

    this.selectedSort.set(sort);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedStatus.set('ALL');
    this.selectedPeriod.set('ALL');
    this.selectedSort.set(
      'DATE_ASCENDING'
    );
  }

  findProvider(): void {

    this.router.navigate([
      '/member/providers'
    ]);
  }

  viewAppointment(
    appointment: Appointment
  ): void {

    this.router.navigate([
      '/member/appointments',
      appointment.id
    ]);
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

    if (status === 'NO_SHOW') {
      return 'No show';
    }

    return status.charAt(0)
      + status
        .slice(1)
        .toLowerCase();
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

  isUpcomingAppointment(
    appointment: Appointment
  ): boolean {

    if (
      appointment.status === 'CANCELLED'
      || appointment.status === 'COMPLETED'
      || appointment.status === 'NO_SHOW'
    ) {
      return false;
    }

    return this.getDateTimeValue(
      appointment
    ) >= Date.now();
  }

  private getDateTimeValue(
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