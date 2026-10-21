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
  MatTooltipModule
} from '@angular/material/tooltip';

import {
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

@Component({
  selector: 'app-plan-details',
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './plan-details.html',
  styleUrl: './plan-details.scss'
})
export class PlanDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  readonly planState =
    inject(InsurancePlanStateService);

  private readonly notificationMessage =
    inject(NotificationMessageService);

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

    this.planState
      .loadAvailablePlanById(planId);
  }

  ngOnDestroy(): void {

    this.planState.clearSelectedPlan();
  }

  goBack(): void {

    this.router.navigate([
      '/member/insurance-plans'
    ]);
  }

  startEnrollment(): void {

  const plan =
    this.planState.selectedPlan();

  if (!plan) {

    this.notificationMessage.showError(
      'Insurance plan information is unavailable.'
    );

    return;
  }

  this.router.navigate(
    ['/member/enrollments/new'],
    {
      queryParams: {
        planId: plan.id
      }
    }
  );
}

  getCoverageValue(
    enabled: boolean
  ): string {

    return enabled
      ? 'Included'
      : 'Not included';
  }

  getCoverageIcon(
    enabled: boolean
  ): string {

    return enabled
      ? 'check_circle'
      : 'cancel';
  }
}