import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject
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
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  ProviderOrganizationStateService
} from '../../services/provider-organization-state.service';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

import {
  DoctorManagementStateService
} from '../../services/doctor-management-state.service';

import {
  AvailabilityManagementStateService
} from '../../services/availability-management-state.service';

import {
  ProviderAppointmentStateService
} from '../../../appointment/services/provider-appointment-state.service';

import {
  Appointment
} from '../../../appointment/models/appointment';

import {
  MedicalRecordStateService
} from '../../../medical-records/services/medical-record-state.service';

import {
  MedicalRecord
} from '../../../medical-records/models/medical-record';

import {
  ClaimStateService
} from '../../../claims/services/claim-state.service';

import {
  Claim
} from '../../../claims/models/claim';

import {
  NotificationStateService
} from '../../../notifications/services/notification-state.service';

@Component({
  selector: 'app-provider-dashboard',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './provider-dashboard.html',
  styleUrl: './provider-dashboard.scss'
})
export class ProviderDashboard
  implements OnInit, OnDestroy {

  private readonly router =
    inject(Router);

  readonly organizationState =
    inject(
      ProviderOrganizationStateService
    );

  readonly departmentState =
    inject(
      DepartmentManagementStateService
    );

  readonly doctorState =
    inject(
      DoctorManagementStateService
    );

  readonly availabilityState =
    inject(
      AvailabilityManagementStateService
    );

  readonly appointmentState =
    inject(
      ProviderAppointmentStateService
    );

  readonly medicalRecordState =
    inject(
      MedicalRecordStateService
    );

  readonly claimState =
    inject(
      ClaimStateService
    );

  readonly notificationState =
    inject(
      NotificationStateService
    );

  readonly dashboardLoading =
    computed(() => {
      return this.organizationState.loading()
        || this.departmentState.loading()
        || this.doctorState.loading()
        || this.availabilityState.loading()
        || this.appointmentState.loading()
        || this.medicalRecordState.listLoading()
        || this.claimState.listLoading();
    });

  readonly hasDashboardData =
    computed(() => {
      return this.organizationState
        .organization() !== null
        || this.departmentState
          .departments().length > 0
        || this.doctorState
          .doctors().length > 0
        || this.availabilityState
          .availabilities().length > 0
        || this.appointmentState
          .appointments().length > 0
        || this.medicalRecordState
          .providerRecords().length > 0
        || this.claimState
          .providerClaims().length > 0;
    });

  readonly errorMessages =
    computed(() => {
      const messages = [
        this.organizationState.errorMessage(),
        this.departmentState.errorMessage(),
        this.doctorState.errorMessage(),
        this.availabilityState.errorMessage(),
        this.appointmentState.errorMessage(),
        this.medicalRecordState.errorMessage(),
        this.claimState.errorMessage(),
        this.notificationState.errorMessage()
      ];

      return [
        ...new Set(
          messages.filter(
            (
              message
            ): message is string => {
              return typeof message === 'string'
                && message.trim().length > 0;
            }
          )
        )
      ];
    });

  readonly organizationStatusLabel =
    computed(() => {
      const organization =
        this.organizationState.organization();

      if (!organization) {
        return 'Setup required';
      }

      return organization.status === 'ACTIVE'
        ? 'Active'
        : 'Inactive';
    });

  readonly organizationStatusIcon =
    computed(() => {
      const organization =
        this.organizationState.organization();

      if (!organization) {
        return 'warning_amber';
      }

      return organization.status === 'ACTIVE'
        ? 'verified'
        : 'pause_circle';
    });

  readonly todayAppointments =
    computed(() => {
      const today =
        this.getTodayDateValue();

      return this.appointmentState
        .appointments()
        .filter(appointment => {
          return appointment.appointmentDate
            === today;
        })
        .sort((
          firstAppointment,
          secondAppointment
        ) => {
          return firstAppointment.startTime
            .localeCompare(
              secondAppointment.startTime
            );
        });
    });

  readonly upcomingAppointments =
    computed(() => {
      const now =
        new Date().getTime();

      return this.appointmentState
        .appointments()
        .filter(appointment => {
          if (
            appointment.status
              === 'CANCELLED'
            || appointment.status
              === 'COMPLETED'
            || appointment.status
              === 'NO_SHOW'
          ) {
            return false;
          }

          return this.getAppointmentTimestamp(
            appointment
          ) >= now;
        })
        .sort((
          firstAppointment,
          secondAppointment
        ) => {
          return this.getAppointmentTimestamp(
            firstAppointment
          ) - this.getAppointmentTimestamp(
            secondAppointment
          );
        })
        .slice(
          0,
          5
        );
    });

  readonly pendingAppointmentCount =
    computed(() => {
      return this.appointmentState
        .bookedCount()
        + this.appointmentState
          .confirmedCount();
    });

  readonly medicalRecordCount =
    computed(() => {
      return this.medicalRecordState
        .providerRecords()
        .length;
    });

  readonly recentMedicalRecords =
    computed(() => {
      return [
        ...this.medicalRecordState
          .providerRecords()
      ]
        .sort((
          firstRecord,
          secondRecord
        ) => {
          return this.getTimestamp(
            secondRecord.updatedAt
          ) - this.getTimestamp(
            firstRecord.updatedAt
          );
        })
        .slice(
          0,
          5
        );
    });

  readonly totalClaimCount =
    computed(() => {
      return this.claimState
        .providerClaims()
        .length;
    });

  readonly submittedClaimCount =
    computed(() => {
      return this.countClaimsByStatus(
        'SUBMITTED'
      );
    });

  readonly underReviewClaimCount =
    computed(() => {
      return this.countClaimsByStatus(
        'UNDER_REVIEW'
      );
    });

  readonly approvedClaimCount =
    computed(() => {
      return this.countClaimsByStatus(
        'APPROVED'
      );
    });

  readonly rejectedClaimCount =
    computed(() => {
      return this.countClaimsByStatus(
        'REJECTED'
      );
    });

  readonly cancelledClaimCount =
    computed(() => {
      return this.countClaimsByStatus(
        'CANCELLED'
      );
    });

  readonly pendingClaimCount =
    computed(() => {
      return this.submittedClaimCount()
        + this.underReviewClaimCount();
    });

  readonly totalClaimedAmount =
    computed(() => {
      return this.claimState
        .providerClaims()
        .reduce((
          total,
          claim
        ) => {
          return total
            + this.toFiniteNumber(
              claim.claimedAmount
            );
        }, 0);
    });

  readonly totalApprovedAmount =
    computed(() => {
      return this.claimState
        .providerClaims()
        .reduce((
          total,
          claim
        ) => {
          return total
            + this.toFiniteNumber(
              claim.approvedAmount
            );
        }, 0);
    });

  readonly recentClaims =
    computed(() => {
      return [
        ...this.claimState.providerClaims()
      ]
        .sort((
          firstClaim,
          secondClaim
        ) => {
          return this.getTimestamp(
            secondClaim.submittedAt
          ) - this.getTimestamp(
            firstClaim.submittedAt
          );
        })
        .slice(
          0,
          5
        );
    });

  ngOnInit(): void {
    this.loadDashboard();
  }

  ngOnDestroy(): void {
    this.clearErrors();
  }

  loadDashboard(): void {
    this.clearErrors();

    this.organizationState
      .loadMyProviderOrganization();

    this.departmentState
      .loadMyDepartments();

    this.doctorState
      .loadMyDoctors();

    this.availabilityState
      .loadMyAvailabilities();

    this.appointmentState
      .loadProviderAppointments();

    this.medicalRecordState
      .loadProviderMedicalRecords();

    this.claimState
      .loadProviderClaims();

    this.notificationState
      .loadUnreadCount();
  }

  viewOrganization(): void {
    this.navigateTo(
      '/provider/organization'
    );
  }

  viewDepartments(): void {
    this.navigateTo(
      '/provider/departments'
    );
  }

  createDepartment(): void {
    this.navigateTo(
      '/provider/departments/new'
    );
  }

  viewDoctors(): void {
    this.navigateTo(
      '/provider/doctors'
    );
  }

  createDoctor(): void {
    this.navigateTo(
      '/provider/doctors/new'
    );
  }

  viewAvailability(): void {
    this.navigateTo(
      '/provider/availability'
    );
  }

  createAvailability(): void {
    this.navigateTo(
      '/provider/availability/new'
    );
  }

  viewAppointments(): void {
    this.navigateTo(
      '/provider/appointments'
    );
  }

  viewAppointment(
    appointment: Appointment
  ): void {
    this.navigateTo(
      '/provider/appointments',
      appointment.id
    );
  }

  viewMedicalRecords(): void {
    this.navigateTo(
      '/provider/medical-records'
    );
  }

  viewMedicalRecord(
    medicalRecord: MedicalRecord
  ): void {
    this.navigateTo(
      '/provider/medical-records',
      medicalRecord.id
    );
  }

  viewClaims(): void {
    this.navigateTo(
      '/provider/claims'
    );
  }

  viewClaim(
    claim: Claim
  ): void {
    this.navigateTo(
      '/provider/claims',
      claim.id
    );
  }

  viewNotifications(): void {
    this.navigateTo(
      '/provider/notifications'
    );
  }

  getAppointmentStatusLabel(
    status: string
  ): string {
    if (status === 'BOOKED') {
      return 'Booked';
    }

    if (status === 'CONFIRMED') {
      return 'Confirmed';
    }

    if (status === 'COMPLETED') {
      return 'Completed';
    }

    if (status === 'CANCELLED') {
      return 'Cancelled';
    }

    if (status === 'NO_SHOW') {
      return 'No-show';
    }

    return status;
  }

  getClaimStatusLabel(
    status: string
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Under review';
    }

    if (status === 'APPROVED') {
      return 'Approved';
    }

    if (status === 'REJECTED') {
      return 'Rejected';
    }

    if (status === 'CANCELLED') {
      return 'Cancelled';
    }

    return 'Submitted';
  }

  getProviderTypeLabel(
    providerType: string
  ): string {
    return providerType
      .replace(
        /_/g,
        ' '
      )
      .toLowerCase()
      .replace(
        /\b\w/g,
        character => {
          return character.toUpperCase();
        }
      );
  }

  private clearErrors(): void {
    this.organizationState.clearError();

    this.departmentState.clearError();

    this.doctorState.clearError();

    this.availabilityState.clearError();

    this.appointmentState.clearError();

    this.medicalRecordState.clearError();

    this.claimState.clearError();

    this.notificationState.clearError();
  }

  private countClaimsByStatus(
    status:
      | 'SUBMITTED'
      | 'UNDER_REVIEW'
      | 'APPROVED'
      | 'REJECTED'
      | 'CANCELLED'
  ): number {
    return this.claimState
      .providerClaims()
      .filter(claim => {
        return claim.status === status;
      })
      .length;
  }

  private navigateTo(
    route: string,
    identifier?: string
  ): void {
    if (
      identifier
      && identifier.trim().length > 0
    ) {
      this.router.navigate([
        route,
        identifier
      ]);

      return;
    }

    this.router.navigate([
      route
    ]);
  }

  private getTodayDateValue(): string {
    const now =
      new Date();

    const year =
      now.getFullYear();

    const month =
      String(
        now.getMonth() + 1
      ).padStart(
        2,
        '0'
      );

    const day =
      String(
        now.getDate()
      ).padStart(
        2,
        '0'
      );

    return `${year}-${month}-${day}`;
  }

  private getAppointmentTimestamp(
    appointment: Appointment
  ): number {
    return this.getTimestamp(
      appointment.appointmentDate
      + 'T'
      + appointment.startTime
    );
  }

  private getTimestamp(
    value: string | null | undefined
  ): number {
    if (!value) {
      return 0;
    }

    const timestamp =
      new Date(value).getTime();

    return Number.isNaN(timestamp)
      ? 0
      : timestamp;
  }

  private toFiniteNumber(
    value: number | null | undefined
  ): number {
    if (
      value === null
      || value === undefined
      || !Number.isFinite(value)
    ) {
      return 0;
    }

    return value;
  }
}