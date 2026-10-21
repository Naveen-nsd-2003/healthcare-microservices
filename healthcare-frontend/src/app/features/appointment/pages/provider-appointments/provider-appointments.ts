import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

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
  Appointment
} from '../../models/appointment';

import {
  AppointmentStatus
} from '../../models/appointment-status';

import {
  ProviderAppointmentStateService
} from '../../services/provider-appointment-state.service';

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
  | 'MEMBER_ASCENDING';

interface DoctorAppointmentGroup {
  doctorId: string;
  doctorName: string;
  specialization: string;
  departmentName: string;
  totalAppointments: number;
  bookedAppointments: number;
  confirmedAppointments: number;
  completedAppointments: number;
  appointments: Appointment[];
}

@Component({
  selector: 'app-provider-appointments',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './provider-appointments.html',
  styleUrl: './provider-appointments.scss'
})
export class ProviderAppointments
  implements OnInit {

  private readonly router =
    inject(Router);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  readonly appointmentState =
    inject(ProviderAppointmentStateService);

  private routeDoctorId: string | null =
    null;

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<AppointmentStatusFilter>('ALL');

  readonly selectedPeriod =
    signal<AppointmentPeriodFilter>('ALL');

  readonly selectedDoctorId =
    signal('ALL');

  readonly selectedSort =
    signal<AppointmentSortOption>(
      'DATE_ASCENDING'
    );

  readonly expandedDoctorIds =
    signal<Set<string>>(
      new Set<string>()
    );

  readonly doctorOptions =
    computed(() => {
      const doctors =
        new Map<string, string>();

      this.appointmentState
        .appointments()
        .forEach(appointment => {
          doctors.set(
            appointment.doctorId,
            appointment.doctorName
          );
        });

      return Array.from(
        doctors.entries()
      )
        .map(([id, name]) => {
          return {
            id,
            name
          };
        })
        .sort(
          (firstDoctor, secondDoctor) => {
            return firstDoctor.name.localeCompare(
              secondDoctor.name
            );
          }
        );
    });

  readonly filteredAppointments =
    computed(() => {
      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const status =
        this.selectedStatus();

      const period =
        this.selectedPeriod();

      const doctorId =
        this.selectedDoctorId();

      const sort =
        this.selectedSort();

      const filtered =
        this.appointmentState
          .appointments()
          .filter(appointment => {
            const matchesSearch =
              search.length === 0
              || appointment.memberEmail
                .toLowerCase()
                .includes(search)
              || appointment.doctorName
                .toLowerCase()
                .includes(search)
              || appointment.doctorSpecialization
                .toLowerCase()
                .includes(search)
              || appointment.departmentName
                .toLowerCase()
                .includes(search)
              || appointment.reasonForVisit
                .toLowerCase()
                .includes(search);

            const matchesStatus =
              status === 'ALL'
              || appointment.status === status;

            const upcoming =
              this.isUpcomingAppointment(
                appointment
              );

            const matchesPeriod =
              period === 'ALL'
              || (
                period === 'UPCOMING'
                && upcoming
              )
              || (
                period === 'PAST'
                && !upcoming
              );

            const matchesDoctor =
              doctorId === 'ALL'
              || appointment.doctorId
                === doctorId;

            return matchesSearch
              && matchesStatus
              && matchesPeriod
              && matchesDoctor;
          });

      return [...filtered].sort(
        (
          firstAppointment,
          secondAppointment
        ) => {
          if (sort === 'DATE_DESCENDING') {
            return this.getDateTimeValue(
              secondAppointment
            ) - this.getDateTimeValue(
              firstAppointment
            );
          }

          if (sort === 'MEMBER_ASCENDING') {
            return firstAppointment.memberEmail
              .localeCompare(
                secondAppointment.memberEmail
              );
          }

          return this.getDateTimeValue(
            firstAppointment
          ) - this.getDateTimeValue(
            secondAppointment
          );
        }
      );
    });

  readonly doctorGroups =
    computed<DoctorAppointmentGroup[]>(() => {
      const groups =
        new Map<string, Appointment[]>();

      this.filteredAppointments()
        .forEach(appointment => {
          const doctorAppointments =
            groups.get(
              appointment.doctorId
            ) ?? [];

          doctorAppointments.push(
            appointment
          );

          groups.set(
            appointment.doctorId,
            doctorAppointments
          );
        });

      return Array.from(
        groups.entries()
      )
        .map(([doctorId, appointments]) => {
          const firstAppointment =
            appointments[0];

          return {
            doctorId,

            doctorName:
              firstAppointment.doctorName,

            specialization:
              firstAppointment.doctorSpecialization,

            departmentName:
              firstAppointment.departmentName,

            totalAppointments:
              appointments.length,

            bookedAppointments:
              this.countStatus(
                appointments,
                'BOOKED'
              ),

            confirmedAppointments:
              this.countStatus(
                appointments,
                'CONFIRMED'
              ),

            completedAppointments:
              this.countStatus(
                appointments,
                'COMPLETED'
              ),

            appointments
          };
        })
        .sort(
          (firstGroup, secondGroup) => {
            return firstGroup.doctorName
              .localeCompare(
                secondGroup.doctorName
              );
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
        || this.selectedDoctorId() !== 'ALL'
        || this.selectedSort() !== 'DATE_ASCENDING';
    });

  ngOnInit(): void {
    this.routeDoctorId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('doctorId');

    if (this.routeDoctorId) {
      this.selectedDoctorId.set(
        this.routeDoctorId
      );

      this.expandedDoctorIds.set(
        new Set([
          this.routeDoctorId
        ])
      );
    }

    this.loadAppointments();
  }

  loadAppointments(): void {
    this.appointmentState
      .loadProviderAppointments();
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

  updateDoctor(
    doctorId: string
  ): void {
    this.selectedDoctorId.set(
      doctorId
    );

    if (doctorId !== 'ALL') {
      this.expandedDoctorIds.update(
        currentIds => {
          const updatedIds =
            new Set(currentIds);

          updatedIds.add(doctorId);

          return updatedIds;
        }
      );
    }
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

    if (this.routeDoctorId) {
      this.selectedDoctorId.set(
        this.routeDoctorId
      );
    } else {
      this.selectedDoctorId.set('ALL');
    }
  }

  viewAppointment(
    appointment: Appointment
  ): void {
    this.router.navigate([
      '/provider/appointments',
      appointment.id
    ]);
  }

  toggleDoctorGroup(
    doctorId: string
  ): void {
    this.expandedDoctorIds.update(
      currentIds => {
        const updatedIds =
          new Set(currentIds);

        if (updatedIds.has(doctorId)) {
          updatedIds.delete(doctorId);
        } else {
          updatedIds.add(doctorId);
        }

        return updatedIds;
      }
    );
  }

  isDoctorExpanded(
    doctorId: string
  ): boolean {
    return this.expandedDoctorIds()
      .has(doctorId);
  }

  expandAll(): void {
    this.expandedDoctorIds.set(
      new Set(
        this.doctorGroups()
          .map(group => group.doctorId)
      )
    );
  }

  collapseAll(): void {
    this.expandedDoctorIds.set(
      new Set<string>()
    );
  }

  getDoctorInitials(
    doctorName: string
  ): string {
    const names =
      doctorName
        .trim()
        .split(/\s+/)
        .filter(name => name.length > 0);

    if (names.length === 0) {
      return 'DR';
    }

    if (names.length === 1) {
      return names[0]
        .substring(0, 2)
        .toUpperCase();
    }

    return (
      names[0].charAt(0)
      + names[names.length - 1].charAt(0)
    ).toUpperCase();
  }

  getFormattedDate(
    appointmentDate: string
  ): string {
    const parts =
      appointmentDate.split('-');

    if (parts.length !== 3) {
      return appointmentDate;
    }

    const year =
      Number(parts[0]);

    const month =
      Number(parts[1]);

    const day =
      Number(parts[2]);

    const date =
      new Date(
        year,
        month - 1,
        day
      );

    if (Number.isNaN(date.getTime())) {
      return appointmentDate;
    }

    return new Intl.DateTimeFormat(
      'en-IN',
      {
        day: '2-digit',
        month: 'short',
        year: 'numeric'
      }
    ).format(date);
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

  getStatusLabel(
    status: AppointmentStatus
  ): string {
    if (status === 'NO_SHOW') {
      return 'No show';
    }

    return status.charAt(0)
      + status.slice(1).toLowerCase();
  }

  isUpcomingAppointment(
    appointment: Appointment
  ): boolean {
    if (
      appointment.status === 'COMPLETED'
      || appointment.status === 'CANCELLED'
      || appointment.status === 'NO_SHOW'
    ) {
      return false;
    }

    return this.getDateTimeValue(
      appointment
    ) >= Date.now();
  }

  private countStatus(
    appointments: Appointment[],
    status: AppointmentStatus
  ): number {
    return appointments.filter(
      appointment => {
        return appointment.status === status;
      }
    ).length;
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

    return Number.isNaN(timestamp)
      ? 0
      : timestamp;
  }
}