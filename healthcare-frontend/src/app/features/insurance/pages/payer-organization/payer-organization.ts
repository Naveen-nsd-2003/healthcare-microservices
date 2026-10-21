import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

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
  CreatePayerOrganizationRequest
} from '../../models/create-payer-organization-request';

import {
  PayerOrganization
} from '../../models/payer-organization';

import {
  PayerStatus
} from '../../models/payer-status';

import {
  UpdatePayerOrganizationRequest
} from '../../models/update-payer-organization-request';

import {
  PayerOrganizationStateService
} from '../../services/payer-organization-state.service';

@Component({
  selector: 'app-payer-organization',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './payer-organization.html',
  styleUrl: './payer-organization.scss'
})
export class PayerOrganizationPage
  implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly organizationState =
    inject(PayerOrganizationStateService);

  readonly editing =
    signal(false);

  readonly creating =
    computed(() => {
      return this.organizationState
        .organizationNotCreated();
    });

  readonly correctingRejectedApplication =
    computed(() => {
      return this.editing()
        && this.organizationState
          .organizationIsRejected();
    });

  readonly showForm =
    computed(() => {
      return this.creating()
        || (
          this.editing()
          && this.organizationState.hasOrganization()
        );
    });

  readonly canEditOrganization =
    computed(() => {

      const organization =
        this.organizationState.organization();

      if (!organization) {
        return false;
      }

      return organization.status === 'ACTIVE'
        || organization.status === 'PENDING_APPROVAL'
        || organization.status === 'REJECTED';
    });

  readonly formTitle =
    computed(() => {

      if (this.creating()) {
        return 'Submit Payer organization';
      }

      if (this.correctingRejectedApplication()) {
        return 'Correct and resubmit organization';
      }

      return 'Update Payer organization';
    });

  readonly formDescription =
    computed(() => {

      if (this.creating()) {

        return 'Provide your organization registration, '
          + 'contact, and address information for '
          + 'Administrator review.';
      }

      if (this.correctingRejectedApplication()) {

        return 'Correct the organization information '
          + 'identified during review. Saving these changes '
          + 'will resubmit the application for approval.';
      }

      return 'Update your organization name, contact '
        + 'information, website, and registered address.';
    });

  readonly submitButtonLabel =
    computed(() => {

      if (this.creating()) {
        return 'Submit for approval';
      }

      if (this.correctingRejectedApplication()) {
        return 'Correct and resubmit';
      }

      return 'Save changes';
    });

  readonly submittingLabel =
    computed(() => {

      if (this.creating()) {
        return 'Submitting application';
      }

      if (this.correctingRejectedApplication()) {
        return 'Resubmitting application';
      }

      return 'Saving changes';
    });

  readonly organizationForm =
    this.formBuilder.nonNullable.group({
      organizationName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150)
        ]
      ],

      registrationNumber: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ],

      contactNumber: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[0-9+()\- ]{10,20}$/
          )
        ]
      ],

      website: [
        '',
        [
          Validators.maxLength(255)
        ]
      ],

      addressLine: [
        '',
        [
          Validators.required,
          Validators.maxLength(255)
        ]
      ],

      city: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ],

      state: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ],

      postalCode: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[A-Za-z0-9 -]{3,20}$/
          )
        ]
      ],

      country: [
        '',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ]
    });

  ngOnInit(): void {

    this.loadOrganization();
  }

  loadOrganization(): void {

    if (
      this.organizationState.loading()
      || this.organizationState.saving()
    ) {
      return;
    }

    this.editing.set(
      false
    );

    this.organizationState
      .loadMyOrganization(
        organization => {

          this.populateForm(
            organization
          );
        },
        () => {

          this.prepareCreationForm();
        },
        message => {

          this.notificationMessage
            .showError(
              message
            );
        }
      );
  }

  startEditing(): void {

    const organization =
      this.organizationState.organization();

    if (
      !organization
      || !this.canEditOrganization()
      || this.organizationState.saving()
    ) {
      return;
    }

    this.populateForm(
      organization
    );

    this.organizationState.clearError();

    this.editing.set(
      true
    );
  }

  cancelEditing(): void {

    if (this.organizationState.saving()) {
      return;
    }

    const organization =
      this.organizationState.organization();

    if (organization) {

      this.populateForm(
        organization
      );

      this.editing.set(
        false
      );

    } else {

      this.prepareCreationForm();
    }

    this.organizationState.clearError();
  }

  submitOrganization(): void {

    this.organizationState.clearError();

    if (this.organizationState.saving()) {
      return;
    }

    if (this.organizationForm.invalid) {

      this.organizationForm.markAllAsTouched();

      this.notificationMessage.showError(
        'Please review the highlighted '
        + 'Payer organization fields.'
      );

      return;
    }

    if (this.creating()) {

      this.createOrganization();

      return;
    }

    this.updateOrganization();
  }

  getStatusClass(
    status: PayerStatus
  ): string {

    switch (status) {

      case 'PENDING_APPROVAL':
        return 'status-badge--pending';

      case 'ACTIVE':
        return 'status-badge--active';

      case 'REJECTED':
        return 'status-badge--rejected';

      case 'SUSPENDED':
        return 'status-badge--suspended';

      case 'INACTIVE':
        return 'status-badge--inactive';

      default:
        return '';
    }
  }

  getStatusLabel(
    status: PayerStatus
  ): string {

    switch (status) {

      case 'PENDING_APPROVAL':
        return 'Pending approval';

      case 'ACTIVE':
        return 'Active';

      case 'REJECTED':
        return 'Rejected';

      case 'SUSPENDED':
        return 'Suspended';

      case 'INACTIVE':
        return 'Inactive';

      default:
        return status;
    }
  }

  getWebsiteUrl(
    website: string | null
  ): string | null {

    const normalizedWebsite =
      website?.trim();

    if (!normalizedWebsite) {
      return null;
    }

    if (
      normalizedWebsite.startsWith('http://')
      || normalizedWebsite.startsWith('https://')
    ) {
      return normalizedWebsite;
    }

    return `https://${normalizedWebsite}`;
  }

  private createOrganization(): void {

    const request =
      this.buildCreateRequest();

    this.organizationState
      .createMyOrganization(
        request,
        organization => {

          this.populateForm(
            organization
          );

          this.editing.set(
            false
          );

          this.notificationMessage.showSuccess(
            'Payer organization application '
            + 'submitted for approval successfully.'
          );
        },
        message => {

          this.notificationMessage.showError(
            message
          );
        }
      );
  }

  private updateOrganization(): void {

    const wasRejected =
      this.organizationState
        .organizationIsRejected();

    const request =
      this.buildUpdateRequest();

    this.organizationState
      .updateMyOrganization(
        request,
        organization => {

          this.populateForm(
            organization
          );

          this.editing.set(
            false
          );

          if (wasRejected) {

            this.notificationMessage.showSuccess(
              'Payer organization application '
              + 'resubmitted for approval successfully.'
            );

          } else {

            this.notificationMessage.showSuccess(
              'Payer organization updated successfully.'
            );
          }
        },
        message => {

          this.notificationMessage.showError(
            message
          );
        }
      );
  }

  private prepareCreationForm(): void {

    this.organizationForm.enable({
      emitEvent: false
    });

    this.organizationForm.reset({
      organizationName: '',
      registrationNumber: '',
      contactNumber: '',
      website: '',
      addressLine: '',
      city: '',
      state: '',
      postalCode: '',
      country: 'India'
    });

    this.organizationForm.markAsPristine();
    this.organizationForm.markAsUntouched();

    this.editing.set(
      false
    );

    this.organizationState.clearError();
  }

  private populateForm(
    organization: PayerOrganization
  ): void {

    this.organizationForm.enable({
      emitEvent: false
    });

    this.organizationForm.setValue({
      organizationName:
        organization.organizationName,

      registrationNumber:
        organization.registrationNumber,

      contactNumber:
        organization.contactNumber,

      website:
        organization.website ?? '',

      addressLine:
        organization.addressLine,

      city:
        organization.city,

      state:
        organization.state,

      postalCode:
        organization.postalCode,

      country:
        organization.country
    });

    this.organizationForm.controls
      .registrationNumber
      .disable({
        emitEvent: false
      });

    this.organizationForm.markAsPristine();
    this.organizationForm.markAsUntouched();
  }

  private buildCreateRequest():
    CreatePayerOrganizationRequest {

    const formValue =
      this.organizationForm.getRawValue();

    const website =
      formValue.website.trim();

    return {
      organizationName:
        formValue.organizationName.trim(),

      registrationNumber:
        formValue.registrationNumber
          .trim(),

      contactNumber:
        formValue.contactNumber.trim(),

      website:
        website.length > 0
          ? website
          : null,

      addressLine:
        formValue.addressLine.trim(),

      city:
        formValue.city.trim(),

      state:
        formValue.state.trim(),

      postalCode:
        formValue.postalCode.trim(),

      country:
        formValue.country.trim()
    };
  }

  private buildUpdateRequest():
    UpdatePayerOrganizationRequest {

    const formValue =
      this.organizationForm.getRawValue();

    const website =
      formValue.website.trim();

    return {
      organizationName:
        formValue.organizationName.trim(),

      contactNumber:
        formValue.contactNumber.trim(),

      website:
        website.length > 0
          ? website
          : null,

      addressLine:
        formValue.addressLine.trim(),

      city:
        formValue.city.trim(),

      state:
        formValue.state.trim(),

      postalCode:
        formValue.postalCode.trim(),

      country:
        formValue.country.trim()
    };
  }
}