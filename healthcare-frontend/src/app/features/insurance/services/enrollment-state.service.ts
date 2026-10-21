import {
  Injectable,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  finalize
} from 'rxjs';

import {
  CreateEnrollmentRequest
} from '../models/create-enrollment-request';

import {
  Enrollment
} from '../models/enrollment';

import {
  EnrollmentApiService
} from './enrollment-api.service';

import {
  ReviewEnrollmentRequest
} from '../models/review-enrollment-request';


@Injectable({
  providedIn: 'root'
})
export class EnrollmentStateService {

  private readonly enrollmentApi =
    inject(EnrollmentApiService);

  private readonly enrollmentsState =
    signal<Enrollment[]>([]);

  private readonly selectedEnrollmentState =
    signal<Enrollment | null>(null);

  private readonly loadingState =
    signal(false);

  private readonly detailsLoadingState =
    signal(false);

  private readonly submittingState =
    signal(false);

  private readonly cancellingState =
    signal(false);

  private readonly errorMessageState =
    signal<string | null>(null);

  private readonly reviewingState =
    signal(false);

  readonly enrollments =
    this.enrollmentsState.asReadonly();

  readonly selectedEnrollment =
    this.selectedEnrollmentState.asReadonly();

  readonly loading =
    this.loadingState.asReadonly();

  readonly detailsLoading =
    this.detailsLoadingState.asReadonly();

  readonly submitting =
    this.submittingState.asReadonly();

  readonly cancelling =
    this.cancellingState.asReadonly();

  readonly errorMessage =
    this.errorMessageState.asReadonly();

  readonly pendingCount =
    computed(
      () =>
        this.enrollmentsState()
          .filter(
            enrollment =>
              enrollment.status === 'PENDING'
          )
          .length
    );

  readonly approvedCount =
    computed(
      () =>
        this.enrollmentsState()
          .filter(
            enrollment =>
              enrollment.status === 'APPROVED'
          )
          .length
    );

  readonly rejectedCount =
    computed(
      () =>
        this.enrollmentsState()
          .filter(
            enrollment =>
              enrollment.status === 'REJECTED'
          )
          .length
    );

  readonly cancelledCount =
    computed(
      () =>
        this.enrollmentsState()
          .filter(
            enrollment =>
              enrollment.status === 'CANCELLED'
          )
          .length
    );

    readonly reviewing =
  this.reviewingState.asReadonly();

  createEnrollment(
    request: CreateEnrollmentRequest,
    afterSuccess?: (
      enrollment: Enrollment
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.submittingState()) {
      return;
    }

    this.submittingState.set(true);
    this.errorMessageState.set(null);

    this.enrollmentApi
      .createEnrollment(request)
      .pipe(
        finalize(
          () =>
            this.submittingState.set(false)
        )
      )
      .subscribe({
        next: enrollment => {

          this.enrollmentsState.update(
            enrollments => [
              enrollment,
              ...enrollments
            ]
          );

          this.selectedEnrollmentState.set(
            enrollment
          );

          this.errorMessageState.set(null);

          afterSuccess?.(enrollment);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(error);

          this.errorMessageState.set(message);

          afterError?.(message);
        }
      });
  }

  loadMyEnrollments(): void {

    this.loadingState.set(true);
    this.errorMessageState.set(null);

    this.enrollmentApi
      .getMyEnrollments()
      .pipe(
        finalize(
          () =>
            this.loadingState.set(false)
        )
      )
      .subscribe({
        next: enrollments => {

          const sortedEnrollments =
            [...enrollments].sort(
              (
                firstEnrollment,
                secondEnrollment
              ) =>
                new Date(
                  secondEnrollment.createdAt
                ).getTime()
                - new Date(
                  firstEnrollment.createdAt
                ).getTime()
            );

          this.enrollmentsState.set(
            sortedEnrollments
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  loadMyEnrollmentById(
    enrollmentId: string
  ): void {

    this.detailsLoadingState.set(true);
    this.errorMessageState.set(null);
    this.selectedEnrollmentState.set(null);

    this.enrollmentApi
      .getMyEnrollmentById(
        enrollmentId
      )
      .pipe(
        finalize(
          () =>
            this.detailsLoadingState.set(
              false
            )
        )
      )
      .subscribe({
        next: enrollment => {

          this.selectedEnrollmentState.set(
            enrollment
          );

          this.errorMessageState.set(null);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          this.errorMessageState.set(
            this.resolveErrorMessage(error)
          );
        }
      });
  }

  cancelEnrollment(
    enrollmentId: string,
    afterSuccess?: (
      enrollment: Enrollment
    ) => void,
    afterError?: (
      message: string
    ) => void
  ): void {

    if (this.cancellingState()) {
      return;
    }

    this.cancellingState.set(true);
    this.errorMessageState.set(null);

    this.enrollmentApi
      .cancelMyEnrollment(enrollmentId)
      .pipe(
        finalize(
          () =>
            this.cancellingState.set(false)
        )
      )
      .subscribe({
        next: enrollment => {

          this.selectedEnrollmentState.set(
            enrollment
          );

          this.enrollmentsState.update(
            enrollments =>
              enrollments.map(
                currentEnrollment =>
                  currentEnrollment.id
                  === enrollment.id
                    ? enrollment
                    : currentEnrollment
              )
          );

          this.errorMessageState.set(null);

          afterSuccess?.(enrollment);
        },

        error: (
          error: HttpErrorResponse
        ) => {

          const message =
            this.resolveErrorMessage(error);

          this.errorMessageState.set(message);

          afterError?.(message);
        }
      });
  }

loadPayerEnrollments(): void {

  this.loadingState.set(true);
  this.errorMessageState.set(null);

  this.enrollmentApi
    .getPayerEnrollments()
    .pipe(
      finalize(
        () =>
          this.loadingState.set(false)
      )
    )
    .subscribe({
      next: enrollments => {

        const sortedEnrollments =
          this.sortNewestFirst(
            enrollments
          );

        this.enrollmentsState.set(
          sortedEnrollments
        );

        this.errorMessageState.set(null);
      },

      error: (
        error: HttpErrorResponse
      ) => {

        this.errorMessageState.set(
          this.resolveErrorMessage(error)
        );
      }
    });
}

loadPayerEnrollmentById(
  enrollmentId: string
): void {

  this.detailsLoadingState.set(true);
  this.errorMessageState.set(null);
  this.selectedEnrollmentState.set(null);

  this.enrollmentApi
    .getPayerEnrollmentById(
      enrollmentId
    )
    .pipe(
      finalize(
        () =>
          this.detailsLoadingState.set(
            false
          )
      )
    )
    .subscribe({
      next: enrollment => {

        this.selectedEnrollmentState.set(
          enrollment
        );

        this.errorMessageState.set(null);
      },

      error: (
        error: HttpErrorResponse
      ) => {

        this.errorMessageState.set(
          this.resolveErrorMessage(error)
        );
      }
    });
}

approveEnrollment(
  enrollmentId: string,
  request: ReviewEnrollmentRequest,
  afterSuccess?: (
    enrollment: Enrollment
  ) => void,
  afterError?: (
    message: string
  ) => void
): void {

  this.reviewEnrollment(
    enrollmentId,
    request,
    'APPROVE',
    afterSuccess,
    afterError
  );
}

rejectEnrollment(
  enrollmentId: string,
  request: ReviewEnrollmentRequest,
  afterSuccess?: (
    enrollment: Enrollment
  ) => void,
  afterError?: (
    message: string
  ) => void
): void {

  this.reviewEnrollment(
    enrollmentId,
    request,
    'REJECT',
    afterSuccess,
    afterError
  );
}


  clearSelectedEnrollment(): void {

    this.selectedEnrollmentState.set(null);
    this.errorMessageState.set(null);
  }

  clearError(): void {

    this.errorMessageState.set(null);
  }

  clearState(): void {

    this.enrollmentsState.set([]);
    this.selectedEnrollmentState.set(null);
    this.loadingState.set(false);
    this.detailsLoadingState.set(false);
    this.submittingState.set(false);
    this.reviewingState.set(false);
    this.cancellingState.set(false);
    this.errorMessageState.set(null);
  }

  private reviewEnrollment(
  enrollmentId: string,
  request: ReviewEnrollmentRequest,
  action: 'APPROVE' | 'REJECT',
  afterSuccess?: (
    enrollment: Enrollment
  ) => void,
  afterError?: (
    message: string
  ) => void
): void {

  if (this.reviewingState()) {
    return;
  }

  this.reviewingState.set(true);
  this.errorMessageState.set(null);

  const reviewRequest =
    action === 'APPROVE'
      ? this.enrollmentApi
        .approveEnrollment(
          enrollmentId,
          request
        )
      : this.enrollmentApi
        .rejectEnrollment(
          enrollmentId,
          request
        );

  reviewRequest
    .pipe(
      finalize(
        () =>
          this.reviewingState.set(false)
      )
    )
    .subscribe({
      next: enrollment => {

        this.selectedEnrollmentState.set(
          enrollment
        );

        this.updateEnrollmentLocally(
          enrollment
        );

        this.errorMessageState.set(null);

        afterSuccess?.(enrollment);
      },

      error: (
        error: HttpErrorResponse
      ) => {

        const message =
          this.resolveErrorMessage(error);

        this.errorMessageState.set(message);

        afterError?.(message);
      }
    });
}

private updateEnrollmentLocally(
  updatedEnrollment: Enrollment
): void {

  this.enrollmentsState.update(
    enrollments =>
      enrollments.map(
        enrollment =>
          enrollment.id
          === updatedEnrollment.id
            ? updatedEnrollment
            : enrollment
      )
  );
}

private sortNewestFirst(
  enrollments: Enrollment[]
): Enrollment[] {

  return [...enrollments].sort(
    (
      firstEnrollment,
      secondEnrollment
    ) =>
      new Date(
        secondEnrollment.createdAt
      ).getTime()
      - new Date(
        firstEnrollment.createdAt
      ).getTime()
  );
}


  private resolveErrorMessage(
    error: HttpErrorResponse
  ): string {

    if (error.status === 0) {

      return 'Unable to connect to the Payers Service. '
        + 'Please verify that the service is running.';
    }

    const backendError =
      error.error;

    if (
      typeof backendError === 'string'
      && backendError.trim().length > 0
    ) {

      return backendError;
    }

    if (
      backendError !== null
      && typeof backendError === 'object'
      && 'message' in backendError
      && typeof backendError.message === 'string'
      && backendError.message.trim().length > 0
    ) {

      return backendError.message;
    }

    if (error.status === 400) {

      return 'Please review the enrollment information '
        + 'and correct the invalid fields.';
    }

    if (error.status === 403) {

      return 'You are not authorized to manage '
        + 'this enrollment.';
    }

    if (error.status === 404) {

      return 'The requested enrollment or insurance '
        + 'plan could not be found.';
    }

    if (error.status === 409) {

      return 'This enrollment cannot be completed '
        + 'because of its current state or an existing '
        + 'enrollment for the selected plan.';
    }

    if (error.status >= 500) {

      return 'The enrollment service is temporarily '
        + 'unavailable. Please try again.';
    }

    return 'Unable to process the enrollment. '
      + 'Please try again.';
  }
}