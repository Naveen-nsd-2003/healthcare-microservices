import {
  Component,
  computed,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  MAT_DIALOG_DATA,
  MatDialogRef
} from '@angular/material/dialog';

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
  EnrollmentReviewDialogData,
  EnrollmentReviewDialogResult
} from '../../models/enrollment-review-dialog-data';

@Component({
  selector: 'app-enrollment-review-dialog',
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule
  ],
  templateUrl: './enrollment-review-dialog.html',
  styleUrl: './enrollment-review-dialog.scss'
})
export class EnrollmentReviewDialog {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly dialogReference =
    inject(
      MatDialogRef<
        EnrollmentReviewDialog,
        EnrollmentReviewDialogResult
      >
    );

  readonly data =
    inject<EnrollmentReviewDialogData>(
      MAT_DIALOG_DATA
    );

  readonly reviewForm =
    this.formBuilder.nonNullable.group({
      reviewRemarks: [
        '',
        [
          Validators.required,
          Validators.maxLength(500)
        ]
      ]
    });

  readonly approving =
    computed(
      () =>
        this.data.action === 'APPROVE'
    );

  readonly dialogTitle =
    computed(
      () =>
        this.approving()
          ? 'Approve enrollment'
          : 'Reject enrollment'
    );

  readonly dialogDescription =
    computed(
      () => {

        if (this.approving()) {

          return 'Confirm that the enrollment information '
            + 'has been reviewed and is eligible for approval.';
        }

        return 'Provide a clear reason for rejecting '
          + 'this enrollment request.';
      }
    );

  readonly actionButtonText =
    computed(
      () =>
        this.approving()
          ? 'Approve enrollment'
          : 'Reject enrollment'
    );

  readonly actionIcon =
    computed(
      () =>
        this.approving()
          ? 'check_circle'
          : 'cancel'
    );

  cancel(): void {

    this.dialogReference.close();
  }

  submitReview(): void {

    if (this.reviewForm.invalid) {

      this.reviewForm.markAllAsTouched();

      return;
    }

    const reviewRemarks =
      this.reviewForm.controls
        .reviewRemarks
        .getRawValue()
        .trim();

    if (reviewRemarks.length === 0) {

      this.reviewForm.controls
        .reviewRemarks
        .setErrors({
          required: true
        });

      this.reviewForm.controls
        .reviewRemarks
        .markAsTouched();

      return;
    }

    const result:
      EnrollmentReviewDialogResult = {

      action:
        this.data.action,

      reviewRemarks
    };

    this.dialogReference.close(
      result
    );
  }
}