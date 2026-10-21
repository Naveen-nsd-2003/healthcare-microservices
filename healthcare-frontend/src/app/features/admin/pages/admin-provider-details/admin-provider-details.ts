import {
  Component,
  OnInit,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  ActivatedRoute
} from '@angular/router';

import {
  MatButtonModule
} from '@angular/material/button';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  ProviderOrganization
} from '../../../provider/models/provider-organization';

import {
  AdminProviderOrganizationApiService
} from '../../services/admin-provider-organization-api.service';

import {
  MatIconModule
} from '@angular/material/icon';

@Component({
  selector: 'app-admin-provider-details',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-provider-details.html',
  styleUrl: './admin-provider-details.scss'
})
export class AdminProviderDetails
  implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly api =
    inject(AdminProviderOrganizationApiService);

  readonly loading =
    signal(false);

  readonly provider =
    signal<ProviderOrganization | null>(
      null
    );

  ngOnInit(): void {

    const providerId =
      this.route.snapshot.paramMap.get(
        'providerId'
      );

    if (!providerId) {
      return;
    }

    this.loadProvider(
      providerId
    );
  }

  loadProvider(
    providerId: string
  ): void {

    this.loading.set(true);

    this.api
      .getProviderById(
        providerId
      )
      .subscribe({
        next: provider => {

          this.provider.set(
            provider
          );

          this.loading.set(false);
        },
        error: () => {

          this.loading.set(false);
        }
      });
  }

  approve(): void {

    const organization =
      this.provider();

    if (!organization) {
      return;
    }

    this.api
      .approveProvider(
        organization.id,
        {
          reviewRemarks:
            'Approved by administrator'
        }
      )
      .subscribe({
        next: provider => {
          this.provider.set(
            provider
          );
        }
      });
  }

  reject(): void {

    const organization =
      this.provider();

    if (!organization) {
      return;
    }

    const reason =
      prompt(
        'Enter rejection reason'
      );

    if (!reason) {
      return;
    }

    this.api
      .rejectProvider(
        organization.id,
        {
          rejectionReason: reason,
          reviewRemarks: null
        }
      )
      .subscribe({
        next: provider => {
          this.provider.set(
            provider
          );
        }
      });
  }

  updateStatus(
    status:
      | 'ACTIVE'
      | 'INACTIVE'
      | 'SUSPENDED'
  ): void {

    const organization =
      this.provider();

    if (!organization) {
      return;
    }

    this.api
      .updateStatus(
        organization.id,
        {
          status
        }
      )
      .subscribe({
        next: provider => {
          this.provider.set(
            provider
          );
        }
      });
  }
}