import {
  Component,
  OnDestroy,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  CurrencyPipe
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
  Doctor
} from '../../models/doctor';

import {
  ProviderType
} from '../../models/provider-type';

import {
  ProviderDiscoveryStateService
} from '../../services/provider-discovery-state.service';

type DoctorSortOption =
  | 'NAME_ASCENDING'
  | 'EXPERIENCE_HIGH_TO_LOW'
  | 'FEE_LOW_TO_HIGH'
  | 'FEE_HIGH_TO_LOW';

@Component({
  selector: 'app-member-provider-details',
  standalone: true,
  imports: [
    CurrencyPipe,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './member-provider-details.html',
  styleUrl: './member-provider-details.scss'
})
export class MemberProviderDetails
  implements OnInit, OnDestroy {

  private readonly activatedRoute =
    inject(ActivatedRoute);

  private readonly router =
    inject(Router);

  readonly providerState =
    inject(ProviderDiscoveryStateService);

  readonly searchText =
    signal('');

  readonly selectedDepartmentId =
    signal('ALL');

  readonly selectedSpecialization =
    signal('ALL');

  readonly selectedSort =
    signal<DoctorSortOption>(
      'NAME_ASCENDING'
    );

  readonly specializationOptions =
    computed(() => {

      const specializations =
        this.providerState
          .doctors()
          .map(doctor => {
            return doctor.specialization.trim();
          })
          .filter(specialization => {
            return specialization.length > 0;
          });

      return Array.from(
        new Set(specializations)
      ).sort(
        (
          firstSpecialization,
          secondSpecialization
        ) => {

          return firstSpecialization.localeCompare(
            secondSpecialization
          );
        }
      );
    });

  readonly filteredDoctors =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const departmentId =
        this.selectedDepartmentId();

      const specialization =
        this.selectedSpecialization();

      const sort =
        this.selectedSort();

      const filtered =
        this.providerState
          .doctors()
          .filter(doctor => {

            const matchesSearch =
              search.length === 0
              || doctor.fullName
                .toLowerCase()
                .includes(search)
              || doctor.specialization
                .toLowerCase()
                .includes(search)
              || doctor.qualification
                .toLowerCase()
                .includes(search)
              || doctor.departmentName
                .toLowerCase()
                .includes(search);

            const matchesDepartment =
              departmentId === 'ALL'
              || doctor.departmentId
                === departmentId;

            const matchesSpecialization =
              specialization === 'ALL'
              || doctor.specialization
                === specialization;

            return matchesSearch
              && matchesDepartment
              && matchesSpecialization;
          });

      return [...filtered].sort(
        (
          firstDoctor,
          secondDoctor
        ) => {

          switch (sort) {

            case 'EXPERIENCE_HIGH_TO_LOW':
              return secondDoctor.yearsOfExperience
                - firstDoctor.yearsOfExperience;

            case 'FEE_LOW_TO_HIGH':
              return firstDoctor.consultationFee
                - secondDoctor.consultationFee;

            case 'FEE_HIGH_TO_LOW':
              return secondDoctor.consultationFee
                - firstDoctor.consultationFee;

            case 'NAME_ASCENDING':
            default:
              return firstDoctor.fullName
                .localeCompare(
                  secondDoctor.fullName
                );
          }
        }
      );
    });

  readonly filtersApplied =
    computed(() => {

      return this.searchText()
        .trim()
        .length > 0
        || this.selectedDepartmentId() !== 'ALL'
        || this.selectedSpecialization() !== 'ALL'
        || this.selectedSort() !== 'NAME_ASCENDING';
    });

  ngOnInit(): void {

    const providerId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('providerId');

    if (!providerId) {

      this.goBack();

      return;
    }

    this.providerState.loadProviderDetails(
      providerId
    );
  }

  ngOnDestroy(): void {

    this.providerState.clearProviderDetails();
  }

  goBack(): void {

    this.router.navigate([
      '/member/providers'
    ]);
  }

  retryLoading(): void {

    const providerId =
      this.activatedRoute
        .snapshot
        .paramMap
        .get('providerId');

    if (!providerId) {

      this.goBack();

      return;
    }

    this.providerState.loadProviderDetails(
      providerId
    );
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateDepartment(
    departmentId: string
  ): void {

    this.selectedDepartmentId.set(
      departmentId
    );
  }

  updateSpecialization(
    specialization: string
  ): void {

    this.selectedSpecialization.set(
      specialization
    );
  }

  updateSort(
    sort: DoctorSortOption
  ): void {

    this.selectedSort.set(sort);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedDepartmentId.set('ALL');
    this.selectedSpecialization.set('ALL');
    this.selectedSort.set(
      'NAME_ASCENDING'
    );
  }

  viewDoctor(
    doctor: Doctor
  ): void {

    this.router.navigate([
      '/member/doctors',
      doctor.id
    ]);
  }

  getProviderTypeLabel(
    providerType: ProviderType
  ): string {

    switch (providerType) {

      case 'HOSPITAL':
        return 'Hospital';

      case 'CLINIC':
        return 'Clinic';

      case 'DIAGNOSTIC_CENTER':
        return 'Diagnostic Center';

      case 'SPECIALTY_CENTER':
        return 'Specialty Center';

      case 'REHABILITATION_CENTER':
        return 'Rehabilitation Center';

      case 'TELEMEDICINE_PROVIDER':
        return 'Telemedicine Provider';

      default:
        return providerType;
    }
  }

  getProviderTypeIcon(
    providerType: ProviderType
  ): string {

    switch (providerType) {

      case 'HOSPITAL':
        return 'local_hospital';

      case 'CLINIC':
        return 'medical_services';

      case 'DIAGNOSTIC_CENTER':
        return 'biotech';

      case 'SPECIALTY_CENTER':
        return 'health_and_safety';

      case 'REHABILITATION_CENTER':
        return 'accessible';

      case 'TELEMEDICINE_PROVIDER':
        return 'video_call';

      default:
        return 'business';
    }
  }

  getWebsiteUrl(
    website: string | null
  ): string | null {

    const normalizedWebsite =
      website?.trim();

    if (!normalizedWebsite) {
      return null;
    }

    if (
      normalizedWebsite.startsWith('http://')
      || normalizedWebsite.startsWith('https://')
    ) {
      return normalizedWebsite;
    }

    return `https://${normalizedWebsite}`;
  }

  getExperienceLabel(
    yearsOfExperience: number
  ): string {

    if (yearsOfExperience === 0) {
      return 'New practitioner';
    }

    if (yearsOfExperience === 1) {
      return '1 year experience';
    }

    return `${yearsOfExperience} years experience`;
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
}