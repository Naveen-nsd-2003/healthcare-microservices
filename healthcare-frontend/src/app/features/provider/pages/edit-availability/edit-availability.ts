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
  DayOfWeek
} from '../../models/day-of-week';

import {
  DoctorAvailability
} from '../../models/doctor-availability';

import {
  UpdateDoctorAvailabilityRequest
} from '../../models/update-doctor-availability-request';

import {
  AvailabilityManagementStateService
} from '../../services/availability-management-state.service';

interface AvailabilityDayOption {
  value: DayOfWeek;
  label: string;
}

@Component({
  selector: 'app-edit-availability',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './edit-availability.html',
  styleUrl: './edit-availability.scss'
})
export class EditAvailability
  implements OnInit, OnDestroy {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly route =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly availabilityState =
    inject(AvailabilityManagementStateService);

  private availabilityId: string | null =
    null;

  readonly dayOptions:
    AvailabilityDayOption[] = [
      {
        value: 'MONDAY',
        label: 'Monday'
      },
      {
        value: 'TUESDAY',
        label: 'Tuesday'
      },
      {
        value: 'WEDNESDAY',
        label: 'Wednesday'
      },
      {
        value: 'THURSDAY',
        label: 'Thursday'
      },
      {
        value: 'FRIDAY',
        label: 'Friday'
      },
      {
        value: 'SATURDAY',
        label: 'Saturday'
      },
      {
        value: 'SUNDAY',
        label: 'Sunday'
      }
    ];

  readonly slotDurationOptions: number[] = [
    10,
    15,
    20,
    30,
    45,
    60,
    90,
    120
  ];

  readonly availabilityForm =
    this.formBuilder.nonNullable.group({
      dayOfWeek: [
        'MONDAY' as DayOfWeek,
        Validators.required
      ],

      startTime: [
        '',
        Validators.required
      ],

      endTime: [
        '',
        Validators.required
      ],

      slotDurationMinutes: [
        30,
        [
          Validators.required,
          Validators.min(10),
          Validators.max(120)
        ]
      ]
    });

  ngOnInit(): void {
    this.availabilityId =
      this.route.snapshot.paramMap.get(
        'availabilityId'
      );

    if (!this.availabilityId) {
      this.notificationService.showError(
        'Availability identifier is missing.'
      );

      this.router.navigate([
        '/provider/availability'
      ]);

      return;
    }

    this.loadAvailability();
  }

  ngOnDestroy(): void {
    this.availabilityState
      .clearSelectedAvailability();
  }

  loadAvailability(): void {
    if (!this.availabilityId) {
      return;
    }

    this.availabilityState.clearError();

    this.availabilityState.loadMyAvailabilityById(
      this.availabilityId,
      availability => {
        this.setFormValues(availability);
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  goBackToAvailability(): void {
    const availability =
      this.availabilityState
        .selectedAvailability();

    if (!availability) {
      this.router.navigate([
        '/provider/availability'
      ]);

      return;
    }

    this.router.navigate(
      [
        '/provider/availability'
      ],
      {
        queryParams: {
          doctorId: availability.doctorId
        }
      }
    );
  }

  updateAvailability(): void {
    this.availabilityState.clearError();

    if (this.availabilityForm.invalid) {
      this.availabilityForm.markAllAsTouched();

      this.notificationService.showError(
        'Please review the highlighted availability fields.'
      );

      return;
    }

    if (this.availabilityState.updating()) {
      return;
    }

    const availability =
      this.availabilityState
        .selectedAvailability();

    if (!availability) {
      this.notificationService.showError(
        'Availability information is unavailable.'
      );

      return;
    }

    const formValue =
      this.availabilityForm.getRawValue();

    const startTime =
      formValue.startTime.trim();

    const endTime =
      formValue.endTime.trim();

    const slotDurationMinutes =
      Number(
        formValue.slotDurationMinutes
      );

    const scheduleDurationMinutes =
      this.calculateDurationMinutes(
        startTime,
        endTime
      );

    if (scheduleDurationMinutes <= 0) {
      this.notificationService.showError(
        'End time must be later than start time.'
      );

      return;
    }

    if (
      slotDurationMinutes
      > scheduleDurationMinutes
    ) {
      this.notificationService.showError(
        'Slot duration must fit within the selected schedule.'
      );

      return;
    }

    const request:
      UpdateDoctorAvailabilityRequest = {
        dayOfWeek:
          formValue.dayOfWeek,

        startTime,

        endTime,

        slotDurationMinutes
      };

    this.availabilityState.updateMyAvailability(
      availability.id,
      request,
      updatedAvailability => {
        this.notificationService.showSuccess(
          'Doctor availability updated successfully.'
        );

        this.router.navigate(
          [
            '/provider/availability'
          ],
          {
            queryParams: {
              doctorId:
                updatedAvailability.doctorId
            }
          }
        );
      },
      message => {
        this.notificationService.showError(
          message
        );
      }
    );
  }

  getScheduleDurationLabel(): string {
    const formValue =
      this.availabilityForm.getRawValue();

    const totalMinutes =
      this.calculateDurationMinutes(
        formValue.startTime,
        formValue.endTime
      );

    if (totalMinutes <= 0) {
      return 'Invalid time range';
    }

    const hours =
      Math.floor(totalMinutes / 60);

    const minutes =
      totalMinutes % 60;

    if (hours === 0) {
      return `${minutes} minutes`;
    }

    if (minutes === 0) {
      return hours === 1
        ? '1 hour'
        : `${hours} hours`;
    }

    return `${hours} hour(s) ${minutes} minutes`;
  }

  getEstimatedSlotCount(): number {
    const formValue =
      this.availabilityForm.getRawValue();

    const scheduleDurationMinutes =
      this.calculateDurationMinutes(
        formValue.startTime,
        formValue.endTime
      );

    const slotDurationMinutes =
      Number(
        formValue.slotDurationMinutes
      );

    if (
      scheduleDurationMinutes <= 0
      || slotDurationMinutes < 10
      || slotDurationMinutes > 120
      || slotDurationMinutes
        > scheduleDurationMinutes
    ) {
      return 0;
    }

    return Math.floor(
      scheduleDurationMinutes
      / slotDurationMinutes
    );
  }

  private setFormValues(
    availability: DoctorAvailability
  ): void {
    this.availabilityForm.setValue({
      dayOfWeek:
        availability.dayOfWeek,

      startTime:
        this.normalizeTime(
          availability.startTime
        ),

      endTime:
        this.normalizeTime(
          availability.endTime
        ),

      slotDurationMinutes:
        availability.slotDurationMinutes
    });

    this.availabilityForm.markAsPristine();
    this.availabilityForm.markAsUntouched();
  }

  private normalizeTime(
    timeValue: string
  ): string {
    const parts =
      timeValue.split(':');

    if (parts.length < 2) {
      return '';
    }

    const hours =
      parts[0].padStart(2, '0');

    const minutes =
      parts[1].padStart(2, '0');

    return `${hours}:${minutes}`;
  }

  private calculateDurationMinutes(
    startTime: string,
    endTime: string
  ): number {
    const startMinutes =
      this.convertTimeToMinutes(
        startTime
      );

    const endMinutes =
      this.convertTimeToMinutes(
        endTime
      );

    if (
      startMinutes === null
      || endMinutes === null
      || endMinutes <= startMinutes
    ) {
      return 0;
    }

    return endMinutes - startMinutes;
  }

  private convertTimeToMinutes(
    timeValue: string
  ): number | null {
    const parts =
      timeValue.split(':');

    if (parts.length < 2) {
      return null;
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
      return null;
    }

    return hours * 60 + minutes;
  }
}