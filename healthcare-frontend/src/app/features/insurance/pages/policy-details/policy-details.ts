import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  CurrencyPipe,
  DatePipe
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
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  InsurancePolicy
} from '../../models/insurance-policy';

import {
  PolicyStatus
} from '../../models/policy-status';

import {
  PolicyStateService
} from '../../services/policy-state.service';

@Component({
  selector: 'app-policy-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './policy-details.html',
  styleUrl: './policy-details.scss'
})
export class PolicyDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly policyState =
    inject(PolicyStateService);

  ngOnInit(): void {

    const policyId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('policyId');

    if (!policyId) {

      this.notificationMessage
        .showError(
          'Policy identifier is missing.'
        );

      this.goBack();

      return;
    }

    this.policyState
      .loadMyPolicyById(policyId);
  }

  ngOnDestroy(): void {

    this.policyState
      .clearSelectedPolicy();
  }

  goBack(): void {

    this.router.navigate([
      '/member/policies'
    ]);
  }

  viewEnrollment(
    policy: InsurancePolicy
  ): void {

    if (!policy.enrollmentId) {

      this.notificationMessage
        .showError(
          'Enrollment information is unavailable '
          + 'for this policy.'
        );

      return;
    }

    this.router.navigate([
      '/member/enrollments',
      policy.enrollmentId
    ]);
  }

  getStatusClass(
    status: PolicyStatus
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'status-badge--active';

      case 'SUSPENDED':
        return 'status-badge--suspended';

      case 'EXPIRED':
        return 'status-badge--expired';

      case 'CANCELLED':
        return 'status-badge--cancelled';

      default:
        return '';
    }
  }

  getStatusIcon(
    status: PolicyStatus
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'verified';

      case 'SUSPENDED':
        return 'pause_circle';

      case 'EXPIRED':
        return 'event_busy';

      case 'CANCELLED':
        return 'block';

      default:
        return 'policy';
    }
  }

  getCoverageIcon(
    enabled: boolean
  ): string {

    if (enabled) {
      return 'check_circle';
    }

    return 'cancel';
  }

  getCoverageLabel(
    enabled: boolean
  ): string {

    if (enabled) {
      return 'Included';
    }

    return 'Not included';
  }

  getRemainingDays(
    policy: InsurancePolicy
  ): number {

    if (
      policy.status === 'EXPIRED'
      || policy.status === 'CANCELLED'
    ) {

      return 0;
    }

    const coverageEndDate =
      this.parseLocalDate(
        policy.coverageEndDate
      );

    const today =
      new Date();

    today.setHours(
      0,
      0,
      0,
      0
    );

    const differenceInMilliseconds =
      coverageEndDate.getTime()
      - today.getTime();

    const millisecondsPerDay =
      1000
      * 60
      * 60
      * 24;

    const remainingDays =
      Math.ceil(
        differenceInMilliseconds
        / millisecondsPerDay
      );

    return Math.max(
      0,
      remainingDays
    );
  }

  getPolicyDurationLabel(
    policy: InsurancePolicy
  ): string {

    if (
      policy.policyDurationMonths === 1
    ) {

      return '1 month';
    }

    return (
      policy.policyDurationMonths
      + ' months'
    );
  }

  getWaitingPeriodLabel(
    policy: InsurancePolicy
  ): string {

    if (
      policy.waitingPeriodDays === 0
    ) {

      return 'No waiting period';
    }

    if (
      policy.waitingPeriodDays === 1
    ) {

      return '1 day';
    }

    return (
      policy.waitingPeriodDays
      + ' days'
    );
  }

  private parseLocalDate(
    dateValue: string
  ): Date {

    const dateParts =
      dateValue
        .split('-')
        .map(Number);

    const year =
      dateParts[0];

    const month =
      dateParts[1];

    const day =
      dateParts[2];

    return new Date(
      year,
      month - 1,
      day
    );
  }
}