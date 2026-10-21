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
  InsurancePolicy
} from '../../models/insurance-policy';

import {
  PolicyStatus
} from '../../models/policy-status';

import {
  PolicyStateService
} from '../../services/policy-state.service';

type PolicyStatusFilter =
  | 'ALL'
  | PolicyStatus;

type PolicySortOption =
  | 'NEWEST'
  | 'COVERAGE_HIGH_TO_LOW'
  | 'PREMIUM_LOW_TO_HIGH'
  | 'EXPIRING_FIRST';

@Component({
  selector: 'app-my-policies',
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
  templateUrl: './my-policies.html',
  styleUrl: './my-policies.scss'
})
export class MyPolicies implements OnInit {

  private readonly router =
    inject(Router);

  readonly policyState =
    inject(PolicyStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<PolicyStatusFilter>('ALL');

  readonly selectedSort =
    signal<PolicySortOption>('NEWEST');

  readonly filteredPolicies =
    computed(
      () => {

        const search =
          this.searchText()
            .trim()
            .toLowerCase();

        const status =
          this.selectedStatus();

        const sort =
          this.selectedSort();

        const filtered =
          this.policyState
            .policies()
            .filter(
              policy => {

                const matchesStatus =
                  status === 'ALL'
                  || policy.status === status;

                const matchesSearch =
                  search.length === 0
                  || policy.policyNumber
                    .toLowerCase()
                    .includes(search)
                  || policy.planName
                    .toLowerCase()
                    .includes(search)
                  || policy.planCode
                    .toLowerCase()
                    .includes(search)
                  || policy.payerOrganizationName
                    .toLowerCase()
                    .includes(search);

                return matchesStatus
                  && matchesSearch;
              }
            );

        return [...filtered].sort(
          (
            firstPolicy,
            secondPolicy
          ) => {

            switch (sort) {

              case 'COVERAGE_HIGH_TO_LOW':
                return secondPolicy.coverageAmount
                  - firstPolicy.coverageAmount;

              case 'PREMIUM_LOW_TO_HIGH':
                return firstPolicy.monthlyPremium
                  - secondPolicy.monthlyPremium;

              case 'EXPIRING_FIRST':
                return this.getDateValue(
                  firstPolicy.coverageEndDate
                )
                  - this.getDateValue(
                    secondPolicy.coverageEndDate
                  );

              case 'NEWEST':
              default:
                return this.getDateValue(
                  secondPolicy.createdAt
                )
                  - this.getDateValue(
                    firstPolicy.createdAt
                  );
            }
          }
        );
      }
    );

  readonly filtersApplied =
    computed(
      () =>
        this.searchText().trim().length > 0
        || this.selectedStatus() !== 'ALL'
        || this.selectedSort() !== 'NEWEST'
    );

  ngOnInit(): void {

    this.loadPolicies();
  }

  loadPolicies(): void {

    this.policyState.loadMyPolicies();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: PolicyStatusFilter
  ): void {

    this.selectedStatus.set(status);
  }

  updateSort(
    sort: PolicySortOption
  ): void {

    this.selectedSort.set(sort);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedStatus.set('ALL');
    this.selectedSort.set('NEWEST');
  }

  viewPolicy(
    policy: InsurancePolicy
  ): void {

    this.router.navigate([
      '/member/policies',
      policy.id
    ]);
  }

  browsePlans(): void {

    this.router.navigate([
      '/member/insurance-plans'
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

  getCoverageFeatures(
    policy: InsurancePolicy
  ): string[] {

    const features: string[] = [];

    if (policy.hospitalizationCoverage) {
      features.push('Hospitalization');
    }

    if (policy.outpatientCoverage) {
      features.push('Outpatient');
    }

    if (policy.prescriptionCoverage) {
      features.push('Prescription');
    }

    if (policy.maternityCoverage) {
      features.push('Maternity');
    }

    return features;
  }

  getRemainingDays(
    policy: InsurancePolicy
  ): number {

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

    const difference =
      endDate.getTime()
      - today.getTime();

    return Math.max(
      0,
      Math.ceil(
        difference
        / (
          1000
          * 60
          * 60
          * 24
        )
      )
    );
  }

  getPolicyDurationLabel(
    policy: InsurancePolicy
  ): string {

    if (policy.policyDurationMonths === 1) {
      return '1 month';
    }

    return `${policy.policyDurationMonths} months`;
  }

  private parseLocalDate(
    dateValue: string
  ): Date {

    const parts =
      dateValue
        .split('-')
        .map(Number);

    return new Date(
      parts[0],
      parts[1] - 1,
      parts[2]
    );
  }

  private getDateValue(
    value: string
  ): number {

    return new Date(value).getTime();
  }
}