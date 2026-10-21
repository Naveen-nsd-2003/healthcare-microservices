import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject
} from '@angular/core';

import {
  DatePipe
} from '@angular/common';

import {
  Router
} from '@angular/router';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  AdminAccountRole,
  AdminAccountStatus,
  AdminAccountSummary
} from '../../models/admin-account-summary';

import {
  AdminStateService
} from '../../services/admin-state.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.scss'
})
export class AdminDashboard
  implements OnInit, OnDestroy {

  private readonly router =
    inject(Router);

  readonly adminState =
    inject(AdminStateService);

  readonly institutionalAccountCount =
    computed(() => {

      const dashboard =
        this.adminState.dashboard();

      if (!dashboard) {
        return 0;
      }

      return dashboard.totalProviders
        + dashboard.totalPayers;
    });

  readonly requiresAttentionCount =
    computed(() => {

      const dashboard =
        this.adminState.dashboard();

      if (!dashboard) {
        return 0;
      }

      return dashboard.inactiveUsers
        + dashboard.blockedUsers
        + dashboard.suspendedUsers
        + dashboard.lockedAccounts;
    });

  readonly accountHealthPercentage =
    computed(() => {

      const dashboard =
        this.adminState.dashboard();

      if (
        !dashboard
        || dashboard.totalUsers <= 0
      ) {
        return 0;
      }

      const percentage =
        (
          dashboard.activeUsers
          / dashboard.totalUsers
        ) * 100;

      return Math.round(
        percentage
      );
    });

  ngOnInit(): void {

    this.loadDashboard();
  }

  ngOnDestroy(): void {

    this.adminState.clearError();
  }

  loadDashboard(): void {

    this.adminState.loadDashboard();
  }

  createProvider(): void {

    this.router.navigate([
      '/admin/providers/create'
    ]);
  }

  createPayer(): void {

    this.router.navigate([
      '/admin/payers/create'
    ]);
  }

  viewProviders(): void {

    this.router.navigate([
      '/admin/providers'
    ]);
  }

  viewPayers(): void {

    this.router.navigate([
      '/admin/payers'
    ]);
  }

  viewNotifications(): void {

    this.router.navigate([
      '/admin/notifications'
    ]);
  }

  getRoleLabel(
    role: AdminAccountRole
  ): string {

    if (role === 'ADMIN') {
      return 'Administrator';
    }

    if (role === 'PROVIDER') {
      return 'Provider';
    }

    if (role === 'PAYER') {
      return 'Payer';
    }

    return 'Member';
  }

  getRoleIcon(
    role: AdminAccountRole
  ): string {

    if (role === 'ADMIN') {
      return 'admin_panel_settings';
    }

    if (role === 'PROVIDER') {
      return 'local_hospital';
    }

    if (role === 'PAYER') {
      return 'account_balance';
    }

    return 'person';
  }

  getStatusLabel(
    status: AdminAccountStatus
  ): string {

    if (status === 'INACTIVE') {
      return 'Inactive';
    }

    if (status === 'BLOCKED') {
      return 'Blocked';
    }

    if (status === 'SUSPENDED') {
      return 'Suspended';
    }

    return 'Active';
  }

  getAccountAccessLabel(
    account: AdminAccountSummary
  ): string {

    if (account.accountLocked) {
      return 'Locked';
    }

    if (account.firstLogin) {
      return 'First login pending';
    }

    return 'Access configured';
  }

  getAccountAccessIcon(
    account: AdminAccountSummary
  ): string {

    if (account.accountLocked) {
      return 'lock';
    }

    if (account.firstLogin) {
      return 'password';
    }

    return 'verified_user';
  }

  getInitials(
    account: AdminAccountSummary
  ): string {

    const firstInitial =
      account.firstName
        ?.trim()
        .charAt(0)
        .toUpperCase()
      ?? '';

    const lastInitial =
      account.lastName
        ?.trim()
        .charAt(0)
        .toUpperCase()
      ?? '';

    const initials =
      `${firstInitial}${lastInitial}`;

    if (initials.length > 0) {
      return initials;
    }

    return account.username
      ?.trim()
      .substring(
        0,
        2
      )
      .toUpperCase()
      || 'US';
  }

  getPhoneNumber(
    phoneNumber: string | null
  ): string {

    const normalizedPhoneNumber =
      phoneNumber?.trim() ?? '';

    return normalizedPhoneNumber.length > 0
      ? normalizedPhoneNumber
      : 'Not provided';
  }
}