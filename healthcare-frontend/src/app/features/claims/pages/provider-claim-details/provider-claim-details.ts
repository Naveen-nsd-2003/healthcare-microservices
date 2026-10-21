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
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

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
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  CancelClaimRequest
} from '../../models/cancel-claim-request';

import {
  ClaimStatus
} from '../../models/claim-status';

import {
  ClaimStateService
} from '../../services/claim-state.service';

@Component({
  selector: 'app-provider-claim-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './provider-claim-details.html',
  styleUrl: './provider-claim-details.scss'
})
export class ProviderClaimDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly claimState =
    inject(ClaimStateService);

  private claimId: string | null =
    null;

  readonly cancellationPanelOpen =
    signal(false);

  readonly canCancelClaim =
    computed(() => {
      return this.claimState
        .selectedClaim()
        ?.status === 'SUBMITTED';
    });

  readonly cancellationForm =
    this.formBuilder.nonNullable.group({
      cancellationReason: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ]
    });

  ngOnInit(): void {
    this.claimId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('claimId');

    if (!this.claimId) {
      this.notificationService.showError(
        'Claim identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.loadClaim();
  }

  ngOnDestroy(): void {
    this.claimState.clearSelectedClaim();

    this.claimState.clearError();
  }

  loadClaim(): void {
    if (!this.claimId) {
      return;
    }

    this.cancellationPanelOpen.set(
      false
    );

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.claimState.loadProviderClaimById(
      this.claimId
    );
  }

  goBack(): void {
    this.router.navigate([
      '/provider/claims'
    ]);
  }

  viewAppointment(): void {
    const claim =
      this.claimState.selectedClaim();

    if (!claim?.appointmentId) {
      return;
    }

    this.router.navigate([
      '/provider/appointments',
      claim.appointmentId
    ]);
  }

  viewMedicalRecord(): void {
    const claim =
      this.claimState.selectedClaim();

    if (!claim?.medicalRecordId) {
      return;
    }

    this.router.navigate([
      '/provider/medical-records',
      claim.medicalRecordId
    ]);
  }

  openCancellationPanel(): void {
    if (
      !this.canCancelClaim()
      || this.claimState
        .cancelling()
    ) {
      return;
    }

    this.claimState.clearError();

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.cancellationPanelOpen.set(
      true
    );
  }

  closeCancellationPanel(): void {
    if (
      this.claimState.cancelling()
    ) {
      return;
    }

    this.cancellationForm.reset({
      cancellationReason: ''
    });

    this.cancellationPanelOpen.set(
      false
    );

    this.claimState.clearError();
  }

  cancelClaim(): void {
    this.claimState.clearError();

    if (
      this.claimState.cancelling()
    ) {
      return;
    }

    const claim =
      this.claimState.selectedClaim();

    if (!claim) {
      this.notificationService.showError(
        'Claim information is unavailable.'
      );

      return;
    }

    if (
      claim.status !== 'SUBMITTED'
    ) {
      this.notificationService.showError(
        'Only a submitted claim can be cancelled.'
      );

      this.cancellationPanelOpen.set(
        false
      );

      return;
    }

    if (
      this.cancellationForm.invalid
    ) {
      this.cancellationForm
        .markAllAsTouched();

      this.notificationService.showError(
        'Enter a valid cancellation reason.'
      );

      return;
    }

    const cancellationReason =
      this.cancellationForm.controls
        .cancellationReason
        .value
        .trim();

    if (
      cancellationReason.length === 0
    ) {
      this.cancellationForm.controls
        .cancellationReason
        .setErrors({
          required: true
        });

      this.cancellationForm.controls
        .cancellationReason
        .markAsTouched();

      this.notificationService.showError(
        'Cancellation reason is required.'
      );

      return;
    }

    const request:
      CancelClaimRequest = {
        cancellationReason
      };

    this.claimState.cancelProviderClaim(
      claim.id,
      request,
      () => {
        this.cancellationPanelOpen.set(
          false
        );

        this.cancellationForm.reset({
          cancellationReason: ''
        });

        this.notificationService.showSuccess(
          'Claim cancelled successfully.'
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
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

    return 'send';
  }

  getStatusTitle(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Claim review is in progress';
    }

    if (status === 'APPROVED') {
      return 'Claim approved';
    }

    if (status === 'REJECTED') {
      return 'Claim rejected';
    }

    if (status === 'CANCELLED') {
      return 'Claim cancelled';
    }

    return 'Claim submitted';
  }

  getStatusDescription(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'The Payer has started reviewing the claim. '
        + 'The Provider can no longer cancel it.';
    }

    if (status === 'APPROVED') {
      return 'The Payer completed the review and approved '
        + 'the claim amount shown below.';
    }

    if (status === 'REJECTED') {
      return 'The Payer completed the review and rejected '
        + 'the claim.';
    }

    if (status === 'CANCELLED') {
      return 'The Provider cancelled this claim before '
        + 'the Payer started its review.';
    }

    return 'The claim was submitted successfully and is '
      + 'waiting for the Payer to begin review.';
  }

  getTimelineClass(
    status: ClaimStatus,
    stage: ClaimStatus
  ): string {
    if (
      stage === 'SUBMITTED'
    ) {
      return 'completed-stage';
    }

    if (
      stage === 'UNDER_REVIEW'
    ) {
      if (
        status === 'UNDER_REVIEW'
        || status === 'APPROVED'
        || status === 'REJECTED'
      ) {
        return 'completed-stage';
      }

      if (
        status === 'SUBMITTED'
      ) {
        return 'current-stage';
      }

      return 'inactive-stage';
    }

    if (
      stage === 'APPROVED'
    ) {
      if (
        status === 'APPROVED'
      ) {
        return 'approved-stage';
      }

      if (
        status === 'REJECTED'
      ) {
        return 'rejected-stage';
      }

      if (
        status === 'CANCELLED'
      ) {
        return 'cancelled-stage';
      }

      return 'inactive-stage';
    }

    return 'inactive-stage';
  }
}