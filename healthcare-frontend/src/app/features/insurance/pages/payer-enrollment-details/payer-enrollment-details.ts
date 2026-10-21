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
  EnrollmentReviewDialog
} from '../../dialogs/enrollment-review-dialog/enrollment-review-dialog';

import {
  EnrollmentReviewAction,
  EnrollmentReviewDialogData,
  EnrollmentReviewDialogResult
} from '../../models/enrollment-review-dialog-data';

import {
  EnrollmentStatus
} from '../../models/enrollment-status';

import {
  NomineeRelationship
} from '../../models/nominee-relationship';

import {
  ReviewEnrollmentRequest
} from '../../models/review-enrollment-request';

import {
  EnrollmentStateService
} from '../../services/enrollment-state.service';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

@Component({
  selector: 'app-payer-enrollment-details',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payer-enrollment-details.html',
  styleUrl: './payer-enrollment-details.scss'
})
export class PayerEnrollmentDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly dialog =
    inject(MatDialog);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly enrollmentState =
    inject(EnrollmentStateService);

  ngOnInit(): void {

    const enrollmentId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('enrollmentId');

    if (!enrollmentId) {

      this.notificationMessage.showError(
        'Enrollment identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.enrollmentState
      .loadPayerEnrollmentById(
        enrollmentId
      );
  }

  ngOnDestroy(): void {

    this.enrollmentState
      .clearSelectedEnrollment();
  }

  goBack(): void {

    this.router.navigate([
      '/payer/enrollments'
    ]);
  }

  openApprovalDialog(): void {

    this.openReviewDialog(
      'APPROVE'
    );
  }

  openRejectionDialog(): void {

    this.openReviewDialog(
      'REJECT'
    );
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
    relationship: NomineeRelationship
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

  private openReviewDialog(
    action: EnrollmentReviewAction
  ): void {

    const enrollment =
      this.enrollmentState
        .selectedEnrollment();

    if (
      !enrollment
      || enrollment.status !== 'PENDING'
      || this.enrollmentState.reviewing()
    ) {

      return;
    }

    const dialogData:
      EnrollmentReviewDialogData = {

      action,

      enrollmentId:
        enrollment.id,

      memberEmail:
        enrollment.memberEmail,

      planName:
        enrollment.planName
    };

    const dialogReference =
      this.dialog.open<
        EnrollmentReviewDialog,
        EnrollmentReviewDialogData,
        EnrollmentReviewDialogResult
      >(
        EnrollmentReviewDialog,
        {
          data: dialogData,
          width: '520px',
          maxWidth:
            'calc(100vw - 32px)',
          panelClass:
            'enrollment-review-dialog-panel',
          disableClose: true,
          autoFocus:
            'textarea',
          restoreFocus: true
        }
      );

    dialogReference
      .afterClosed()
      .subscribe(
        result => {

          if (!result) {
            return;
          }

          const request:
            ReviewEnrollmentRequest = {

            reviewRemarks:
              result.reviewRemarks
          };

          if (
            result.action === 'APPROVE'
          ) {

            this.approveEnrollment(
              enrollment.id,
              request
            );

            return;
          }

          this.rejectEnrollment(
            enrollment.id,
            request
          );
        }
      );
  }

  private approveEnrollment(
    enrollmentId: string,
    request: ReviewEnrollmentRequest
  ): void {

    this.enrollmentState
      .approveEnrollment(
        enrollmentId,
        request,
        () => {

          this.notificationMessage
            .showSuccess(
              'Enrollment approved successfully. '
              + 'The Member policy is now available.'
            );
        },
        message => {

          this.notificationMessage
            .showError(message);
        }
      );
  }

  private rejectEnrollment(
    enrollmentId: string,
    request: ReviewEnrollmentRequest
  ): void {

    this.enrollmentState
      .rejectEnrollment(
        enrollmentId,
        request,
        () => {

          this.notificationMessage
            .showSuccess(
              'Enrollment rejected successfully.'
            );
        },
        message => {

          this.notificationMessage
            .showError(message);
        }
      );
  }
}