import {
  Component,
  HostListener,
  DestroyRef,
  inject,
  signal
} from '@angular/core';

import {
  NavigationEnd,
  Router,
  RouterOutlet
} from '@angular/router';

import {
  MatDialog
} from '@angular/material/dialog';

import {
  filter
} from 'rxjs';

import {
  takeUntilDestroyed
} from '@angular/core/rxjs-interop';

import {
  AuthService
} from '../../core/auth/auth.service';

import {
  NotificationMessageService
} from '../../core/services/notification-message.service';

import {
  NotificationStateService
} from '../../features/notifications/services/notification-state.service';

import {
  ConfirmationDialog
} from '../../shared/dialogs/confirmation-dialog/confirmation-dialog';

import {
  ConfirmationDialogData
} from '../../shared/models/confirmation-dialog-data';

import {
  Sidebar
} from './components/sidebar/sidebar';

import {
  TopToolbar
} from './components/top-toolbar/top-toolbar';

import {
  MemberProfileStateService
} from '../../features/member/services/member-profile-state.service';

import {
  InsurancePlanStateService
} from '../../features/insurance/services/insurance-plan-state.service';

import {
  EnrollmentStateService
} from '../../features/insurance/services/enrollment-state.service';

import {
  PolicyStateService
} from '../../features/insurance/services/policy-state.service';

import {
  PayerOrganizationStateService
} from '../../features/insurance/services/payer-organization-state.service';

import {
  ProviderDiscoveryStateService
} from '../../features/provider/services/provider-discovery-state.service';

import {
  AppointmentStateService
} from '../../features/appointment/services/appointment-state.service';

import {
  DepartmentManagementStateService
} from '../../features/provider/services/department-management-state.service';

import {
  DoctorManagementStateService
} from '../../features/provider/services/doctor-management-state.service';

import {
  AvailabilityManagementStateService
} from '../../features/provider/services/availability-management-state.service';

import {
  ProviderAppointmentStateService
} from '../../features/appointment/services/provider-appointment-state.service';

import {
  MedicalRecordStateService
} from '../../features/medical-records/services/medical-record-state.service';


import {
  ClaimStateService
} from '../../features/claims/services/claim-state.service';

import {
  ProviderOrganizationStateService
} from '../../features/provider/services/provider-organization-state.service';
import { AdminStateService } from '../../features/admin/services/admin-state.service';
@Component({
  selector: 'app-dashboard-layout',
  imports: [
    RouterOutlet,
    Sidebar,
    TopToolbar
  ],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.scss'
})
export class DashboardLayout {

  private readonly destroyRef =
    inject(DestroyRef);

  private readonly dialog =
    inject(MatDialog);

  private readonly router =
    inject(Router);

  private readonly authService =
    inject(AuthService);

  private readonly notificationMessage =
    inject(NotificationMessageService);

  private readonly notificationState =
    inject(NotificationStateService);

  private readonly memberProfileState =
    inject(MemberProfileStateService);

  private readonly policyState =
    inject(PolicyStateService);


  private readonly departmentManagementState =
    inject(DepartmentManagementStateService);

  private readonly insurancePlanState =
    inject(InsurancePlanStateService);
  readonly isMobile =
    signal(
      window.innerWidth <= 960
    );

  private readonly payerOrganizationState =
    inject(PayerOrganizationStateService);
  private readonly availabilityManagementState =
    inject(AvailabilityManagementStateService);


  private readonly enrollmentState =
    inject(EnrollmentStateService);


  private readonly providerDiscoveryState =
    inject(ProviderDiscoveryStateService);

  private readonly appointmentState =
    inject(AppointmentStateService);

  private readonly providerOrganizationState =
    inject(ProviderOrganizationStateService);

  private readonly doctorManagementState =
    inject(DoctorManagementStateService);

  private readonly providerAppointmentState =
    inject(ProviderAppointmentStateService);

  private readonly medicalRecordState =
    inject(MedicalRecordStateService);

  private readonly claimState =
    inject(ClaimStateService);

    private readonly adminState =
    inject(AdminStateService);
       
  readonly sidebarCollapsed =
    signal(false);

  readonly mobileSidebarOpen =
    signal(false);

  constructor() {

    this.router.events
      .pipe(
        filter(
          event =>
            event instanceof NavigationEnd
        ),
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe(
        () => {

          if (
            this.isMobile()
          ) {

            this.mobileSidebarOpen.set(
              false
            );
          }
        }
      );
  }

  @HostListener(
    'window:resize'
  )
  handleWindowResize(): void {

    const mobile =
      window.innerWidth <= 960;

    this.isMobile.set(
      mobile
    );

    if (!mobile) {

      this.mobileSidebarOpen.set(
        false
      );
    }
  }

  toggleSidebar(): void {

    if (
      this.isMobile()
    ) {

      this.mobileSidebarOpen.update(
        open => !open
      );

      return;
    }

    this.sidebarCollapsed.update(
      collapsed => !collapsed
    );
  }

  closeMobileSidebar(): void {

    if (
      !this.isMobile()
    ) {

      return;
    }

    this.mobileSidebarOpen.set(
      false
    );
  }

  handleNavigationSelected(): void {

    this.closeMobileSidebar();
  }

  requestLogout(): void {

    const dialogData:
      ConfirmationDialogData = {

      title: 'Log out?',

      message:
        'You will need to sign in again '
        + 'to access the Healthcare Portal.',

      confirmText: 'Log out',

      cancelText: 'Cancel',

      icon: 'logout',

      type: 'danger'
    };

    const dialogReference =
      this.dialog.open(
        ConfirmationDialog,
        {
          data: dialogData,

          width: '420px',

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
        ),
        takeUntilDestroyed(
          this.destroyRef
        )
      )
      .subscribe(
        () => {

          this.notificationState
            .clearState();

          this.memberProfileState
            .clearState();

          this.insurancePlanState
            .clearState();
          this.payerOrganizationState
            .clearState();

          this.appointmentState
            .clearState();

          this.policyState
            .clearState();

          this.providerDiscoveryState
            .clearState();
          this.departmentManagementState
            .clearState();
          this.enrollmentState
            .clearState();
          this.providerOrganizationState
            .clearState();
          this.providerAppointmentState
            .clearState();
          this.availabilityManagementState
            .clearState();
          this.doctorManagementState
            .clearState();
          this.authService
            .logout();

          this.medicalRecordState
            .clearState();

          this.claimState
            .clearState();

            this.adminState
            .clearState();

          this.notificationMessage
            .showSuccess(
              'Logged out successfully.'
            );
        }
      );
  }
}