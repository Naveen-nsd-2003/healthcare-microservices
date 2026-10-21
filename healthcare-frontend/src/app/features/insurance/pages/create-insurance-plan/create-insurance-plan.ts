import {
  Component,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

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
  ConfirmationDialog
} from '../../../../shared/dialogs/confirmation-dialog/confirmation-dialog';

import {
  ConfirmationDialogData
} from '../../../../shared/models/confirmation-dialog-data';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  CreateInsurancePlanRequest
} from '../../models/create-insurance-plan-request';

import {
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

@Component({
  selector: 'app-create-insurance-plan',
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
  templateUrl: './create-insurance-plan.html',
  styleUrl: './create-insurance-plan.scss'
})
export class CreateInsurancePlan {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly router =
    inject(Router);

  private readonly dialog =
    inject(MatDialog);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly planState =
    inject(InsurancePlanStateService);

  readonly planForm =
    this.formBuilder.nonNullable.group({
      planName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150)
        ]
      ],

      planCode: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[A-Za-z0-9-]{3,50}$/
          )
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
        true
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

  goBack(): void {

    this.router.navigate([
      '/payer/insurance-plans'
    ]);
  }

  reviewAndCreate(): void {

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

      this.planForm.controls
        .maximumAge
        .setErrors({
          ageRange: true
        });

      this.planForm.controls
        .maximumAge
        .markAsTouched();

      this.notificationMessage.showError(
        'Maximum age must be greater than or equal to minimum age.'
      );

      return;
    }

    if (
      !this.hasAtLeastOneBenefit()
    ) {

      this.notificationMessage.showError(
        'Select at least one healthcare coverage benefit.'
      );

      return;
    }

    if (this.planState.submitting()) {
      return;
    }

    const dialogData:
      ConfirmationDialogData = {

      title: 'Create insurance plan?',

      message:
        'Create '
        + formValue.planName.trim()
        + ' with plan code '
        + formValue.planCode.trim().toUpperCase()
        + '? The plan will initially be created as DRAFT.',

      confirmText: 'Create plan',

      cancelText: 'Review details',

      icon: 'health_and_safety',

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

          this.createPlan();
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

  private createPlan(): void {

    const formValue =
      this.planForm.getRawValue();

    const request:
      CreateInsurancePlanRequest = {

      planName:
        formValue.planName.trim(),

      planCode:
        formValue.planCode
          .trim()
          .toUpperCase(),

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

      waitingPeriodDays:
        Number(
          formValue.waitingPeriodDays
        ),

      hospitalizationCoverage:
        formValue.hospitalizationCoverage,

      outpatientCoverage:
        formValue.outpatientCoverage,

      prescriptionCoverage:
        formValue.prescriptionCoverage,

      maternityCoverage:
        formValue.maternityCoverage
    };

    this.planState.createPlan(
      request,
      plan => {

        this.notificationMessage.showSuccess(
          'Insurance plan created successfully as a draft.'
        );

        this.router.navigate([
          '/payer/insurance-plans',
          plan.id
        ]);
      },
      message => {

        this.notificationMessage.showError(
          message
        );
      }
    );
  }
}