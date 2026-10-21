import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
  CurrencyPipe,
  DatePipe
} from '@angular/common';

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
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  NotificationMessageService
} from '../../../../core/services/notification-message.service';

import {
  Doctor
} from '../../models/doctor';

import {
  DoctorStatus
} from '../../models/doctor-status';

import {
  DoctorManagementStateService
} from '../../services/doctor-management-state.service';

@Component({
  selector: 'app-provider-doctor-details',
  standalone: true,
  imports: [
   
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './provider-doctor-details.html',
  styleUrl: './provider-doctor-details.scss'
})
export class ProviderDoctorDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly doctorState =
    inject(DoctorManagementStateService);

  private doctorId: string | null =
    null;

  ngOnInit(): void {

    this.doctorId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('doctorId');

    if (!this.doctorId) {

      this.notificationService.showError(
        'Doctor identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.loadDoctor();
  }

  ngOnDestroy(): void {

    this.doctorState.clearSelectedDoctor();
  }

  loadDoctor(): void {

    if (!this.doctorId) {
      return;
    }

    this.doctorState.loadMyDoctorById(
      this.doctorId
    );
  }

  goBack(): void {

    this.router.navigate([
      '/provider/doctors'
    ]);
  }

  editDoctor(): void {

    const doctor =
      this.doctorState.selectedDoctor();

    if (!doctor) {

      this.notificationService.showError(
        'Doctor information is unavailable.'
      );

      return;
    }

    this.router.navigate([
      '/provider/doctors',
      doctor.id,
      'edit'
    ]);
  }

  manageAvailability(): void {

    const doctor =
      this.doctorState.selectedDoctor();

    if (!doctor) {

      this.notificationService.showError(
        'Doctor information is unavailable.'
      );

      return;
    }

    this.router.navigate(
      [
        '/provider/availability'
      ],
      {
        queryParams: {
          doctorId: doctor.id
        }
      }
    );
  }

  activateDoctor(): void {

    this.updateDoctorStatus(
      'ACTIVE'
    );
  }

  deactivateDoctor(): void {

    this.updateDoctorStatus(
      'INACTIVE'
    );
  }

  getInitials(
    doctor: Doctor
  ): string {

    const firstInitial =
      doctor.firstName
        .trim()
        .charAt(0)
        .toUpperCase();

    const lastInitial =
      doctor.lastName
        .trim()
        .charAt(0)
        .toUpperCase();

    return firstInitial + lastInitial;
  }

  getExperienceLabel(
    yearsOfExperience: number
  ): string {

    if (yearsOfExperience === 0) {
      return 'New practitioner';
    }

    if (yearsOfExperience === 1) {
      return '1 year of experience';
    }

    return yearsOfExperience
      + ' years of experience';
  }

  private updateDoctorStatus(
    status: DoctorStatus
  ): void {

    const doctor =
      this.doctorState.selectedDoctor();

    if (!doctor) {

      this.notificationService.showError(
        'Doctor information is unavailable.'
      );

      return;
    }

    if (
      doctor.status === status
      || this.doctorState.statusUpdating()
    ) {
      return;
    }

    this.doctorState.updateMyDoctorStatus(
      doctor.id,
      status,
      updatedDoctor => {

        if (updatedDoctor.status === 'ACTIVE') {

          this.notificationService.showSuccess(
            'Doctor activated successfully.'
          );

          return;
        }

        this.notificationService.showSuccess(
          'Doctor made inactive successfully.'
        );
      },
      message => {

        this.notificationService.showError(
          message
        );
      }
    );
  }
}