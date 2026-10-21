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
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  ClaimStatus
} from '../../models/claim-status';

import {
  ClaimStateService
} from '../../services/claim-state.service';

@Component({
  selector: 'app-member-claim-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './member-claim-details.html',
  styleUrl: './member-claim-details.scss'
})
export class MemberClaimDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly claimState =
    inject(ClaimStateService);

  private claimId: string | null =
    null;

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

    this.claimState.loadMyClaimById(
      this.claimId
    );
  }

  goBack(): void {
    this.router.navigate([
      '/member/claims'
    ]);
  }

  viewMedicalRecord(): void {
    const claim =
      this.claimState.selectedClaim();

    if (!claim?.medicalRecordId) {
      return;
    }

    this.router.navigate([
      '/member/medical-records',
      claim.medicalRecordId
    ]);
  }

  viewAppointment(): void {
    const claim =
      this.claimState.selectedClaim();

    if (!claim?.appointmentId) {
      return;
    }

    this.router.navigate([
      '/member/appointments',
      claim.appointmentId
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

    return 'send';
  }

  getStatusTitle(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Your claim is under review';
    }

    if (status === 'APPROVED') {
      return 'Your claim was approved';
    }

    if (status === 'REJECTED') {
      return 'Your claim was rejected';
    }

    if (status === 'CANCELLED') {
      return 'The claim was cancelled';
    }

    return 'Your claim was submitted';
  }

  getStatusDescription(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Your insurance provider has started reviewing '
        + 'the claim and a decision is pending.';
    }

    if (status === 'APPROVED') {
      return 'Your insurance provider approved the claim. '
        + 'The approved amount is shown below.';
    }

    if (status === 'REJECTED') {
      return 'Your insurance provider reviewed and rejected '
        + 'the claim. Review the reason provided below.';
    }

    if (status === 'CANCELLED') {
      return 'The healthcare Provider cancelled this claim '
        + 'before the review workflow was completed.';
    }

    return 'Your healthcare Provider submitted this claim '
      + 'and it is waiting for insurance review.';
  }

  getResolutionLabel(
    status: ClaimStatus
  ): string {
    if (status === 'APPROVED') {
      return 'Approved';
    }

    if (status === 'REJECTED') {
      return 'Rejected';
    }

    if (status === 'CANCELLED') {
      return 'Cancelled';
    }

    return 'Pending';
  }
}