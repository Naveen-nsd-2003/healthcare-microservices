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
  MatSelectModule
} from '@angular/material/select';

import {
  MatFormFieldModule
} from '@angular/material/form-field';

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
  selector: 'app-my-enrollments',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './my-enrollments.html',
  styleUrl: './my-enrollments.scss'
})
export class MyEnrollments implements OnInit {

  private readonly router =
    inject(Router);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  readonly enrollmentState =
    inject(EnrollmentStateService);

  readonly selectedStatus =
    signal<EnrollmentFilter>('ALL');

  readonly filteredEnrollments =
    computed(
      () => {

        const status =
          this.selectedStatus();

        if (status === 'ALL') {

          return this.enrollmentState
            .enrollments();
        }

        return this.enrollmentState
          .enrollments()
          .filter(
            enrollment =>
              enrollment.status === status
          );
      }
    );

  ngOnInit(): void {

    this.loadEnrollments();

    const createdEnrollmentId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('created');

    if (createdEnrollmentId) {

      this.router.navigate(
        [],
        {
          relativeTo:
            this.activatedRoute,

          queryParams: {
            created: null
          },

          queryParamsHandling:
            'merge',

          replaceUrl: true
        }
      );
    }
  }

  loadEnrollments(): void {

    this.enrollmentState
      .loadMyEnrollments();
  }

  updateStatusFilter(
    status: EnrollmentFilter
  ): void {

    this.selectedStatus.set(status);
  }

  viewEnrollment(
    enrollment: Enrollment
  ): void {

    this.router.navigate([
      '/member/enrollments',
      enrollment.id
    ]);
  }

  browsePlans(): void {

    this.router.navigate([
      '/member/insurance-plans'
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