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
  AdminProviderOrganizationState
} from '../../services/admin-provider-organization-state.service';

@Component({
  selector: 'app-admin-providers',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-providers.html',
  styleUrl: './admin-providers.scss'
})
export class AdminProviders
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly state =
    inject(
      AdminProviderOrganizationState
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

    this.loadProviders();
  }

  loadProviders(): void {

    this.state.loadOrganizations();
  }

  openProvider(
    providerId: string
  ): void {

    this.router.navigate([
      '/admin/providers',
      providerId
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