import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  CurrencyPipe
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
  provideNativeDateAdapter
} from '@angular/material/core';

import {
  MatDatepickerModule
} from '@angular/material/datepicker';

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
  MatSelectModule
} from '@angular/material/select';

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
  CreateEnrollmentRequest
} from '../../models/create-enrollment-request';

import {
  NomineeRelationship,
  NOMINEE_RELATIONSHIP_OPTIONS
} from '../../models/nominee-relationship';

import {
  EnrollmentStateService
} from '../../services/enrollment-state.service';

import {
  InsurancePlanStateService
} from '../../services/insurance-plan-state.service';

@Component({
  selector: 'app-create-enrollment',
  imports: [
    CurrencyPipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  providers: [
    provideNativeDateAdapter()
  ],
  templateUrl: './create-enrollment.html',
  styleUrl: './create-enrollment.scss'
})
export class CreateEnrollment
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

  readonly enrollmentState =
    inject(EnrollmentStateService);

  readonly nomineeRelationshipOptions =
    NOMINEE_RELATIONSHIP_OPTIONS;

  readonly minimumCoverageStartDate =
    this.createTodayDate();

  readonly enrollmentForm =
    this.formBuilder.nonNullable.group({
      requestedCoverageStartDate: [
        null as Date | null,
        [
          Validators.required
        ]
      ],

      nomineeName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150),
          Validators.pattern(
            /^[A-Za-z][A-Za-z .'-]*$/
          )
        ]
      ],

      nomineeRelationship: [
        null as NomineeRelationship | null,
        [
          Validators.required
        ]
      ],

      nomineeContactNumber: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[0-9+()\- ]{10,20}$/
          )
        ]
      ]
    });

  ngOnInit(): void {

    const planId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('planId');

    if (!planId) {

      this.notificationMessage.showError(
        'Select an insurance plan before creating an enrollment.'
      );

      this.goBackToPlans();

      return;
    }

    this.planState
      .loadAvailablePlanById(planId);
  }

  ngOnDestroy(): void {

    this.planState.clearSelectedPlan();
    this.enrollmentState.clearError();
  }

  reviewAndSubmit(): void {

    this.enrollmentState.clearError();

    if (this.enrollmentForm.invalid) {

      this.enrollmentForm.markAllAsTouched();

      this.notificationMessage.showError(
        'Please review the highlighted enrollment fields.'
      );

      return;
    }

    if (this.enrollmentState.submitting()) {
      return;
    }

    const plan =
      this.planState.selectedPlan();

    if (!plan) {

      this.notificationMessage.showError(
        'Insurance plan information is unavailable.'
      );

      return;
    }

    const formValue =
      this.enrollmentForm.getRawValue();

    const selectedDate =
      formValue.requestedCoverageStartDate;

    const relationship =
      formValue.nomineeRelationship;

    if (!selectedDate || !relationship) {

      this.enrollmentForm.markAllAsTouched();

      return;
    }

    const relationshipLabel =
      this.getRelationshipLabel(
        relationship
      );

    const dialogData:
      ConfirmationDialogData = {

        title: 'Submit enrollment?',

        message:
          `Submit your enrollment request for `
          + `${plan.planName} with `
          + `${formValue.nomineeName.trim()} `
          + `as your ${relationshipLabel.toLowerCase()}?`,

        confirmText: 'Submit enrollment',

        cancelText: 'Review details',

        icon: 'assignment_turned_in',

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

          this.submitEnrollment(
            plan.id,
            selectedDate,
            relationship
          );
        }
      );
  }

  goBackToPlan(): void {

    const plan =
      this.planState.selectedPlan();

    if (!plan) {

      this.goBackToPlans();

      return;
    }

    this.router.navigate([
      '/member/insurance-plans',
      plan.id
    ]);
  }

  goBackToPlans(): void {

    this.router.navigate([
      '/member/insurance-plans'
    ]);
  }

  getRelationshipLabel(
    relationship: NomineeRelationship
  ): string {

    return this.nomineeRelationshipOptions
      .find(
        option =>
          option.value === relationship
      )?.label ?? relationship;
  }

  private submitEnrollment(
    planId: string,
    requestedCoverageStartDate: Date,
    nomineeRelationship: NomineeRelationship
  ): void {

    const formValue =
      this.enrollmentForm.getRawValue();

    const request:
      CreateEnrollmentRequest = {

      planId,

      requestedCoverageStartDate:
        this.formatLocalDate(
          requestedCoverageStartDate
        ),

      nomineeName:
        formValue.nomineeName.trim(),

      nomineeRelationship,

      nomineeContactNumber:
        formValue
          .nomineeContactNumber
          .trim()
    };

    this.enrollmentState.createEnrollment(
      request,
      enrollment => {

        this.notificationMessage.showSuccess(
          'Insurance enrollment submitted successfully.'
        );

        this.router.navigate(
          ['/member/enrollments'],
          {
            queryParams: {
              created:
                enrollment.id
            }
          }
        );
      },
      message => {

        this.notificationMessage.showError(
          message
        );
      }
    );
  }

  private createTodayDate(): Date {

    const today =
      new Date();

    today.setHours(
      0,
      0,
      0,
      0
    );

    return today;
  }

  private formatLocalDate(
    date: Date
  ): string {

    const year =
      date.getFullYear();

    const month =
      String(
        date.getMonth() + 1
      ).padStart(2, '0');

    const day =
      String(
        date.getDate()
      ).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}