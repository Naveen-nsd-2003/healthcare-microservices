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
  MatDatepickerModule
} from '@angular/material/datepicker';

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
  MatTooltipModule
} from '@angular/material/tooltip';

import {
  provideNativeDateAdapter
} from '@angular/material/core';

import {
  MemberProfile
} from '../../models/member-profile';

import {
  MemberProfileRequest
} from '../../models/member-profile-request';

import {
  MemberProfileStateService
} from '../../services/member-profile-state.service';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

@Component({
  selector: 'app-member-profile',
  imports: [
    DatePipe,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  providers: [
    provideNativeDateAdapter()
  ],
  templateUrl: './member-profile.html',
  styleUrl: './member-profile.scss'
})
export class MemberProfilePage implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  readonly profileState =
    inject(MemberProfileStateService);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  readonly editing =
    signal(false);

  readonly maximumDateOfBirth =
    this.createMaximumDateOfBirth();

  readonly pageTitle =
    computed(
      () => {

        if (this.profileState.profileMissing()) {
          return 'Complete your healthcare profile';
        }

        if (this.editing()) {
          return 'Update your healthcare profile';
        }

        return 'My healthcare profile';
      }
    );

  readonly pageDescription =
    computed(
      () => {

        if (this.profileState.profileMissing()) {

          return 'Complete your personal, contact, '
            + 'address, and emergency information '
            + 'before continuing with healthcare services.';
        }

        if (this.editing()) {

          return 'Review and update your personal, '
            + 'contact, address, and emergency details.';
        }

        return 'Review your personal and healthcare '
          + 'contact information.';
      }
    );

  readonly showProfileForm =
    computed(
      () =>
        this.profileState.profileMissing()
        || this.editing()
    );

  readonly profileForm =
    this.formBuilder.nonNullable.group({
      firstName: [
        '',
        [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z][A-Za-z .'-]*$/
          )
        ]
      ],

      lastName: [
        '',
        [
          Validators.required,
          Validators.maxLength(100),
          Validators.pattern(
            /^[A-Za-z][A-Za-z .'-]*$/
          )
        ]
      ],

      dateOfBirth: [
        null as Date | null,
        [
          Validators.required
        ]
      ],

      phoneNumber: [
        '',
        [
          Validators.required,
          Validators.pattern(
            /^[0-9+()\- ]{10,20}$/
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
        'India',
        [
          Validators.required,
          Validators.maxLength(100)
        ]
      ],

      emergencyContactName: [
        '',
        [
          Validators.required,
          Validators.maxLength(150),
          Validators.pattern(
            /^[A-Za-z][A-Za-z .'-]*$/
          )
        ]
      ],

      emergencyContactNumber: [
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

    this.loadProfile();
  }

  loadProfile(): void {

    this.editing.set(false);

    this.profileState.loadProfile(
      profile => {

        this.populateForm(profile);
      },
      () => {

        this.resetFormForCreation();
      }
    );
  }

  startEditing(): void {

    const profile =
      this.profileState.profile();

    if (!profile) {
      return;
    }

    this.populateForm(profile);
    this.editing.set(true);

    window.scrollTo({
      top: 0,
      behavior: 'smooth'
    });
  }

  cancelEditing(): void {

    const profile =
      this.profileState.profile();

    if (!profile) {
      return;
    }

    this.populateForm(profile);
    this.profileState.clearError();
    this.editing.set(false);
  }

  submitProfile(): void {

    this.profileState.clearError();

    if (this.profileForm.invalid) {

      this.profileForm.markAllAsTouched();

      this.notificationMessage.showError(
        'Please review the highlighted profile fields.'
      );

      return;
    }

    if (this.profileState.submitting()) {
      return;
    }

    const request =
      this.buildProfileRequest();

    if (this.profileState.profileMissing()) {

      this.profileState.createProfile(
        request,
        profile => {

          this.populateForm(profile);
          this.editing.set(false);

          this.notificationMessage.showSuccess(
            'Healthcare profile completed successfully.'
          );
        },
        message => {

          this.notificationMessage.showError(
            message
          );
        }
      );

      return;
    }

    this.profileState.updateProfile(
      request,
      profile => {

        this.populateForm(profile);
        this.editing.set(false);

        this.notificationMessage.showSuccess(
          'Healthcare profile updated successfully.'
        );
      },
      message => {

        this.notificationMessage.showError(
          message
        );
      }
    );
  }

  getStatusClass(
    status: string
  ): string {

    switch (status) {

      case 'ACTIVE':
        return 'status-badge--active';

      case 'SUSPENDED':
        return 'status-badge--suspended';

      case 'INACTIVE':
        return 'status-badge--inactive';

      default:
        return '';
    }
  }

  private populateForm(
    profile: MemberProfile
  ): void {

    this.profileForm.setValue({
      firstName:
        profile.firstName,

      lastName:
        profile.lastName,

      dateOfBirth:
        this.parseLocalDate(
          profile.dateOfBirth
        ),

      phoneNumber:
        profile.phoneNumber,

      addressLine:
        profile.addressLine,

      city:
        profile.city,

      state:
        profile.state,

      postalCode:
        profile.postalCode,

      country:
        profile.country,

      emergencyContactName:
        profile.emergencyContactName,

      emergencyContactNumber:
        profile.emergencyContactNumber
    });

    this.profileForm.markAsPristine();
    this.profileForm.markAsUntouched();
  }

  private resetFormForCreation(): void {

    this.profileForm.reset({
      firstName: '',
      lastName: '',
      dateOfBirth: null,
      phoneNumber: '',
      addressLine: '',
      city: '',
      state: '',
      postalCode: '',
      country: 'India',
      emergencyContactName: '',
      emergencyContactNumber: ''
    });

    this.profileForm.markAsPristine();
    this.profileForm.markAsUntouched();
  }

  private buildProfileRequest():
    MemberProfileRequest {

    const formValue =
      this.profileForm.getRawValue();

    if (!formValue.dateOfBirth) {

      throw new Error(
        'Date of birth is required.'
      );
    }

    return {
      firstName:
        formValue.firstName.trim(),

      lastName:
        formValue.lastName.trim(),

      dateOfBirth:
        this.formatLocalDate(
          formValue.dateOfBirth
        ),

      phoneNumber:
        formValue.phoneNumber.trim(),

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

      emergencyContactName:
        formValue.emergencyContactName.trim(),

      emergencyContactNumber:
        formValue.emergencyContactNumber.trim()
    };
  }

  private createMaximumDateOfBirth():
    Date {

    const yesterday =
      new Date();

    yesterday.setHours(
      0,
      0,
      0,
      0
    );

    yesterday.setDate(
      yesterday.getDate() - 1
    );

    return yesterday;
  }

  private parseLocalDate(
    dateValue: string
  ): Date {

    const dateParts =
      dateValue
        .split('-')
        .map(Number);

    return new Date(
      dateParts[0],
      dateParts[1] - 1,
      dateParts[2]
    );
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