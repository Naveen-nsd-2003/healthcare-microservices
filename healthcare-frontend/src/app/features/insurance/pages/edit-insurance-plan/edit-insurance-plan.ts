import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

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
  MatCheckboxModule
} from '@angular/material/checkbox';

import {
  MatDialog
} from '@angular/material/dialog';

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
  filter
} from 'rxjs';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  ConfirmationDialog
} from '../../../../shared/dialogs/confirmation-dialog/confirmation-dialog';

import {
  ConfirmationDialogData
} from '../../../../shared/models/confirmation-dialog-data';

import {
  InsurancePlan
} from '../../models/insurance-plan';

import {
  UpdateInsurancePlanRequest
} from '../../models/update-insurance-plan-request';

import {
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

@Component({
  selector: 'app-edit-insurance-plan',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './edit-insurance-plan.html',
  styleUrl: './edit-insurance-plan.scss'
})
export class EditInsurancePlan
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly dialog =
    inject(MatDialog);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly planState =
    inject(InsurancePlanStateService);

  private planId: string | null =
    null;

  readonly planForm =
    this.formBuilder.nonNullable.group({
      planName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150)
        ]
      ],

      description: [
        '',
        [
          Validators.required,
          Validators.maxLength(1000)
        ]
      ],

      coverageAmount: [
        0,
        [
          Validators.required,
          Validators.min(1)
        ]
      ],

      monthlyPremium: [
        0,
        [
          Validators.required,
          Validators.min(0.01)
        ]
      ],

      annualDeductible: [
        0,
        [
          Validators.required,
          Validators.min(0)
        ]
      ],

      copayPercentage: [
        0,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(100)
        ]
      ],

      coinsurancePercentage: [
        0,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(100)
        ]
      ],

      minimumAge: [
        0,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(120)
        ]
      ],

      maximumAge: [
        100,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(120)
        ]
      ],

      policyDurationMonths: [
        12,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(120)
        ]
      ],

      waitingPeriodDays: [
        0,
        [
          Validators.required,
          Validators.min(0),
          Validators.max(1460)
        ]
      ],

      hospitalizationCoverage: [
        false
      ],

      outpatientCoverage: [
        false
      ],

      prescriptionCoverage: [
        false
      ],

      maternityCoverage: [
        false
      ]
    });

  ngOnInit(): void {

    this.planId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('planId');

    if (!this.planId) {

      this.notificationMessage.showError(
        'Insurance plan identifier is missing.'
      );

      this.goBackToPlans();

      return;
    }

    this.planState.loadMyPlanById(
      this.planId,
      plan => {

        this.populateForm(plan);
      }
    );
  }

  ngOnDestroy(): void {

    this.planState.clearSelectedPlan();
  }

  goBackToPlans(): void {

    this.router.navigate([
      '/payer/insurance-plans'
    ]);
  }

  goBackToDetails(): void {

    const selectedPlan =
      this.planState.selectedPlan();

    const targetPlanId =
      selectedPlan?.id
      ?? this.planId;

    if (!targetPlanId) {

      this.goBackToPlans();

      return;
    }

    this.router.navigate([
      '/payer/insurance-plans',
      targetPlanId
    ]);
  }

  reviewAndUpdate(): void {

    this.planState.clearError();

    if (this.planForm.invalid) {

      this.planForm.markAllAsTouched();

      this.notificationMessage.showError(
        'Please review the highlighted insurance plan fields.'
      );

      return;
    }

    const formValue =
      this.planForm.getRawValue();

    if (
      formValue.maximumAge
      < formValue.minimumAge
    ) {

      this.notificationMessage.showError(
        'Maximum age must be greater than or equal to minimum age.'
      );

      return;
    }

    if (!this.hasAtLeastOneBenefit()) {

      this.notificationMessage.showError(
        'Select at least one healthcare coverage benefit.'
      );

      return;
    }

    if (this.planState.updating()) {
      return;
    }

    const selectedPlan =
      this.planState.selectedPlan();

    if (!selectedPlan) {

      this.notificationMessage.showError(
        'Insurance plan information is unavailable.'
      );

      return;
    }

    const dialogData:
      ConfirmationDialogData = {
        title: 'Save plan changes?',

        message:
          'Update '
          + selectedPlan.planName
          + ' ('
          + selectedPlan.planCode
          + ') with the current form values?',

        confirmText: 'Save changes',

        cancelText: 'Review details',

        icon: 'save',

        type: 'default'
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

          this.submitUpdate(
            selectedPlan.id
          );
        }
      );
  }

  hasAtLeastOneBenefit(): boolean {

    const formValue =
      this.planForm.getRawValue();

    return formValue.hospitalizationCoverage
      || formValue.outpatientCoverage
      || formValue.prescriptionCoverage
      || formValue.maternityCoverage;
  }

  private populateForm(
    plan: InsurancePlan
  ): void {

    this.planForm.setValue({
      planName:
        plan.planName,

      description:
        plan.description,

      coverageAmount:
        plan.coverageAmount,

      monthlyPremium:
        plan.monthlyPremium,

      annualDeductible:
        plan.annualDeductible,

      copayPercentage:
        plan.copayPercentage,

      coinsurancePercentage:
        plan.coinsurancePercentage,

      minimumAge:
        plan.minimumAge,

      maximumAge:
        plan.maximumAge,

      policyDurationMonths:
        plan.policyDurationMonths,

      waitingPeriodDays:
        plan.waitingPeriodDays,

      hospitalizationCoverage:
        plan.hospitalizationCoverage,

      outpatientCoverage:
        plan.outpatientCoverage,

      prescriptionCoverage:
        plan.prescriptionCoverage,

      maternityCoverage:
        plan.maternityCoverage
    });

    this.planForm.markAsPristine();
    this.planForm.markAsUntouched();
  }

  private submitUpdate(
    planId: string
  ): void {

    const request =
      this.buildUpdateRequest();

    this.planState.updateMyPlan(
      planId,
      request,
      updatedPlan => {

        this.notificationMessage.showSuccess(
          'Insurance plan updated successfully.'
        );

        this.router.navigate([
          '/payer/insurance-plans',
          updatedPlan.id
        ]);
      },
      message => {

        this.notificationMessage.showError(
          message
        );
      }
    );
  }

  private buildUpdateRequest():
    UpdateInsurancePlanRequest {

    const formValue =
      this.planForm.getRawValue();

    return {
      planName:
        formValue.planName.trim(),

      description:
        formValue.description.trim(),

      coverageAmount:
        Number(
          formValue.coverageAmount
        ),

      monthlyPremium:
        Number(
          formValue.monthlyPremium
        ),

      annualDeductible:
        Number(
          formValue.annualDeductible
        ),

      copayPercentage:
        Number(
          formValue.copayPercentage
        ),

      coinsurancePercentage:
        Number(
          formValue.coinsurancePercentage
        ),

      minimumAge:
        Number(
          formValue.minimumAge
        ),

      maximumAge:
        Number(
          formValue.maximumAge
        ),

      policyDurationMonths:
        Number(
          formValue.policyDurationMonths
        ),

      hospitalizationCoverage:
        formValue.hospitalizationCoverage,

      outpatientCoverage:
        formValue.outpatientCoverage,

      prescriptionCoverage:
        formValue.prescriptionCoverage,

      maternityCoverage:
        formValue.maternityCoverage,

      waitingPeriodDays:
        Number(
          formValue.waitingPeriodDays
        )
    };
  }
}