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
  MatCheckboxModule
} from '@angular/material/checkbox';

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
  CreateProviderOrganizationRequest
} from '../../models/create-provider-organization-request';

import {
  ProviderOrganization
} from '../../models/provider-organization';

import {
  ProviderStatus
} from '../../models/provider-status';

import {
  ProviderType
} from '../../models/provider-type';

import {
  UpdateProviderOrganizationRequest
} from '../../models/update-provider-organization-request';

import {
  ProviderOrganizationStateService
} from '../../services/provider-organization-state.service';

@Component({
  selector: 'app-provider-organization',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './provider-organization.html',
  styleUrl: './provider-organization.scss'
})
export class ProviderOrganizationPage
  implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly organizationState =
    inject(ProviderOrganizationStateService);

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
        return 'Submit Provider organization';
      }

      if (this.correctingRejectedApplication()) {
        return 'Correct and resubmit organization';
      }

      return 'Update Provider organization';
    });

  readonly formDescription =
    computed(() => {

      if (this.creating()) {

        return 'Provide your registration, licensing, '
          + 'contact, address, and service information '
          + 'for Administrator review.';
      }

      if (this.correctingRejectedApplication()) {

        return 'Correct the organization information '
          + 'identified during review. Saving the changes '
          + 'will resubmit the application for approval.';
      }

      return 'Update your organization name, Provider type, '
        + 'contact information, registered address, and '
        + 'emergency-service availability.';
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

  readonly providerTypes: {
    value: ProviderType;
    label: string;
  }[] = [
    {
      value: 'HOSPITAL',
      label: 'Hospital'
    },
    {
      value: 'CLINIC',
      label: 'Clinic'
    },
    {
      value: 'DIAGNOSTIC_CENTER',
      label: 'Diagnostic Center'
    },
    {
      value: 'SPECIALTY_CENTER',
      label: 'Specialty Center'
    },
    {
      value: 'REHABILITATION_CENTER',
      label: 'Rehabilitation Center'
    },
    {
      value: 'TELEMEDICINE_PROVIDER',
      label: 'Telemedicine Provider'
    }
  ];

  readonly organizationForm =
    this.formBuilder.nonNullable.group({
      organizationName: [
        '',
        [
          Validators.required,
          Validators.maxLength(200)
        ]
      ],

      providerType: [
        'HOSPITAL' as ProviderType,
        [
          Validators.required
        ]
      ],

      registrationNumber: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z0-9./_-]+$/
          )
        ]
      ],

      licenseNumber: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z0-9./_-]+$/
          )
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
          Validators.maxLength(255),
          Validators.pattern(
            /^(https?:\/\/)?([A-Za-z0-9-]+\.)+[A-Za-z]{2,}(:[0-9]{1,5})?(\/.*)?$/
          )
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
      ],

      emergencyServicesAvailable: [
        false
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
      .loadMyProviderOrganization(
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
        + 'Provider organization fields.'
      );

      return;
    }

    if (this.creating()) {

      this.createOrganization();

      return;
    }

    this.updateOrganization();
  }

  getProviderTypeLabel(
    providerType: ProviderType
  ): string {

    const providerTypeOption =
      this.providerTypes.find(option => {
        return option.value === providerType;
      });

    return providerTypeOption?.label
      ?? providerType;
  }

  getStatusClass(
    status: ProviderStatus
  ): string {

    switch (status) {

      case 'PENDING_APPROVAL':
        return 'status-badge--pending';

      case 'ACTIVE':
        return 'status-badge--active';

      case 'REJECTED':
        return 'status-badge--rejected';

      case 'INACTIVE':
        return 'status-badge--inactive';

      case 'SUSPENDED':
        return 'status-badge--suspended';

      default:
        return '';
    }
  }

  getStatusLabel(
    status: ProviderStatus
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
      .createMyProviderOrganization(
        request,
        organization => {

          this.populateForm(
            organization
          );

          this.editing.set(
            false
          );

          this.notificationMessage.showSuccess(
            'Provider organization application '
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
      .updateMyProviderOrganization(
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
              'Provider organization application '
              + 'resubmitted for approval successfully.'
            );

          } else {

            this.notificationMessage.showSuccess(
              'Provider organization updated successfully.'
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
      providerType: 'HOSPITAL',
      registrationNumber: '',
      licenseNumber: '',
      contactNumber: '',
      website: '',
      addressLine: '',
      city: '',
      state: '',
      postalCode: '',
      country: 'India',
      emergencyServicesAvailable: false
    });

    this.organizationForm.markAsPristine();
    this.organizationForm.markAsUntouched();

    this.editing.set(
      false
    );

    this.organizationState.clearError();
  }

  private populateForm(
    organization: ProviderOrganization
  ): void {

    this.organizationForm.enable({
      emitEvent: false
    });

    this.organizationForm.setValue({
      organizationName:
        organization.organizationName,

      providerType:
        organization.providerType,

      registrationNumber:
        organization.registrationNumber,

      licenseNumber:
        organization.licenseNumber,

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
        organization.country,

      emergencyServicesAvailable:
        organization.emergencyServicesAvailable
    });

    this.organizationForm.controls
      .registrationNumber
      .disable({
        emitEvent: false
      });

    this.organizationForm.controls
      .licenseNumber
      .disable({
        emitEvent: false
      });

    this.organizationForm.markAsPristine();
    this.organizationForm.markAsUntouched();
  }

  private buildCreateRequest():
    CreateProviderOrganizationRequest {

    const formValue =
      this.organizationForm.getRawValue();

    const website =
      formValue.website.trim();

    return {
      organizationName:
        formValue.organizationName.trim(),

      providerType:
        formValue.providerType,

      registrationNumber:
        formValue.registrationNumber
          .trim()
          .toUpperCase(),

      licenseNumber:
        formValue.licenseNumber
          .trim()
          .toUpperCase(),

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
        formValue.country.trim(),

      emergencyServicesAvailable:
        formValue.emergencyServicesAvailable
    };
  }

  private buildUpdateRequest():
    UpdateProviderOrganizationRequest {

    const formValue =
      this.organizationForm.getRawValue();

    const website =
      formValue.website.trim();

    return {
      organizationName:
        formValue.organizationName.trim(),

      providerType:
        formValue.providerType,

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
        formValue.country.trim(),

      emergencyServicesAvailable:
        formValue.emergencyServicesAvailable
    };
  }
}