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
  selector: 'app-payer-policies',
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
  templateUrl: './payer-policies.html',
  styleUrl: './payer-policies.scss'
})
export class PayerPolicies implements OnInit {

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

        const selectedStatus =
          this.selectedStatus();

        const selectedSort =
          this.selectedSort();

        const filtered =
          this.policyState
            .policies()
            .filter(
              policy => {

                const matchesStatus =
                  selectedStatus === 'ALL'
                  || policy.status === selectedStatus;

                const matchesSearch =
                  search.length === 0
                  || policy.policyNumber
                    .toLowerCase()
                    .includes(search)
                  || policy.memberEmail
                    .toLowerCase()
                    .includes(search)
                  || policy.planName
                    .toLowerCase()
                    .includes(search)
                  || policy.planCode
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

            switch (selectedSort) {

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

    this.policyState
      .loadPayerPolicies();
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
      '/payer/policies',
      policy.id
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

  private getDateValue(
    value: string
  ): number {

    return new Date(value).getTime();
  }
}