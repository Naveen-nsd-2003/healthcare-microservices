import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
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
  ProviderOrganization
} from '../../models/provider-organization';

import {
  ProviderType
} from '../../models/provider-type';

import {
  ProviderDiscoveryStateService
} from '../../services/provider-discovery-state.service';

type ProviderTypeFilter =
  | 'ALL'
  | ProviderType;

type EmergencyFilter =
  | 'ALL'
  | 'AVAILABLE'
  | 'NOT_AVAILABLE';

type ProviderSortOption =
  | 'NAME_ASCENDING'
  | 'CITY_ASCENDING'
  | 'EMERGENCY_FIRST';

@Component({
  selector: 'app-member-providers',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule
  ],
  templateUrl: './member-providers.html',
  styleUrl: './member-providers.scss'
})
export class MemberProviders
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly providerState =
    inject(ProviderDiscoveryStateService);

  readonly searchText =
    signal('');

  readonly selectedType =
    signal<ProviderTypeFilter>('ALL');

  readonly selectedEmergency =
    signal<EmergencyFilter>('ALL');

  readonly selectedSort =
    signal<ProviderSortOption>(
      'NAME_ASCENDING'
    );

  readonly filteredProviders =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const selectedType =
        this.selectedType();

      const emergencyFilter =
        this.selectedEmergency();

      const selectedSort =
        this.selectedSort();

      const filtered =
        this.providerState
          .providers()
          .filter(provider => {

            const matchesSearch =
              search.length === 0
              || provider.organizationName
                .toLowerCase()
                .includes(search)
              || provider.city
                .toLowerCase()
                .includes(search)
              || provider.state
                .toLowerCase()
                .includes(search)
              || provider.country
                .toLowerCase()
                .includes(search)
              || this.getProviderTypeLabel(
                provider.providerType
              )
                .toLowerCase()
                .includes(search);

            const matchesType =
              selectedType === 'ALL'
              || provider.providerType
                === selectedType;

            const matchesEmergency =
              emergencyFilter === 'ALL'
              || (
                emergencyFilter === 'AVAILABLE'
                && provider.emergencyServicesAvailable
              )
              || (
                emergencyFilter === 'NOT_AVAILABLE'
                && !provider.emergencyServicesAvailable
              );

            return matchesSearch
              && matchesType
              && matchesEmergency;
          });

      return [...filtered].sort(
        (firstProvider, secondProvider) => {

          switch (selectedSort) {

            case 'CITY_ASCENDING': {
              const cityDifference =
                firstProvider.city
                  .localeCompare(
                    secondProvider.city
                  );

              if (cityDifference !== 0) {
                return cityDifference;
              }

              return firstProvider.organizationName
                .localeCompare(
                  secondProvider.organizationName
                );
            }

            case 'EMERGENCY_FIRST': {
              if (
                firstProvider.emergencyServicesAvailable
                !== secondProvider.emergencyServicesAvailable
              ) {
                return firstProvider
                  .emergencyServicesAvailable
                    ? -1
                    : 1;
              }

              return firstProvider.organizationName
                .localeCompare(
                  secondProvider.organizationName
                );
            }

            case 'NAME_ASCENDING':
            default:
              return firstProvider.organizationName
                .localeCompare(
                  secondProvider.organizationName
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
        || this.selectedType() !== 'ALL'
        || this.selectedEmergency() !== 'ALL'
        || this.selectedSort() !== 'NAME_ASCENDING';
    });

  ngOnInit(): void {

    this.loadProviders();
  }

  loadProviders(): void {

    this.providerState
      .loadAvailableProviders();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateProviderType(
    providerType: ProviderTypeFilter
  ): void {

    this.selectedType.set(
      providerType
    );
  }

  updateEmergencyFilter(
    emergencyFilter: EmergencyFilter
  ): void {

    this.selectedEmergency.set(
      emergencyFilter
    );
  }

  updateSort(
    sortOption: ProviderSortOption
  ): void {

    this.selectedSort.set(
      sortOption
    );
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedType.set('ALL');
    this.selectedEmergency.set('ALL');
    this.selectedSort.set(
      'NAME_ASCENDING'
    );
  }

  viewProvider(
    provider: ProviderOrganization
  ): void {

    this.router.navigate([
      '/member/providers',
      provider.id
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

    const trimmedWebsite =
      website?.trim();

    if (!trimmedWebsite) {
      return null;
    }

    if (
      trimmedWebsite.startsWith('http://')
      || trimmedWebsite.startsWith('https://')
    ) {
      return trimmedWebsite;
    }

    return `https://${trimmedWebsite}`;
  }
}