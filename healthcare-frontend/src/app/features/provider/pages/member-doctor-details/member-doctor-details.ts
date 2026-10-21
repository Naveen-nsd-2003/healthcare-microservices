import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  CurrencyPipe
} from '@angular/common';

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
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  Doctor
} from '../../models/doctor';

import {
  DoctorAvailability
} from '../../models/doctor-availability';

import {
  DayOfWeek
} from '../../models/day-of-week';

import {
  ProviderDiscoveryStateService
} from '../../services/provider-discovery-state.service';

@Component({
  selector: 'app-member-doctor-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './member-doctor-details.html',
  styleUrl: './member-doctor-details.scss'
})
export class MemberDoctorDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  readonly providerState =
    inject(ProviderDiscoveryStateService);

  private doctorId: string | null =
    null;

  ngOnInit(): void {

    this.doctorId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('doctorId');

    if (!this.doctorId) {

      this.goBackToProviders();

      return;
    }

    this.providerState.loadDoctorDetails(
      this.doctorId
    );
  }

  ngOnDestroy(): void {

    this.providerState.clearDoctorDetails();
  }

  retryLoading(): void {

    if (!this.doctorId) {

      this.goBackToProviders();

      return;
    }

    this.providerState.loadDoctorDetails(
      this.doctorId
    );
  }

  goBackToProvider(): void {

    const doctor =
      this.providerState.selectedDoctor();

    if (!doctor) {

      this.goBackToProviders();

      return;
    }

    this.router.navigate([
      '/member/providers',
      doctor.providerId
    ]);
  }

  goBackToProviders(): void {

    this.router.navigate([
      '/member/providers'
    ]);
  }

  getInitials(
    doctor: Doctor
  ): string {

    const firstInitial =
      doctor.firstName
        .trim()
        .charAt(0)
        .toUpperCase();

    const lastInitial =
      doctor.lastName
        .trim()
        .charAt(0)
        .toUpperCase();

    return firstInitial + lastInitial;
  }

  getExperienceLabel(
    yearsOfExperience: number
  ): string {

    if (yearsOfExperience === 0) {
      return 'New practitioner';
    }

    if (yearsOfExperience === 1) {
      return '1 year of experience';
    }

    return (
      yearsOfExperience
      + ' years of experience'
    );
  }

  getDayLabel(
    dayOfWeek: DayOfWeek
  ): string {

    switch (dayOfWeek) {

      case 'MONDAY':
        return 'Monday';

      case 'TUESDAY':
        return 'Tuesday';

      case 'WEDNESDAY':
        return 'Wednesday';

      case 'THURSDAY':
        return 'Thursday';

      case 'FRIDAY':
        return 'Friday';

      case 'SATURDAY':
        return 'Saturday';

      case 'SUNDAY':
        return 'Sunday';

      default:
        return dayOfWeek;
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

  getScheduleTimeRange(
    schedule: DoctorAvailability
  ): string {

    return (
      this.getTimeLabel(
        schedule.startTime
      )
      + ' - '
      + this.getTimeLabel(
        schedule.endTime
      )
    );
  }

  getScheduleCountForDay(
    dayOfWeek: DayOfWeek
  ): number {

    return this.providerState
      .schedules()
      .filter(schedule => {
        return schedule.dayOfWeek
          === dayOfWeek;
      })
      .length;
  }

  getSchedulesForDay(
    dayOfWeek: DayOfWeek
  ): DoctorAvailability[] {

    return this.providerState
      .schedules()
      .filter(schedule => {
        return schedule.dayOfWeek
          === dayOfWeek;
      });
  }

  getAvailableDays(): DayOfWeek[] {

    const days:
      DayOfWeek[] = [
        'MONDAY',
        'TUESDAY',
        'WEDNESDAY',
        'THURSDAY',
        'FRIDAY',
        'SATURDAY',
        'SUNDAY'
      ];

    return days.filter(day => {
      return this.getScheduleCountForDay(day) > 0;
    });
  }

  getWeeklyScheduleCount(): number {

    return this.providerState
      .schedules()
      .length;
  }

  getConsultationDurationLabel(
    minutes: number
  ): string {

    if (minutes === 1) {
      return '1 minute';
    }

    return minutes + ' minutes';
  }

  bookAppointment(): void {

    const doctor =
      this.providerState.selectedDoctor();

    if (!doctor) {
      return;
    }

    this.router.navigate(
      [
        '/member/appointments/new'
      ],
      {
        queryParams: {
          doctorId:
            doctor.id,

          providerId:
            doctor.providerId,

          departmentId:
            doctor.departmentId
        }
      }
    );
  }
}