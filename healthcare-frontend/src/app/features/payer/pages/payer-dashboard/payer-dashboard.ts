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
  PayerOrganizationStateService
} from '../../../insurance/services/payer-organization-state.service';

import {
  InsurancePlanStateService
} from '../../../insurance/services/insurance-plan-state.service';

import {
  EnrollmentStateService
} from '../../../insurance/services/enrollment-state.service';

import {
  PolicyStateService
} from '../../../insurance/services/policy-state.service';

import {
  InsurancePlan
} from '../../../insurance/models/insurance-plan';

import {
  Enrollment
} from '../../../insurance/models/enrollment';

import {
  InsurancePolicy
} from '../../../insurance/models/insurance-policy';

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
  selector: 'app-payer-dashboard',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payer-dashboard.html',
  styleUrl: './payer-dashboard.scss'
})
export class PayerDashboard
  implements OnInit, OnDestroy {

  private readonly router =
    inject(Router);

  readonly organizationState =
    inject(
      PayerOrganizationStateService
    );

  readonly planState =
    inject(
      InsurancePlanStateService
    );

  readonly enrollmentState =
    inject(
      EnrollmentStateService
    );

  readonly policyState =
    inject(
      PolicyStateService
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
        || this.planState.loading()
        || this.enrollmentState.loading()
        || this.policyState.loading()
        || this.claimState.listLoading();
    });

  readonly hasDashboardData =
    computed(() => {
      return this.organizationState
        .organization() !== null
        || this.planState
          .plans().length > 0
        || this.enrollmentState
          .enrollments().length > 0
        || this.policyState
          .policies().length > 0
        || this.claimState
          .payerClaims().length > 0;
    });

  readonly errorMessages =
    computed(() => {
      const messages = [
        this.organizationState.errorMessage(),
        this.planState.errorMessage(),
        this.enrollmentState.errorMessage(),
        this.policyState.errorMessage(),
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

  readonly totalEnrollmentCount =
    computed(() => {
      return this.enrollmentState
        .enrollments()
        .length;
    });

  readonly totalPlanCoverage =
    computed(() => {
      return this.planState
        .plans()
        .reduce((
          total,
          plan
        ) => {
          return total
            + this.toFiniteNumber(
              plan.coverageAmount
            );
        }, 0);
    });

  readonly averageMonthlyPremium =
    computed(() => {
      const plans =
        this.planState.plans();

      if (plans.length === 0) {
        return 0;
      }

      const totalPremium =
        plans.reduce((
          total,
          plan
        ) => {
          return total
            + this.toFiniteNumber(
              plan.monthlyPremium
            );
        }, 0);

      return totalPremium
        / plans.length;
    });

  readonly outpatientPlanCount =
    computed(() => {
      return this.planState
        .plans()
        .filter(plan => {
          return plan.outpatientCoverage;
        })
        .length;
    });

  readonly hospitalizationPlanCount =
    computed(() => {
      return this.planState
        .plans()
        .filter(plan => {
          return plan.hospitalizationCoverage;
        })
        .length;
    });

  readonly recentPlans =
    computed(() => {
      return [
        ...this.planState.plans()
      ]
        .sort((
          firstPlan,
          secondPlan
        ) => {
          return this.getTimestamp(
            secondPlan.createdAt
          ) - this.getTimestamp(
            firstPlan.createdAt
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
          5
        );
    });

  readonly totalPolicyCoverage =
    computed(() => {
      return this.policyState
        .policies()
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

  readonly monthlyPolicyPremium =
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

  readonly recentPolicies =
    computed(() => {
      return [
        ...this.policyState.policies()
      ]
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
          5
        );
    });

  readonly totalClaimCount =
    computed(() => {
      return this.claimState
        .payerClaims()
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
        .payerClaims()
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
        .payerClaims()
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
        ...this.claimState.payerClaims()
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

  readonly requiresAttentionCount =
    computed(() => {
      return this.planState.draftCount()
        + this.enrollmentState.pendingCount()
        + this.submittedClaimCount()
        + this.underReviewClaimCount()
        + this.policyState.suspendedCount();
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
      .loadMyOrganization();

    this.planState
      .loadMyPlans();

    this.enrollmentState
      .loadPayerEnrollments();

    this.policyState
      .loadPayerPolicies();

    this.claimState
      .loadPayerClaims();

    this.notificationState
      .loadUnreadCount();
  }

  viewOrganization(): void {
    this.navigateTo(
      '/payer/organization'
    );
  }

  viewPlans(): void {
    this.navigateTo(
      '/payer/insurance-plans'
    );
  }

  createPlan(): void {
    this.navigateTo(
      '/payer/insurance-plans/new'
    );
  }

  viewPlan(
    plan: InsurancePlan
  ): void {
    this.navigateTo(
      '/payer/insurance-plans',
      plan.id
    );
  }

  viewEnrollments(): void {
    this.navigateTo(
      '/payer/enrollments'
    );
  }

  viewEnrollment(
    enrollment: Enrollment
  ): void {
    this.navigateTo(
      '/payer/enrollments',
      enrollment.id
    );
  }

  viewPolicies(): void {
    this.navigateTo(
      '/payer/policies'
    );
  }

  viewPolicy(
    policy: InsurancePolicy
  ): void {
    this.navigateTo(
      '/payer/policies',
      policy.id
    );
  }

  viewClaims(): void {
    this.navigateTo(
      '/payer/claims'
    );
  }

  viewClaim(
    claim: Claim
  ): void {
    this.navigateTo(
      '/payer/claims',
      claim.id
    );
  }

  viewNotifications(): void {
    this.navigateTo(
      '/payer/notifications'
    );
  }

  getPlanStatusLabel(
    status: string
  ): string {
    if (status === 'ACTIVE') {
      return 'Active';
    }

    if (status === 'INACTIVE') {
      return 'Inactive';
    }

    return 'Draft';
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
    this.organizationState.clearError();

    this.planState.clearError();

    this.enrollmentState.clearError();

    this.policyState.clearError();

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
      .payerClaims()
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