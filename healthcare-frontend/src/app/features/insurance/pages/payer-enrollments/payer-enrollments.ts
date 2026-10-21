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
  Enrollment
} from '../../models/enrollment';

import {
  EnrollmentStatus
} from '../../models/enrollment-status';

import {
  EnrollmentStateService
} from '../../services/enrollment-state.service';

type EnrollmentFilter =
  | 'ALL'
  | EnrollmentStatus;

@Component({
  selector: 'app-payer-enrollments',
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
  templateUrl: './payer-enrollments.html',
  styleUrl: './payer-enrollments.scss'
})
export class PayerEnrollments
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly enrollmentState =
    inject(EnrollmentStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<EnrollmentFilter>('ALL');

  readonly filteredEnrollments =
    computed(
      () => {

        const search =
          this.searchText()
            .trim()
            .toLowerCase();

        const status =
          this.selectedStatus();

        return this.enrollmentState
          .enrollments()
          .filter(
            enrollment => {

              const matchesStatus =
                status === 'ALL'
                || enrollment.status
                  === status;

              const matchesSearch =
                search.length === 0
                || enrollment.memberEmail
                  .toLowerCase()
                  .includes(search)
                || enrollment.planName
                  .toLowerCase()
                  .includes(search)
                || enrollment.planCode
                  .toLowerCase()
                  .includes(search)
                || enrollment.nomineeName
                  .toLowerCase()
                  .includes(search);

              return matchesStatus
                && matchesSearch;
            }
          );
      }
    );

  ngOnInit(): void {

    this.loadEnrollments();
  }

  loadEnrollments(): void {

    this.enrollmentState
      .loadPayerEnrollments();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: EnrollmentFilter
  ): void {

    this.selectedStatus.set(status);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedStatus.set('ALL');
  }

  viewEnrollment(
    enrollment: Enrollment
  ): void {

    this.router.navigate([
      '/payer/enrollments',
      enrollment.id
    ]);
  }

  getStatusClass(
    status: EnrollmentStatus
  ): string {

    switch (status) {

      case 'APPROVED':
        return 'status-badge--approved';

      case 'REJECTED':
        return 'status-badge--rejected';

      case 'CANCELLED':
        return 'status-badge--cancelled';

      case 'PENDING':
      default:
        return 'status-badge--pending';
    }
  }

  getStatusIcon(
    status: EnrollmentStatus
  ): string {

    switch (status) {

      case 'APPROVED':
        return 'check_circle';

      case 'REJECTED':
        return 'cancel';

      case 'CANCELLED':
        return 'block';

      case 'PENDING':
      default:
        return 'schedule';
    }
  }

  getRelationshipLabel(
  relationship: string
): string {

  return relationship
    .toLowerCase()
    .split('_')
    .map(
      word =>
        word.charAt(0).toUpperCase()
        + word.slice(1)
    )
    .join(' ');
}
}