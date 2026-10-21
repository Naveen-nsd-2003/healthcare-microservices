import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  effect,
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
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  EligibleClaimPolicy
} from '../../models/eligible-claim-policy';

import {
  SubmitClaimRequest
} from '../../models/submit-claim-request';

import {
  ClaimStateService
} from '../../services/claim-state.service';

@Component({
  selector: 'app-submit-claim',
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
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './submit-claim.html',
  styleUrl: './submit-claim.scss'
})
export class SubmitClaim
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly claimState =
    inject(ClaimStateService);

  private appointmentId: string | null =
    null;

  private medicalRecordId: string | null =
    null;

  private readonly formInitialized =
    signal(false);

  readonly selectedPolicyId =
    signal('');

  readonly selectedPolicy =
    computed<EligibleClaimPolicy | null>(() => {
      const context =
        this.claimState.submissionContext();

      const policyId =
        this.selectedPolicyId();

      if (
        !context
        || policyId.length === 0
      ) {
        return null;
      }

      return context.eligiblePolicies
        .find(policy => {
          return policy.policyId === policyId;
        })
        ?? null;
    });

  readonly maximumClaimAmount =
    computed<number | null>(() => {
      const context =
        this.claimState.submissionContext();

      const policy =
        this.selectedPolicy();

      if (!context || !policy) {
        return null;
      }

      return Math.min(
        context.appointment.consultationFee,
        policy.coverageAmount
      );
    });

  readonly hasEligiblePolicies =
    computed(() => {
      return (
        this.claimState
          .submissionContext()
          ?.eligiblePolicies
          .length
        ?? 0
      ) > 0;
    });

  readonly claimForm =
    this.formBuilder.nonNullable.group({
      policyId: [
        '',
        [
          Validators.required
        ]
      ],
      claimedAmount: [
        '',
        [
          Validators.required,
          this.positiveMoneyValidator(),
          this.moneyPrecisionValidator()
        ]
      ],
      claimDescription: [
        '',
        [
          Validators.required,
          Validators.maxLength(1500)
        ]
      ]
    });

  private readonly initializeContextEffect =
    effect(() => {
      const context =
        this.claimState.submissionContext();

      if (
        !context
        || this.formInitialized()
      ) {
        return;
      }

      const eligiblePolicies =
        context.eligiblePolicies;

      if (eligiblePolicies.length === 1) {
        const policyId =
          eligiblePolicies[0].policyId;

        this.selectedPolicyId.set(
          policyId
        );

        this.claimForm.controls.policyId
          .setValue(
            policyId
          );
      }

      const defaultClaimAmount =
        context.appointment.consultationFee;

      if (
        Number.isFinite(defaultClaimAmount)
        && defaultClaimAmount > 0
      ) {
        this.claimForm.controls
          .claimedAmount
          .setValue(
            defaultClaimAmount.toFixed(2)
          );
      }

      this.claimForm.markAsPristine();

      this.claimForm.markAsUntouched();

      this.formInitialized.set(true);

      this.revalidateClaimedAmount();
    });

  ngOnInit(): void {
    this.appointmentId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('appointmentId');

    this.medicalRecordId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('medicalRecordId');

    if (
      !this.appointmentId
      || !this.medicalRecordId
    ) {
      this.notificationService.showError(
        'Appointment and Medical Record '
        + 'identifiers are required.'
      );

      this.goBackToMedicalRecords();

      return;
    }

    this.loadSubmissionContext();
  }

  ngOnDestroy(): void {
    this.claimState
      .clearSubmissionContext();
  }

  loadSubmissionContext(): void {
    if (
      !this.appointmentId
      || !this.medicalRecordId
    ) {
      return;
    }

    this.formInitialized.set(false);

    this.selectedPolicyId.set('');

    this.claimForm.reset({
      policyId: '',
      claimedAmount: '',
      claimDescription: ''
    });

    this.claimState
      .loadProviderClaimSubmissionContext(
        this.appointmentId,
        this.medicalRecordId
      );
  }

  updateSelectedPolicy(
    policyId: string
  ): void {
    this.selectedPolicyId.set(
      policyId
    );

    this.claimForm.controls.policyId
      .setValue(
        policyId
      );

    this.revalidateClaimedAmount();
  }

  submitClaim(): void {
    this.claimState.clearError();

    if (
      this.claimState.submitting()
    ) {
      return;
    }

    const context =
      this.claimState.submissionContext();

    if (!context) {
      this.notificationService.showError(
        'Claim submission information '
        + 'is unavailable.'
      );

      return;
    }

    if (
      context.eligiblePolicies.length === 0
    ) {
      this.notificationService.showError(
        'No eligible insurance policy covers '
        + 'this appointment service date.'
      );

      return;
    }

    this.revalidateClaimedAmount();

    if (this.claimForm.invalid) {
      this.claimForm.markAllAsTouched();

      this.notificationService.showError(
        'Review the claim information and '
        + 'correct the highlighted fields.'
      );

      return;
    }

    const formValue =
      this.claimForm.getRawValue();

    const policy =
      context.eligiblePolicies
        .find(eligiblePolicy => {
          return eligiblePolicy.policyId
            === formValue.policyId;
        });

    if (!policy) {
      this.claimForm.controls.policyId
        .setErrors({
          required: true
        });

      this.notificationService.showError(
        'Select an eligible insurance policy.'
      );

      return;
    }

    const claimedAmount =
      Number(formValue.claimedAmount);

    const maximumAmount =
      Math.min(
        context.appointment.consultationFee,
        policy.coverageAmount
      );

    if (
      !Number.isFinite(claimedAmount)
      || claimedAmount <= 0
    ) {
      this.claimForm.controls
        .claimedAmount
        .setErrors({
          positiveAmount: true
        });

      this.notificationService.showError(
        'Claimed amount must be greater '
        + 'than zero.'
      );

      return;
    }

    if (claimedAmount > maximumAmount) {
      this.claimForm.controls
        .claimedAmount
        .setErrors({
          maximumClaimAmount: {
            maximumAmount
          }
        });

      this.notificationService.showError(
        'Claimed amount cannot exceed '
        + 'the allowed maximum amount.'
      );

      return;
    }

    const claimDescription =
      formValue.claimDescription.trim();

    if (claimDescription.length === 0) {
      this.claimForm.controls
        .claimDescription
        .setErrors({
          required: true
        });

      this.notificationService.showError(
        'Claim description is required.'
      );

      return;
    }

    const request:
      SubmitClaimRequest = {
        appointmentId:
          context.appointment.appointmentId,
        medicalRecordId:
          context.medicalRecord.medicalRecordId,
        policyId:
          policy.policyId,
        claimedAmount:
          this.normalizeMoney(
            claimedAmount
          ),
        claimDescription
      };

    this.claimState.submitClaim(
      request,
      claim => {
        this.notificationService.showSuccess(
          'Claim submitted successfully.'
        );

        this.router.navigate([
          '/provider/claims',
          claim.id
        ]);
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  goBack(): void {
    const medicalRecordId =
      this.medicalRecordId;

    if (medicalRecordId) {
      this.router.navigate([
        '/provider/medical-records',
        medicalRecordId
      ]);

      return;
    }

    this.goBackToMedicalRecords();
  }

  goBackToMedicalRecords(): void {
    this.router.navigate([
      '/provider/medical-records'
    ]);
  }

  getTimeRange(
    startTime: string,
    endTime: string
  ): string {
    return this.getTimeLabel(
      startTime
    )
      + ' - '
      + this.getTimeLabel(
        endTime
      );
  }

  getPolicyDisplayLabel(
    policy: EligibleClaimPolicy
  ): string {
    return policy.planName
      + ' · '
      + policy.policyNumber;
  }

  revalidateClaimedAmount(): void {
    const control =
      this.claimForm.controls
        .claimedAmount;

    const currentValue =
      control.value;

    const errors:
      ValidationErrors = {
        ...(control.errors ?? {})
      };

    delete errors[
      'maximumClaimAmount'
    ];

    const amount =
      Number(currentValue);

    const maximumAmount =
      this.maximumClaimAmount();

    if (
      maximumAmount !== null
      && Number.isFinite(amount)
      && amount > maximumAmount
    ) {
      errors[
        'maximumClaimAmount'
      ] = {
        maximumAmount
      };
    }

    const remainingErrorKeys =
      Object.keys(errors);

    control.setErrors(
      remainingErrorKeys.length > 0
        ? errors
        : null
    );
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

  private getTimeLabel(
    timeValue: string
  ): string {
    if (!timeValue) {
      return 'Not available';
    }

    const parts =
      timeValue.split(':');

    if (parts.length < 2) {
      return timeValue;
    }

    const hours =
      Number(parts[0]);

    const minutes =
      Number(parts[1]);

    if (
      Number.isNaN(hours)
      || Number.isNaN(minutes)
      || hours < 0
      || hours > 23
      || minutes < 0
      || minutes > 59
    ) {
      return timeValue;
    }

    const period =
      hours >= 12
        ? 'PM'
        : 'AM';

    const displayHours =
      hours % 12 === 0
        ? 12
        : hours % 12;

    const displayMinutes =
      String(minutes).padStart(
        2,
        '0'
      );

    return displayHours
      + ':'
      + displayMinutes
      + ' '
      + period;
  }
}
