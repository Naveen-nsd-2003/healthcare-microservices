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
  InsurancePlan
} from '../../models/insurance-plan';

import {
  InsurancePlanStatus
} from '../../models/insurance-plan-status';

import {
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

type PlanStatusFilter =
  | 'ALL'
  | InsurancePlanStatus;

type PlanSortOption =
  | 'NEWEST'
  | 'NAME_ASCENDING'
  | 'COVERAGE_HIGH_TO_LOW'
  | 'PREMIUM_LOW_TO_HIGH';

@Component({
  selector: 'app-payer-insurance-plans',
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
  templateUrl: './payer-insurance-plans.html',
  styleUrl: './payer-insurance-plans.scss'
})
export class PayerInsurancePlans
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly planState =
    inject(InsurancePlanStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<PlanStatusFilter>('ALL');

  readonly selectedSort =
    signal<PlanSortOption>('NEWEST');

  readonly filteredPlans =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const status =
        this.selectedStatus();

      const sort =
        this.selectedSort();

      const filtered =
        this.planState
          .plans()
          .filter(plan => {

            const matchesStatus =
              status === 'ALL'
              || plan.status === status;

            const matchesSearch =
              search.length === 0
              || plan.planName
                .toLowerCase()
                .includes(search)
              || plan.planCode
                .toLowerCase()
                .includes(search)
              || plan.description
                .toLowerCase()
                .includes(search);

            return matchesStatus
              && matchesSearch;
          });

      return [...filtered].sort(
        (
          firstPlan,
          secondPlan
        ) => {

          switch (sort) {

            case 'NAME_ASCENDING':
              return firstPlan.planName
                .localeCompare(
                  secondPlan.planName
                );

            case 'COVERAGE_HIGH_TO_LOW':
              return secondPlan.coverageAmount
                - firstPlan.coverageAmount;

            case 'PREMIUM_LOW_TO_HIGH':
              return firstPlan.monthlyPremium
                - secondPlan.monthlyPremium;

            case 'NEWEST':
            default:
              return this.getDateValue(
                secondPlan.createdAt
              )
                - this.getDateValue(
                  firstPlan.createdAt
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
        || this.selectedSort() !== 'NEWEST';
    });

  ngOnInit(): void {

    this.loadPlans();
  }

  loadPlans(): void {

    this.planState.loadMyPlans();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: PlanStatusFilter
  ): void {

    this.selectedStatus.set(status);
  }

  updateSort(
    sort: PlanSortOption
  ): void {

    this.selectedSort.set(sort);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedStatus.set('ALL');
    this.selectedSort.set('NEWEST');
  }

  createPlan(): void {

    this.router.navigate([
      '/payer/insurance-plans/new'
    ]);
  }

  viewPlan(
    plan: InsurancePlan
  ): void {

    this.router.navigate([
      '/payer/insurance-plans',
      plan.id
    ]);
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

  getCoverageFeatures(
    plan: InsurancePlan
  ): string[] {

    const features: string[] = [];

    if (plan.hospitalizationCoverage) {
      features.push('Hospitalization');
    }

    if (plan.outpatientCoverage) {
      features.push('Outpatient');
    }

    if (plan.prescriptionCoverage) {
      features.push('Prescription');
    }

    if (plan.maternityCoverage) {
      features.push('Maternity');
    }

    return features;
  }

  private getDateValue(
    dateValue: string
  ): number {

    const timestamp =
      new Date(dateValue).getTime();

    if (Number.isNaN(timestamp)) {
      return 0;
    }

    return timestamp;
  }
}