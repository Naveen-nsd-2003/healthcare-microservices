import {
  Component,
  OnDestroy,
  OnInit,
  inject
} from '@angular/core';

import {
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
  DepartmentStatus
} from '../../models/department-status';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

@Component({
  selector: 'app-provider-department-details',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './provider-department-details.html',
  styleUrl: './provider-department-details.scss'
})
export class ProviderDepartmentDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  private readonly notificationService =
    inject(NotificationMessageService);

  readonly departmentState =
    inject(DepartmentManagementStateService);

  private departmentId: string | null =
    null;

  ngOnInit(): void {

    this.departmentId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('departmentId');

    if (!this.departmentId) {

      this.notificationService.showError(
        'Department identifier is missing.'
      );

      this.goBack();

      return;
    }

    this.loadDepartment();
  }

  ngOnDestroy(): void {

    this.departmentState
      .clearSelectedDepartment();
  }

  loadDepartment(): void {

    if (!this.departmentId) {
      return;
    }

    this.departmentState
      .loadMyDepartmentById(
        this.departmentId
      );
  }

  goBack(): void {

    this.router.navigate([
      '/provider/departments'
    ]);
  }

  editDepartment(): void {

    const department =
      this.departmentState
        .selectedDepartment();

    if (!department) {

      this.notificationService.showError(
        'Department information is unavailable.'
      );

      return;
    }

    this.router.navigate([
      '/provider/departments',
      department.id,
      'edit'
    ]);
  }

  activateDepartment(): void {

    this.updateDepartmentStatus(
      'ACTIVE'
    );
  }

  deactivateDepartment(): void {

    this.updateDepartmentStatus(
      'INACTIVE'
    );
  }

  getStatusIcon(
    status: DepartmentStatus
  ): string {

    if (status === 'ACTIVE') {
      return 'check_circle';
    }

    return 'block';
  }

  private updateDepartmentStatus(
    status: DepartmentStatus
  ): void {

    const department =
      this.departmentState
        .selectedDepartment();

    if (!department) {

      this.notificationService.showError(
        'Department information is unavailable.'
      );

      return;
    }

    if (
      department.status === status
      || this.departmentState.statusUpdating()
    ) {
      return;
    }

    this.departmentState
      .updateMyDepartmentStatus(
        department.id,
        status,
        updatedDepartment => {

          if (
            updatedDepartment.status
            === 'ACTIVE'
          ) {

            this.notificationService.showSuccess(
              'Department activated successfully.'
            );
          } else {

            this.notificationService.showSuccess(
              'Department made inactive successfully.'
            );
          }
        },
        message => {

          this.notificationService.showError(
            message
          );
        }
      );
  }
}
