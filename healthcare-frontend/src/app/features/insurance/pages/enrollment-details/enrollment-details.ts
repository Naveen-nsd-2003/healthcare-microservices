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
  filter
} from 'rxjs';

import {
  ConfirmationDialog
} from '../../../../shared/dialogs/confirmation-dialog/confirmation-dialog';

import {
  ConfirmationDialogData
} from '../../../../shared/models/confirmation-dialog-data';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  EnrollmentStatus
} from '../../models/enrollment-status';

import {
  EnrollmentStateService
} from '../../services/enrollment-state.service';

@Component({
  selector: 'app-enrollment-details',
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './enrollment-details.html',
  styleUrl: './enrollment-details.scss'
})
export class EnrollmentDetails
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
      .loadMyEnrollmentById(
        enrollmentId
      );
  }

  ngOnDestroy(): void {

    this.enrollmentState
      .clearSelectedEnrollment();
  }

  goBack(): void {

    this.router.navigate([
      '/member/enrollments'
    ]);
  }

  cancelEnrollment(): void {

    const enrollment =
      this.enrollmentState
        .selectedEnrollment();

    if (
      !enrollment
      || enrollment.status !== 'PENDING'
    ) {

      return;
    }

    const dialogData:
      ConfirmationDialogData = {

        title: 'Cancel enrollment?',

        message:
          `Cancel your enrollment request for `
          + `${enrollment.planName}? `
          + `This action cannot be reversed.`,

        confirmText: 'Cancel enrollment',

        cancelText: 'Keep enrollment',

        icon: 'cancel',

        type: 'danger'
      };

    const dialogReference =
      this.dialog.open(
        ConfirmationDialog,
        {
          data: dialogData,
          width: '440px',
          maxWidth:
            'calc(100vw - 32px)',
          panelClass:
            'healthcare-dialog-panel',
          disableClose: true,
          autoFocus: false,
          restoreFocus: true
        }
      );

    dialogReference
      .afterClosed()
      .pipe(
        filter(
          confirmed =>
            confirmed === true
        )
      )
      .subscribe(
        () => {

          this.enrollmentState
            .cancelEnrollment(
              enrollment.id,
              () => {

                this.notificationMessage
                  .showSuccess(
                    'Enrollment cancelled successfully.'
                  );
              },
              message => {

                this.notificationMessage
                  .showError(message);
              }
            );
        }
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