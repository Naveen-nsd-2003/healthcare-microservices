import {
  Component,
  OnDestroy,
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
  Claim
} from '../../models/claim';

import {
  ClaimStatus
} from '../../models/claim-status';

import {
  ClaimStateService
} from '../../services/claim-state.service';

type ClaimStatusFilter =
  | 'ALL'
  | ClaimStatus;

type ClaimSortOption =
  | 'SUBMITTED_DESCENDING'
  | 'SUBMITTED_ASCENDING'
  | 'SERVICE_DATE_DESCENDING'
  | 'CLAIMED_AMOUNT_DESCENDING'
  | 'PROVIDER_ASCENDING'
  | 'MEMBER_ASCENDING';

@Component({
  selector: 'app-payer-claims',
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
  templateUrl: './payer-claims.html',
  styleUrl: './payer-claims.scss'
})
export class PayerClaims
  implements OnInit, OnDestroy {

  private readonly router =
    inject(Router);

  readonly claimState =
    inject(ClaimStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<ClaimStatusFilter>(
      'ALL'
    );

  readonly selectedSort =
    signal<ClaimSortOption>(
      'SUBMITTED_DESCENDING'
    );

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

  readonly pendingDecisionCount =
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

  readonly filteredClaims =
    computed(() => {
      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const selectedStatus =
        this.selectedStatus();

      const selectedSort =
        this.selectedSort();

      const filteredClaims =
        this.claimState
          .payerClaims()
          .filter(claim => {
            const searchableValues = [
              claim.claimNumber,
              claim.memberEmail,
              claim.providerOrganizationName,
              claim.doctorName,
              claim.doctorSpecialization,
              claim.departmentName,
              claim.policyNumber,
              claim.planName,
              claim.planCode,
              claim.diagnosisSnapshot,
              claim.claimDescription
            ];

            const matchesSearch =
              search.length === 0
              || searchableValues.some(value => {
                return this.normalizeSearchValue(
                  value
                ).includes(search);
              });

            const matchesStatus =
              selectedStatus === 'ALL'
              || claim.status
                === selectedStatus;

            return matchesSearch
              && matchesStatus;
          });

      return [
        ...filteredClaims
      ].sort((
        firstClaim,
        secondClaim
      ) => {
        if (
          selectedSort
          === 'SUBMITTED_ASCENDING'
        ) {
          return this.getTimestampValue(
            firstClaim.submittedAt
          ) - this.getTimestampValue(
            secondClaim.submittedAt
          );
        }

        if (
          selectedSort
          === 'SERVICE_DATE_DESCENDING'
        ) {
          return this.getDateValue(
            secondClaim.serviceDate
          ) - this.getDateValue(
            firstClaim.serviceDate
          );
        }

        if (
          selectedSort
          === 'CLAIMED_AMOUNT_DESCENDING'
        ) {
          return this.toFiniteNumber(
            secondClaim.claimedAmount
          ) - this.toFiniteNumber(
            firstClaim.claimedAmount
          );
        }

        if (
          selectedSort
          === 'PROVIDER_ASCENDING'
        ) {
          return this.normalizeSearchValue(
            firstClaim.providerOrganizationName
          ).localeCompare(
            this.normalizeSearchValue(
              secondClaim.providerOrganizationName
            )
          );
        }

        if (
          selectedSort
          === 'MEMBER_ASCENDING'
        ) {
          return this.normalizeSearchValue(
            firstClaim.memberEmail
          ).localeCompare(
            this.normalizeSearchValue(
              secondClaim.memberEmail
            )
          );
        }

        return this.getTimestampValue(
          secondClaim.submittedAt
        ) - this.getTimestampValue(
          firstClaim.submittedAt
        );
      });
    });

  readonly filtersApplied =
    computed(() => {
      return this.searchText()
        .trim()
        .length > 0
        || this.selectedStatus()
          !== 'ALL'
        || this.selectedSort()
          !== 'SUBMITTED_DESCENDING';
    });

  ngOnInit(): void {
    this.loadClaims();
  }

  ngOnDestroy(): void {
    this.claimState.clearError();
  }

  loadClaims(): void {
    this.claimState.loadPayerClaims();
  }

  updateSearch(
    value: string
  ): void {
    this.searchText.set(
      value
    );
  }

  updateStatus(
    status: ClaimStatusFilter
  ): void {
    this.selectedStatus.set(
      status
    );
  }

  updateSort(
    sort: ClaimSortOption
  ): void {
    this.selectedSort.set(
      sort
    );
  }

  clearFilters(): void {
    this.searchText.set('');

    this.selectedStatus.set(
      'ALL'
    );

    this.selectedSort.set(
      'SUBMITTED_DESCENDING'
    );
  }

  viewClaim(
    claim: Claim
  ): void {
    if (!claim.id) {
      return;
    }

    this.router.navigate([
      '/payer/claims',
      claim.id
    ]);
  }

  viewPolicies(): void {
    this.router.navigate([
      '/payer/policies'
    ]);
  }

  getStatusLabel(
    status: ClaimStatus
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

  getStatusIcon(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'manage_search';
    }

    if (status === 'APPROVED') {
      return 'verified';
    }

    if (status === 'REJECTED') {
      return 'cancel';
    }

    if (status === 'CANCELLED') {
      return 'block';
    }

    return 'inbox';
  }

  getStatusDescription(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Review is in progress and a decision is pending.';
    }

    if (status === 'APPROVED') {
      return 'The claim review is complete and approved.';
    }

    if (status === 'REJECTED') {
      return 'The claim review is complete and rejected.';
    }

    if (status === 'CANCELLED') {
      return 'The Provider cancelled the claim before review.';
    }

    return 'This claim is waiting for the Payer to start review.';
  }

  getDiagnosisPreview(
    diagnosis: string
  ): string {
    const normalizedDiagnosis =
      diagnosis?.trim() ?? '';

    if (normalizedDiagnosis.length === 0) {
      return 'No diagnosis snapshot is available.';
    }

    if (
      normalizedDiagnosis.length <= 140
    ) {
      return normalizedDiagnosis;
    }

    return normalizedDiagnosis
      .substring(
        0,
        137
      )
      .trimEnd()
      + '...';
  }

  getApprovedAmountLabel(
    claim: Claim
  ): string {
    if (
      claim.approvedAmount === null
      || claim.approvedAmount === undefined
      || !Number.isFinite(
        claim.approvedAmount
      )
    ) {
      return 'Not resolved';
    }

    return new Intl.NumberFormat(
      'en-IN',
      {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }
    ).format(
      claim.approvedAmount
    );
  }

  private countClaimsByStatus(
    status: ClaimStatus
  ): number {
    return this.claimState
      .payerClaims()
      .filter(claim => {
        return claim.status === status;
      })
      .length;
  }

  private normalizeSearchValue(
    value: string | null | undefined
  ): string {
    if (!value) {
      return '';
    }

    return value
      .trim()
      .toLowerCase();
  }

  private getTimestampValue(
    timestamp: string | null | undefined
  ): number {
    if (!timestamp) {
      return 0;
    }

    const value =
      new Date(
        timestamp
      ).getTime();

    return Number.isNaN(value)
      ? 0
      : value;
  }

  private getDateValue(
    dateValue: string | null | undefined
  ): number {
    if (!dateValue) {
      return 0;
    }

    const value =
      new Date(
        `${dateValue}T00:00:00`
      ).getTime();

    return Number.isNaN(value)
      ? 0
      : value;
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