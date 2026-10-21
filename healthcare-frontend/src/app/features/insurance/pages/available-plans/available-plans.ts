import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  CurrencyPipe
} from '@angular/common';

import {
  FormsModule
} from '@angular/forms';

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
  MatChipsModule
} from '@angular/material/chips';

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
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

type PlanSortOption =
  | 'PREMIUM_LOW_TO_HIGH'
  | 'PREMIUM_HIGH_TO_LOW'
  | 'COVERAGE_HIGH_TO_LOW'
  | 'NAME_ASCENDING';

@Component({
  selector: 'app-available-plans',
  imports: [
    CurrencyPipe,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './available-plans.html',
  styleUrl: './available-plans.scss'
})
export class AvailablePlans implements OnInit {

  private readonly router =
    inject(Router);

  readonly planState =
    inject(InsurancePlanStateService);

  readonly searchText =
    signal('');

  readonly selectedPayer =
    signal('ALL');

  readonly sortOption =
    signal<PlanSortOption>(
      'PREMIUM_LOW_TO_HIGH'
    );

  readonly payerOrganizations =
    computed(
      () => {

        const organizations =
          this.planState
            .plans()
            .map(
              plan =>
                plan.payerOrganizationName
            )
            .filter(
              organization =>
                organization.trim().length > 0
            );

        return Array.from(
          new Set(organizations)
        ).sort(
          (
            firstOrganization,
            secondOrganization
          ) =>
            firstOrganization.localeCompare(
              secondOrganization
            )
        );
      }
    );

  readonly filteredPlans =
    computed(
      () => {

        const search =
          this.searchText()
            .trim()
            .toLowerCase();

        const payer =
          this.selectedPayer();

        const sort =
          this.sortOption();

        const filtered =
          this.planState
            .plans()
            .filter(
              plan => {

                const matchesSearch =
                  search.length === 0
                  || plan.planName
                    .toLowerCase()
                    .includes(search)
                  || plan.planCode
                    .toLowerCase()
                    .includes(search)
                  || plan.payerOrganizationName
                    .toLowerCase()
                    .includes(search);

                const matchesPayer =
                  payer === 'ALL'
                  || plan.payerOrganizationName
                    === payer;

                return matchesSearch
                  && matchesPayer;
              }
            );

        return [...filtered].sort(
          (
            firstPlan,
            secondPlan
          ) => {

            switch (sort) {

              case 'PREMIUM_HIGH_TO_LOW':
                return secondPlan.monthlyPremium
                  - firstPlan.monthlyPremium;

              case 'COVERAGE_HIGH_TO_LOW':
                return secondPlan.coverageAmount
                  - firstPlan.coverageAmount;

              case 'NAME_ASCENDING':
                return firstPlan.planName
                  .localeCompare(
                    secondPlan.planName
                  );

              case 'PREMIUM_LOW_TO_HIGH':
              default:
                return firstPlan.monthlyPremium
                  - secondPlan.monthlyPremium;
            }
          }
        );
      }
    );

  readonly filtersApplied =
    computed(
      () =>
        this.searchText().trim().length > 0
        || this.selectedPayer() !== 'ALL'
        || this.sortOption()
          !== 'PREMIUM_LOW_TO_HIGH'
    );

  ngOnInit(): void {

    this.loadPlans();
  }

  loadPlans(): void {

    this.planState.loadAvailablePlans();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updatePayer(
    value: string
  ): void {

    this.selectedPayer.set(value);
  }

  updateSort(
    value: PlanSortOption
  ): void {

    this.sortOption.set(value);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedPayer.set('ALL');

    this.sortOption.set(
      'PREMIUM_LOW_TO_HIGH'
    );
  }

  viewPlan(
    plan: InsurancePlan
  ): void {

    this.router.navigate([
      '/member/insurance-plans',
      plan.id
    ]);
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
}