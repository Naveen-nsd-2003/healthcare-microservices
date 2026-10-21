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
  InsurancePlan
} from '../../models/insurance-plan';

import {
  InsurancePlanStatus
} from '../../models/insurance-plan-status';

import {
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

@Component({
  selector: 'app-payer-plan-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payer-plan-details.html',
  styleUrl: './payer-plan-details.scss'
})
export class PayerPlanDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly dialog =
    inject(MatDialog);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly planState =
    inject(InsurancePlanStateService);

  ngOnInit(): void {

    const planId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('planId');

    if (!planId) {

      this.notificationMessage.showError(
        'Insurance plan identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.planState.loadMyPlanById(
      planId
    );
  }

  ngOnDestroy(): void {

    this.planState.clearSelectedPlan();
  }

  goBack(): void {

    this.router.navigate([
      '/payer/insurance-plans'
    ]);
  }

  editPlan(): void {

    const plan =
      this.planState.selectedPlan();

    if (!plan) {

      this.notificationMessage.showError(
        'Insurance plan information is unavailable.'
      );

      return;
    }

    this.router.navigate([
      '/payer/insurance-plans',
      plan.id,
      'edit'
    ]);
  }

  requestActivation(): void {

    const plan =
      this.planState.selectedPlan();

    if (
      !plan
      || (
        plan.status !== 'DRAFT'
        && plan.status !== 'INACTIVE'
      )
    ) {
      return;
    }

    this.openStatusConfirmation(
      plan,
      'ACTIVE'
    );
  }

  requestDeactivation(): void {

    const plan =
      this.planState.selectedPlan();

    if (
      !plan
      || plan.status !== 'ACTIVE'
    ) {
      return;
    }

    this.openStatusConfirmation(
      plan,
      'INACTIVE'
    );
  }

  getStatusClass(
    status: InsurancePlanStatus
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'status-badge--active';

      case 'INACTIVE':
        return 'status-badge--inactive';

      case 'DRAFT':
      default:
        return 'status-badge--draft';
    }
  }

  getStatusIcon(
    status: InsurancePlanStatus
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'check_circle';

      case 'INACTIVE':
        return 'block';

      case 'DRAFT':
      default:
        return 'edit_note';
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

  getAgeEligibilityLabel(
    plan: InsurancePlan
  ): string {

    if (
      plan.minimumAge === 0
      && plan.maximumAge === 120
    ) {
      return 'All ages';
    }

    return (
      plan.minimumAge
      + ' to '
      + plan.maximumAge
      + ' years'
    );
  }

  getPolicyDurationLabel(
    plan: InsurancePlan
  ): string {

    if (plan.policyDurationMonths === 1) {
      return '1 month';
    }

    return (
      plan.policyDurationMonths
      + ' months'
    );
  }

  getWaitingPeriodLabel(
    plan: InsurancePlan
  ): string {

    if (plan.waitingPeriodDays === 0) {
      return 'No waiting period';
    }

    if (plan.waitingPeriodDays === 1) {
      return '1 day';
    }

    return (
      plan.waitingPeriodDays
      + ' days'
    );
  }

  private openStatusConfirmation(
    plan: InsurancePlan,
    targetStatus: InsurancePlanStatus
  ): void {

    if (this.planState.statusUpdating()) {
      return;
    }

    const dialogData =
      this.createDialogData(
        plan,
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

          this.updatePlanStatus(
            plan.id,
            targetStatus
          );
        }
      );
  }

  private updatePlanStatus(
    planId: string,
    targetStatus: InsurancePlanStatus
  ): void {

    this.planState.updateMyPlanStatus(
      planId,
      targetStatus,
      plan => {

        this.notificationMessage.showSuccess(
          this.getSuccessMessage(
            plan.status
          )
        );
      },
      message => {

        this.notificationMessage.showError(
          message
        );
      }
    );
  }

  private createDialogData(
    plan: InsurancePlan,
    targetStatus: InsurancePlanStatus
  ): ConfirmationDialogData {

    if (targetStatus === 'ACTIVE') {

      const activationDescription =
        plan.status === 'DRAFT'
          ? 'Activating this draft will make it available to eligible Members.'
          : 'Reactivating this plan will make it available to eligible Members again.';

      return {
        title:
          plan.status === 'DRAFT'
            ? 'Activate insurance plan?'
            : 'Reactivate insurance plan?',

        message:
          activationDescription
          + ' Plan: '
          + plan.planName
          + ' ('
          + plan.planCode
          + ').',

        confirmText:
          plan.status === 'DRAFT'
            ? 'Activate plan'
            : 'Reactivate plan',

        cancelText:
          plan.status === 'DRAFT'
            ? 'Keep as draft'
            : 'Keep inactive',

        icon: 'check_circle',

        type: 'default'
      };
    }

    return {
      title: 'Make insurance plan inactive?',

      message:
        'Make '
        + plan.planName
        + ' ('
        + plan.planCode
        + ') inactive? '
        + 'The plan will no longer be available for '
        + 'new Member enrollments. Existing policies '
        + 'will not be changed automatically.',

      confirmText: 'Make inactive',

      cancelText: 'Keep active',

      icon: 'block',

      type: 'danger'
    };
  }

  private getSuccessMessage(
    status: InsurancePlanStatus
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'Insurance plan activated successfully.';

      case 'INACTIVE':
        return 'Insurance plan made inactive successfully.';

      case 'DRAFT':
        return 'Insurance plan returned to draft successfully.';

      default:
        return 'Insurance plan status updated successfully.';
    }
  }
}