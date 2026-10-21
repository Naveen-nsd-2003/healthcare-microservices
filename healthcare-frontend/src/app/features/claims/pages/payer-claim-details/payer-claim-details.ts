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
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
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
  ApproveClaimRequest
} from '../../models/approve-claim-request';

import {
  ClaimStatus
} from '../../models/claim-status';

import {
  RejectClaimRequest
} from '../../models/reject-claim-request';

import {
  ClaimStateService
} from '../../services/claim-state.service';

type DecisionPanel =
  | 'APPROVE'
  | 'REJECT'
  | null;

@Component({
  selector: 'app-payer-claim-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payer-claim-details.html',
  styleUrl: './payer-claim-details.scss'
})
export class PayerClaimDetails
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

  readonly activeDecisionPanel =
    signal<DecisionPanel>(null);

  readonly canStartReview =
    computed(() => {
      return this.claimState
        .selectedClaim()
        ?.status === 'SUBMITTED';
    });

  readonly canResolveClaim =
    computed(() => {
      return this.claimState
        .selectedClaim()
        ?.status === 'UNDER_REVIEW';
    });

  readonly maximumApprovalAmount =
    computed(() => {
      const claim =
        this.claimState.selectedClaim();

      if (!claim) {
        return null;
      }

      return Math.min(
        claim.claimedAmount,
        claim.coverageAmount
      );
    });

  readonly actionInProgress =
    computed(() => {
      return this.claimState.reviewing()
        || this.claimState.approving()
        || this.claimState.rejecting();
    });

  readonly approveForm =
    this.formBuilder.nonNullable.group({
      approvedAmount: [
        '',
        [
          Validators.required,
          this.positiveMoneyValidator(),
          this.moneyPrecisionValidator()
        ]
      ],
      reviewRemarks: [
        '',
        [
          Validators.required,
          Validators.maxLength(1500)
        ]
      ]
    });

  readonly rejectForm =
    this.formBuilder.nonNullable.group({
      rejectionReason: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ],
      reviewRemarks: [
        '',
        [
          Validators.required,
          Validators.maxLength(1500)
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

    this.closeDecisionPanel();

    this.claimState.loadPayerClaimById(
      this.claimId
    );
  }

  goBack(): void {
    this.router.navigate([
      '/payer/claims'
    ]);
  }

  startReview(): void {
    this.claimState.clearError();

    if (
      !this.claimId
      || !this.canStartReview()
      || this.actionInProgress()
    ) {
      return;
    }

    this.claimState.startPayerClaimReview(
      this.claimId,
      () => {
        this.notificationService.showSuccess(
          'Claim review started successfully.'
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  openApprovePanel(): void {
    if (
      !this.canResolveClaim()
      || this.actionInProgress()
    ) {
      return;
    }

    this.claimState.clearError();

    const claim =
      this.claimState.selectedClaim();

    const suggestedAmount =
      claim
        ? Math.min(
          claim.claimedAmount,
          claim.coverageAmount
        )
        : 0;

    this.approveForm.reset({
      approvedAmount:
        suggestedAmount > 0
          ? suggestedAmount.toFixed(2)
          : '',
      reviewRemarks: ''
    });

    this.rejectForm.reset({
      rejectionReason: '',
      reviewRemarks: ''
    });

    this.activeDecisionPanel.set(
      'APPROVE'
    );

    this.revalidateApprovedAmount();
  }

  openRejectPanel(): void {
    if (
      !this.canResolveClaim()
      || this.actionInProgress()
    ) {
      return;
    }

    this.claimState.clearError();

    this.approveForm.reset({
      approvedAmount: '',
      reviewRemarks: ''
    });

    this.rejectForm.reset({
      rejectionReason: '',
      reviewRemarks: ''
    });

    this.activeDecisionPanel.set(
      'REJECT'
    );
  }

  closeDecisionPanel(): void {
    if (this.actionInProgress()) {
      return;
    }

    this.activeDecisionPanel.set(null);

    this.approveForm.reset({
      approvedAmount: '',
      reviewRemarks: ''
    });

    this.rejectForm.reset({
      rejectionReason: '',
      reviewRemarks: ''
    });

    this.claimState.clearError();
  }

  approveClaim(): void {
    this.claimState.clearError();

    if (
      this.actionInProgress()
      || !this.claimId
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
      claim.status !== 'UNDER_REVIEW'
    ) {
      this.notificationService.showError(
        'Only a claim under review can be approved.'
      );

      this.activeDecisionPanel.set(null);

      return;
    }

    this.revalidateApprovedAmount();

    if (this.approveForm.invalid) {
      this.approveForm.markAllAsTouched();

      this.notificationService.showError(
        'Review the approval information and '
        + 'correct the highlighted fields.'
      );

      return;
    }

    const formValue =
      this.approveForm.getRawValue();

    const approvedAmount =
      Number(formValue.approvedAmount);

    const maximumAmount =
      Math.min(
        claim.claimedAmount,
        claim.coverageAmount
      );

    if (
      !Number.isFinite(approvedAmount)
      || approvedAmount <= 0
    ) {
      this.approveForm.controls
        .approvedAmount
        .setErrors({
          positiveAmount: true
        });

      return;
    }

    if (
      approvedAmount > maximumAmount
    ) {
      this.approveForm.controls
        .approvedAmount
        .setErrors({
          maximumApprovalAmount: {
            maximumAmount
          }
        });

      this.approveForm.controls
        .approvedAmount
        .markAsTouched();

      this.notificationService.showError(
        'Approved amount cannot exceed the '
        + 'claimed amount or coverage amount.'
      );

      return;
    }

    const reviewRemarks =
      formValue.reviewRemarks.trim();

    if (reviewRemarks.length === 0) {
      this.approveForm.controls
        .reviewRemarks
        .setErrors({
          required: true
        });

      this.approveForm.controls
        .reviewRemarks
        .markAsTouched();

      return;
    }

    const request:
      ApproveClaimRequest = {
        approvedAmount:
          this.normalizeMoney(
            approvedAmount
          ),
        reviewRemarks
      };

    this.claimState.approveClaim(
      claim.id,
      request,
      () => {
        this.activeDecisionPanel.set(null);

        this.approveForm.reset({
          approvedAmount: '',
          reviewRemarks: ''
        });

        this.notificationService.showSuccess(
          'Claim approved successfully.'
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  rejectClaim(): void {
    this.claimState.clearError();

    if (
      this.actionInProgress()
      || !this.claimId
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
      claim.status !== 'UNDER_REVIEW'
    ) {
      this.notificationService.showError(
        'Only a claim under review can be rejected.'
      );

      this.activeDecisionPanel.set(null);

      return;
    }

    if (this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();

      this.notificationService.showError(
        'Review the rejection information and '
        + 'correct the highlighted fields.'
      );

      return;
    }

    const formValue =
      this.rejectForm.getRawValue();

    const rejectionReason =
      formValue.rejectionReason.trim();

    const reviewRemarks =
      formValue.reviewRemarks.trim();

    if (rejectionReason.length === 0) {
      this.rejectForm.controls
        .rejectionReason
        .setErrors({
          required: true
        });

      this.rejectForm.controls
        .rejectionReason
        .markAsTouched();

      return;
    }

    if (reviewRemarks.length === 0) {
      this.rejectForm.controls
        .reviewRemarks
        .setErrors({
          required: true
        });

      this.rejectForm.controls
        .reviewRemarks
        .markAsTouched();

      return;
    }

    const request:
      RejectClaimRequest = {
        rejectionReason,
        reviewRemarks
      };

    this.claimState.rejectClaim(
      claim.id,
      request,
      () => {
        this.activeDecisionPanel.set(null);

        this.rejectForm.reset({
          rejectionReason: '',
          reviewRemarks: ''
        });

        this.notificationService.showSuccess(
          'Claim rejected successfully.'
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  revalidateApprovedAmount(): void {
    const control =
      this.approveForm.controls
        .approvedAmount;

    const currentErrors:
      ValidationErrors = {
        ...(control.errors ?? {})
      };

    delete currentErrors[
      'maximumApprovalAmount'
    ];

    const approvedAmount =
      Number(control.value);

    const maximumAmount =
      this.maximumApprovalAmount();

    if (
      maximumAmount !== null
      && Number.isFinite(approvedAmount)
      && approvedAmount > maximumAmount
    ) {
      currentErrors[
        'maximumApprovalAmount'
      ] = {
        maximumAmount
      };
    }

    const errorKeys =
      Object.keys(currentErrors);

    control.setErrors(
      errorKeys.length > 0
        ? currentErrors
        : null
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

    return 'inbox';
  }

  getStatusTitle(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Claim review is in progress';
    }

    if (status === 'APPROVED') {
      return 'Claim approval completed';
    }

    if (status === 'REJECTED') {
      return 'Claim rejection completed';
    }

    if (status === 'CANCELLED') {
      return 'Claim cancelled by Provider';
    }

    return 'Claim awaiting review';
  }

  getStatusDescription(
    status: ClaimStatus
  ): string {
    if (status === 'UNDER_REVIEW') {
      return 'Review the verified claim information and '
        + 'approve or reject the request.';
    }

    if (status === 'APPROVED') {
      return 'The claim has been approved and no further '
        + 'review action is available.';
    }

    if (status === 'REJECTED') {
      return 'The claim has been rejected and no further '
        + 'review action is available.';
    }

    if (status === 'CANCELLED') {
      return 'The Provider cancelled this claim before '
        + 'the review process was completed.';
    }

    return 'Start review to evaluate the claim and record '
      + 'an approval or rejection decision.';
  }

  private positiveMoneyValidator():
    ValidatorFn {

    return (
      control: AbstractControl
    ): ValidationErrors | null => {
      const rawValue =
        control.value;

      if (
        rawValue === null
        || rawValue === undefined
        || String(rawValue).trim().length
          === 0
      ) {
        return null;
      }

      const amount =
        Number(rawValue);

      if (
        !Number.isFinite(amount)
        || amount <= 0
      ) {
        return {
          positiveAmount: true
        };
      }

      return null;
    };
  }

  private moneyPrecisionValidator():
    ValidatorFn {

    return (
      control: AbstractControl
    ): ValidationErrors | null => {
      const rawValue =
        control.value;

      if (
        rawValue === null
        || rawValue === undefined
        || String(rawValue).trim().length
          === 0
      ) {
        return null;
      }

      const normalizedValue =
        String(rawValue).trim();

      const moneyPattern =
        /^\d{1,10}(\.\d{1,2})?$/;

      if (
        !moneyPattern.test(
          normalizedValue
        )
      ) {
        return {
          moneyPrecision: true
        };
      }

      return null;
    };
  }

  private normalizeMoney(
    amount: number
  ): number {
    return Math.round(
      amount * 100
    ) / 100;
  }
}