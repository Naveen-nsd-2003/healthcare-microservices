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
  MatDialog
} from '@angular/material/dialog';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  filter
} from 'rxjs';

import {
  ConfirmationDialog
} from '../../../../shared/dialogs/confirmation-dialog/confirmation-dialog';

import {
  ConfirmationDialogData
} from '../../../../shared/models/confirmation-dialog-data';

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
  selector: 'app-payer-policy-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payer-policy-details.html',
  styleUrl: './payer-policy-details.scss'
})
export class PayerPolicyDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly dialog =
    inject(MatDialog);

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
      .loadPayerPolicyById(policyId);
  }

  ngOnDestroy(): void {

    this.policyState
      .clearSelectedPolicy();
  }

  goBack(): void {

    this.router.navigate([
      '/payer/policies'
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
      '/payer/enrollments',
      policy.enrollmentId
    ]);
  }

  requestActivation(): void {

    const policy =
      this.policyState.selectedPolicy();

    if (
      !policy
      || policy.status !== 'SUSPENDED'
    ) {

      return;
    }

    this.openStatusConfirmation(
      policy,
      'ACTIVE'
    );
  }

  requestSuspension(): void {

    const policy =
      this.policyState.selectedPolicy();

    if (
      !policy
      || policy.status !== 'ACTIVE'
    ) {

      return;
    }

    this.openStatusConfirmation(
      policy,
      'SUSPENDED'
    );
  }

  requestCancellation(): void {

    const policy =
      this.policyState.selectedPolicy();

    if (
      !policy
      || (
        policy.status !== 'ACTIVE'
        && policy.status !== 'SUSPENDED'
      )
    ) {

      return;
    }

    this.openStatusConfirmation(
      policy,
      'CANCELLED'
    );
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

    const endDate =
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

    const millisecondsPerDay =
      1000
      * 60
      * 60
      * 24;

    const difference =
      endDate.getTime()
      - today.getTime();

    return Math.max(
      0,
      Math.ceil(
        difference
        / millisecondsPerDay
      )
    );
  }

  getWaitingPeriodLabel(
    policy: InsurancePolicy
  ): string {

    if (policy.waitingPeriodDays === 0) {
      return 'No waiting period';
    }

    if (policy.waitingPeriodDays === 1) {
      return '1 day';
    }

    return (
      policy.waitingPeriodDays
      + ' days'
    );
  }

  getPolicyDurationLabel(
    policy: InsurancePolicy
  ): string {

    if (policy.policyDurationMonths === 1) {
      return '1 month';
    }

    return (
      policy.policyDurationMonths
      + ' months'
    );
  }

  private openStatusConfirmation(
    policy: InsurancePolicy,
    targetStatus: PolicyStatus
  ): void {

    if (this.policyState.updating()) {
      return;
    }

    const dialogData =
      this.createDialogData(
        policy,
        targetStatus
      );

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
      .subscribe(
        () => {

          this.updateStatus(
            policy.id,
            targetStatus
          );
        }
      );
  }

  private updateStatus(
    policyId: string,
    targetStatus: PolicyStatus
  ): void {

    this.policyState
      .updatePolicyStatus(
        policyId,
        targetStatus,
        policy => {

          this.notificationMessage
            .showSuccess(
              this.getSuccessMessage(
                policy.status
              )
            );
        },
        message => {

          this.notificationMessage
            .showError(message);
        }
      );
  }

  private createDialogData(
    policy: InsurancePolicy,
    targetStatus: PolicyStatus
  ): ConfirmationDialogData {

    switch (targetStatus) {

      case 'ACTIVE':
        return {
          title: 'Reactivate policy?',

          message:
            'Reactivate policy '
            + policy.policyNumber
            + ' for '
            + policy.memberEmail
            + '? The Member will regain eligible '
            + 'policy coverage.',

          confirmText: 'Reactivate policy',

          cancelText: 'Keep suspended',

          icon: 'play_circle',

          type: 'default'
        };

      case 'SUSPENDED':
        return {
          title: 'Suspend policy?',

          message:
            'Suspend policy '
            + policy.policyNumber
            + ' for '
            + policy.memberEmail
            + '? The Member may temporarily lose '
            + 'coverage eligibility.',

          confirmText: 'Suspend policy',

          cancelText: 'Keep active',

          icon: 'pause_circle',

          type: 'danger'
        };

      case 'CANCELLED':
        return {
          title: 'Cancel policy?',

          message:
            'Cancel policy '
            + policy.policyNumber
            + ' for '
            + policy.memberEmail
            + '? Cancellation is a final action and '
            + 'cannot be reversed.',

          confirmText: 'Cancel policy',

          cancelText: 'Keep policy',

          icon: 'cancel',

          type: 'danger'
        };

      default:
        return {
          title: 'Update policy status?',

          message:
            'Confirm the requested policy '
            + 'status change.',

          confirmText: 'Confirm',

          cancelText: 'Cancel',

          icon: 'policy',

          type: 'default'
        };
    }
  }

  private getSuccessMessage(
    status: PolicyStatus
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'Policy reactivated successfully.';

      case 'SUSPENDED':
        return 'Policy suspended successfully.';

      case 'CANCELLED':
        return 'Policy cancelled successfully.';

      case 'EXPIRED':
        return 'Policy marked as expired successfully.';

      default:
        return 'Policy status updated successfully.';
    }
  }

  private parseLocalDate(
    dateValue: string
  ): Date {

    const dateParts =
      dateValue
        .split('-')
        .map(Number);

    return new Date(
      dateParts[0],
      dateParts[1] - 1,
      dateParts[2]
    );
  }
}
