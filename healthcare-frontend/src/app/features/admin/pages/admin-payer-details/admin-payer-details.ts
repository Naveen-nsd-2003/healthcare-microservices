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
  MatIconModule
} from '@angular/material/icon';

import {
  MatProgressSpinnerModule
} from '@angular/material/progress-spinner';

import {
  PayerOrganization
} from '../../../insurance/models/payer-organization';

import {
  AdminPayerOrganizationApiService
} from '../../services/admin-payer-organization-api.service';

@Component({
  selector: 'app-admin-payer-details',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './admin-payer-details.html',
  styleUrl: './admin-payer-details.scss'
})
export class AdminPayerDetails
  implements OnInit {

  private readonly route =
    inject(ActivatedRoute);

  private readonly api =
    inject(AdminPayerOrganizationApiService);

  readonly loading =
    signal(false);

  readonly payer =
    signal<PayerOrganization | null>(
      null
    );

  ngOnInit(): void {

    const payerId =
      this.route.snapshot.paramMap.get(
        'payerId'
      );

    if (!payerId) {
      return;
    }

    this.loadPayer(
      payerId
    );
  }

  loadPayer(
    payerId: string
  ): void {

    this.loading.set(true);

    this.api
      .getPayerById(
        payerId
      )
      .subscribe({
        next: payer => {

          this.payer.set(
            payer
          );

          this.loading.set(
            false
          );
        },
        error: () => {

          this.loading.set(
            false
          );
        }
      });
  }

  approve(): void {

    const organization =
      this.payer();

    if (!organization) {
      return;
    }

    this.api
      .approvePayer(
        organization.id,
        {
          reviewRemarks:
            'Approved by administrator'
        }
      )
      .subscribe({
        next: payer => {

          this.payer.set(
            payer
          );
        }
      });
  }

  reject(): void {

    const organization =
      this.payer();

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
      .rejectPayer(
        organization.id,
        {
          rejectionReason:
            reason,

          reviewRemarks:
            null
        }
      )
      .subscribe({
        next: payer => {

          this.payer.set(
            payer
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
      this.payer();

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
        next: payer => {

          this.payer.set(
            payer
          );
        }
      });
  }
}