import {
  Component,
  OnInit,
  computed,
  inject
} from '@angular/core';

import {
  CommonModule
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
  AdminPayerOrganizationState
} from '../../services/admin-payer-organization-state.service';

@Component({
  selector: 'app-admin-payers',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-payers.html',
  styleUrl: './admin-payers.scss'
})
export class AdminPayers
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly state =
    inject(
      AdminPayerOrganizationState
    );

  readonly pendingCount =
    computed(() => {

      return this.state
        .organizations()
        .filter(
          organization =>
            organization.status
            === 'PENDING_APPROVAL'
        )
        .length;
    });

  ngOnInit(): void {

    this.loadPayers();
  }

  loadPayers(): void {

    this.state.loadOrganizations();
  }

  openPayer(
    payerId: string
  ): void {

    this.router.navigate([
      '/admin/payers',
      payerId
    ]);
  }

  getStatusClass(
    status: string
  ): string {

    switch (status) {

      case 'PENDING_APPROVAL':
        return 'pending';

      case 'ACTIVE':
        return 'active';

      case 'REJECTED':
        return 'rejected';

      case 'SUSPENDED':
        return 'suspended';

      default:
        return 'inactive';
    }
  }
}