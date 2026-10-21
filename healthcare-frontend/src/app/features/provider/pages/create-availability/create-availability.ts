import {
  Component,
  OnInit,
  computed,
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
  CreateDoctorAvailabilityRequest
} from '../../models/create-doctor-availability-request';

import {
  DayOfWeek
} from '../../models/day-of-week';

import {
  AvailabilityManagementStateService
} from '../../services/availability-management-state.service';

import {
  DoctorManagementStateService
} from '../../services/doctor-management-state.service';

@Component({
  selector: 'app-create-availability',
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
  templateUrl: './create-availability.html',
  styleUrl: './create-availability.scss'
})
export class CreateAvailability
  implements OnInit {

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly availabilityState =
    inject(AvailabilityManagementStateService);

  readonly doctorState =
    inject(DoctorManagementStateService);

  private requestedDoctorId: string | null =
    null;

  readonly activeDoctors =
    computed(() => {

      return this.doctorState
        .doctors()
        .filter(doctor => {
          return doctor.status === 'ACTIVE';
        })
        .sort(
          (firstDoctor, secondDoctor) => {

            const departmentDifference =
              firstDoctor.departmentName.localeCompare(
                secondDoctor.departmentName
              );

            if (departmentDifference !== 0) {
              return departmentDifference;
            }

            return firstDoctor.fullName.localeCompare(
              secondDoctor.fullName
            );
          }
        );
    });

  readonly doctorPreselected =
    computed(() => {

      return this.requestedDoctorId !== null;
    });

  readonly dayOptions:
    {
      value: DayOfWeek;
      label: string;
    }[] = [
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

  readonly slotDurationOptions:
    number[] = [
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
      doctorId: [
        '',
        [
          Validators.required
        ]
      ],

      dayOfWeek: [
        'MONDAY' as DayOfWeek,
        [
          Validators.required
        ]
      ],

      startTime: [
        '09:00',
        [
          Validators.required
        ]
      ],

      endTime: [
        '17:00',
        [
          Validators.required
        ]
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

    this.requestedDoctorId =
      this.activatedRoute
        .snapshot
        .queryParamMap
        .get('doctorId');

    if (this.requestedDoctorId) {

      this.availabilityForm.controls
        .doctorId
        .setValue(
          this.requestedDoctorId
        );
    }

    this.doctorState.loadMyDoctors();
  }

  goBack(): void {

    const doctorId =
      this.availabilityForm.controls
        .doctorId.value;

    if (doctorId) {

      this.router.navigate(
        [
          '/provider/availability'
        ],
        {
          queryParams: {
            doctorId
          }
        }
      );

      return;
    }

    this.router.navigate([
      '/provider/availability'
    ]);
  }

  goToDoctors(): void {

    this.router.navigate([
      '/provider/doctors'
    ]);
  }

  reloadDoctors(): void {

    this.doctorState.loadMyDoctors();
  }

  createAvailability(): void {

    this.availabilityState.clearError();

    if (this.availabilityForm.invalid) {

      this.availabilityForm.markAllAsTouched();

      this.notificationService.showError(
        'Please review the highlighted availability fields.'
      );

      return;
    }

    const formValue =
      this.availabilityForm.getRawValue();

    if (
      !this.isEndTimeAfterStartTime(
        formValue.startTime,
        formValue.endTime
      )
    ) {

      this.notificationService.showError(
        'End time must be later than start time.'
      );

      return;
    }

    const scheduleMinutes =
      this.getScheduleDurationMinutes(
        formValue.startTime,
        formValue.endTime
      );

    if (
      formValue.slotDurationMinutes
      > scheduleMinutes
    ) {

      this.notificationService.showError(
        'Slot duration must fit within the selected schedule.'
      );

      return;
    }

    if (
      this.availabilityState.creating()
    ) {
      return;
    }

    const request:
      CreateDoctorAvailabilityRequest = {
        doctorId:
          formValue.doctorId,

        dayOfWeek:
          formValue.dayOfWeek,

        startTime:
          formValue.startTime,

        endTime:
          formValue.endTime,

        slotDurationMinutes:
          Number(
            formValue.slotDurationMinutes
          )
      };

    this.availabilityState
      .createAvailability(
        request,
        availability => {

          this.notificationService.showSuccess(
            'Doctor availability created successfully.'
          );

          this.router.navigate(
            [
              '/provider/availability'
            ],
            {
              queryParams: {
                doctorId:
                  availability.doctorId
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

  getSelectedDoctorName(): string {

    const selectedDoctorId =
      this.availabilityForm.controls
        .doctorId.value;

    const selectedDoctor =
      this.activeDoctors()
        .find(doctor => {
          return doctor.id
            === selectedDoctorId;
        });

    return selectedDoctor?.fullName
      ?? 'No doctor selected';
  }

  getSelectedDoctorDepartment(): string {

    const selectedDoctorId =
      this.availabilityForm.controls
        .doctorId.value;

    const selectedDoctor =
      this.activeDoctors()
        .find(doctor => {
          return doctor.id
            === selectedDoctorId;
        });

    return selectedDoctor?.departmentName
      ?? 'Department unavailable';
  }

  getScheduleDurationLabel(): string {

    const startTime =
      this.availabilityForm.controls
        .startTime.value;

    const endTime =
      this.availabilityForm.controls
        .endTime.value;

    if (
      !this.isEndTimeAfterStartTime(
        startTime,
        endTime
      )
    ) {
      return 'Invalid time range';
    }

    const durationMinutes =
      this.getScheduleDurationMinutes(
        startTime,
        endTime
      );

    const hours =
      Math.floor(
        durationMinutes / 60
      );

    const minutes =
      durationMinutes % 60;

    if (hours === 0) {
      return minutes + ' minutes';
    }

    if (minutes === 0) {

      return hours === 1
        ? '1 hour'
        : hours + ' hours';
    }

    return hours
      + ' hour(s) '
      + minutes
      + ' minutes';
  }

  getEstimatedSlotCount(): number {

    const startTime =
      this.availabilityForm.controls
        .startTime.value;

    const endTime =
      this.availabilityForm.controls
        .endTime.value;

    const slotDuration =
      Number(
        this.availabilityForm.controls
          .slotDurationMinutes.value
      );

    if (
      !this.isEndTimeAfterStartTime(
        startTime,
        endTime
      )
      || slotDuration < 10
      || slotDuration > 120
    ) {
      return 0;
    }

    const durationMinutes =
      this.getScheduleDurationMinutes(
        startTime,
        endTime
      );

    return Math.floor(
      durationMinutes / slotDuration
    );
  }

  private isEndTimeAfterStartTime(
    startTime: string,
    endTime: string
  ): boolean {

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
    ) {
      return false;
    }

    return endMinutes > startMinutes;
  }

  private getScheduleDurationMinutes(
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

    const timeParts =
      timeValue.split(':');

    if (timeParts.length < 2) {
      return null;
    }

    const hours =
      Number(timeParts[0]);

    const minutes =
      Number(timeParts[1]);

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

    return (
      hours * 60
      + minutes
    );
  }
}