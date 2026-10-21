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
  MemberProfileStateService
} from '../../services/member-profile-state.service';

import {
  AppointmentStateService
} from '../../../appointment/services/appointment-state.service';

import {
  Appointment
} from '../../../appointment/models/appointment';

import {
  EnrollmentStateService
} from '../../../insurance/services/enrollment-state.service';

import {
  Enrollment
} from '../../../insurance/models/enrollment';

import {
  PolicyStateService
} from '../../../insurance/services/policy-state.service';

import {
  InsurancePolicy
} from '../../../insurance/models/insurance-policy';

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
  selector: 'app-member-dashboard',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './member-dashboard.html',
  styleUrl: './member-dashboard.scss'
})
export class MemberDashboard
  implements OnInit, OnDestroy {

  private readonly router =
    inject(Router);

  readonly profileState =
    inject(MemberProfileStateService);

  readonly appointmentState =
    inject(AppointmentStateService);

  readonly enrollmentState =
    inject(EnrollmentStateService);

  readonly policyState =
    inject(PolicyStateService);

  readonly medicalRecordState =
    inject(MedicalRecordStateService);

  readonly claimState =
    inject(ClaimStateService);

  readonly notificationState =
    inject(NotificationStateService);

  readonly dashboardLoading =
    computed(() => {
      return this.profileState.loading()
        || this.appointmentState.loading()
        || this.enrollmentState.loading()
        || this.policyState.loading()
        || this.medicalRecordState.listLoading()
        || this.claimState.listLoading();
    });

  readonly hasDashboardData =
    computed(() => {
      return this.profileState.profile() !== null
        || this.appointmentState
          .appointments().length > 0
        || this.enrollmentState
          .enrollments().length > 0
        || this.policyState
          .policies().length > 0
        || this.medicalRecordState
          .memberRecords().length > 0
        || this.claimState
          .memberClaims().length > 0;
    });

  readonly errorMessages =
    computed(() => {
      const messages = [
        this.profileState.errorMessage(),
        this.appointmentState.errorMessage(),
        this.enrollmentState.errorMessage(),
        this.policyState.errorMessage(),
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

  readonly fullName =
    computed(() => {
      const profile =
        this.profileState.profile();

      if (!profile) {
        return 'Healthcare Member';
      }

      return (
        profile.firstName.trim()
        + ' '
        + profile.lastName.trim()
      ).trim();
    });

  readonly profileStatusLabel =
    computed(() => {
      const profile =
        this.profileState.profile();

      if (!profile) {
        return 'Profile required';
      }

      return profile.status === 'ACTIVE'
        ? 'Active'
        : 'Inactive';
    });

  readonly profileStatusIcon =
    computed(() => {
      const profile =
        this.profileState.profile();

      if (!profile) {
        return 'warning_amber';
      }

      return profile.status === 'ACTIVE'
        ? 'verified'
        : 'pause_circle';
    });

  readonly totalEnrollmentCount =
    computed(() => {
      return this.enrollmentState
        .enrollments()
        .length;
    });

  readonly totalPolicyCoverage =
    computed(() => {
      return this.policyState
        .policies()
        .filter(policy => {
          return policy.status === 'ACTIVE';
        })
        .reduce((
          total,
          policy
        ) => {
          return total
            + this.toFiniteNumber(
              policy.coverageAmount
            );
        }, 0);
    });

  readonly activePolicyMonthlyPremium =
    computed(() => {
      return this.policyState
        .policies()
        .filter(policy => {
          return policy.status === 'ACTIVE';
        })
        .reduce((
          total,
          policy
        ) => {
          return total
            + this.toFiniteNumber(
              policy.monthlyPremium
            );
        }, 0);
    });

  readonly upcomingAppointments =
    computed(() => {
      const now =
        new Date().getTime();

      return this.appointmentState
        .appointments()
        .filter(appointment => {
          if (
            appointment.status === 'COMPLETED'
            || appointment.status === 'CANCELLED'
            || appointment.status === 'NO_SHOW'
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

  readonly recentMedicalRecords =
    computed(() => {
      return [
        ...this.medicalRecordState
          .memberRecords()
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

  readonly upcomingFollowUpCount =
    computed(() => {
      const today =
        this.getTodayDateValue();

      return this.medicalRecordState
        .memberRecords()
        .filter(record => {
          return record.followUpDate !== null
            && record.followUpDate >= today;
        })
        .length;
    });

  readonly totalClaimCount =
    computed(() => {
      return this.claimState
        .memberClaims()
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

  readonly activeClaimCount =
    computed(() => {
      return this.submittedClaimCount()
        + this.underReviewClaimCount();
    });

  readonly totalClaimedAmount =
    computed(() => {
      return this.claimState
        .memberClaims()
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
        .memberClaims()
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
        ...this.claimState.memberClaims()
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

  readonly recentEnrollments =
    computed(() => {
      return [
        ...this.enrollmentState
          .enrollments()
      ]
        .sort((
          firstEnrollment,
          secondEnrollment
        ) => {
          return this.getTimestamp(
            secondEnrollment.createdAt
          ) - this.getTimestamp(
            firstEnrollment.createdAt
          );
        })
        .slice(
          0,
          3
        );
    });

  readonly activePolicies =
    computed(() => {
      return this.policyState
        .policies()
        .filter(policy => {
          return policy.status === 'ACTIVE';
        })
        .sort((
          firstPolicy,
          secondPolicy
        ) => {
          return this.getTimestamp(
            secondPolicy.createdAt
          ) - this.getTimestamp(
            firstPolicy.createdAt
          );
        })
        .slice(
          0,
          3
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

    this.profileState.loadProfile();

    this.appointmentState
      .loadMyAppointments();

    this.enrollmentState
      .loadMyEnrollments();

    this.policyState
      .loadMyPolicies();

    this.medicalRecordState
      .loadMyMedicalRecords();

    this.claimState
      .loadMyClaims();

    this.notificationState
      .loadUnreadCount();
  }

  viewProfile(): void {
    this.navigateTo(
      '/member/profile'
    );
  }

  findProviders(): void {
    this.navigateTo(
      '/member/providers'
    );
  }

  browseInsurancePlans(): void {
    this.navigateTo(
      '/member/insurance-plans'
    );
  }

  viewEnrollments(): void {
    this.navigateTo(
      '/member/enrollments'
    );
  }

  viewEnrollment(
    enrollment: Enrollment
  ): void {
    this.navigateTo(
      '/member/enrollments',
      enrollment.id
    );
  }

  viewPolicies(): void {
    this.navigateTo(
      '/member/policies'
    );
  }

  viewPolicy(
    policy: InsurancePolicy
  ): void {
    this.navigateTo(
      '/member/policies',
      policy.id
    );
  }

  viewAppointments(): void {
    this.navigateTo(
      '/member/appointments'
    );
  }

  viewAppointment(
    appointment: Appointment
  ): void {
    this.navigateTo(
      '/member/appointments',
      appointment.id
    );
  }

  viewMedicalRecords(): void {
    this.navigateTo(
      '/member/medical-records'
    );
  }

  viewMedicalRecord(
    medicalRecord: MedicalRecord
  ): void {
    this.navigateTo(
      '/member/medical-records',
      medicalRecord.id
    );
  }

  viewClaims(): void {
    this.navigateTo(
      '/member/claims'
    );
  }

  viewClaim(
    claim: Claim
  ): void {
    this.navigateTo(
      '/member/claims',
      claim.id
    );
  }

  viewNotifications(): void {
    this.navigateTo(
      '/member/notifications'
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

  getEnrollmentStatusLabel(
    status: string
  ): string {
    if (status === 'APPROVED') {
      return 'Approved';
    }

    if (status === 'REJECTED') {
      return 'Rejected';
    }

    if (status === 'CANCELLED') {
      return 'Cancelled';
    }

    return 'Pending';
  }

  getPolicyStatusLabel(
    status: string
  ): string {
    if (status === 'SUSPENDED') {
      return 'Suspended';
    }

    if (status === 'EXPIRED') {
      return 'Expired';
    }

    if (status === 'CANCELLED') {
      return 'Cancelled';
    }

    return 'Active';
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

  private clearErrors(): void {
    this.profileState.clearError();

    this.appointmentState.clearError();

    this.enrollmentState.clearError();

    this.policyState.clearError();

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
      .memberClaims()
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
