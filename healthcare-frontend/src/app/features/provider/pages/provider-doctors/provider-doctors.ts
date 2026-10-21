import {
  Component,
  OnInit,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  CurrencyPipe
} from '@angular/common';

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
  Doctor
} from '../../models/doctor';

import {
  DoctorStatus
} from '../../models/doctor-status';

import {
  DoctorManagementStateService
} from '../../services/doctor-management-state.service';

type DoctorStatusFilter =
  | 'ALL'
  | DoctorStatus;

type DoctorSortOption =
  | 'NAME_ASCENDING'
  | 'EXPERIENCE_DESCENDING'
  | 'FEE_ASCENDING'
  | 'FEE_DESCENDING';

@Component({
  selector: 'app-provider-doctors',
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
  templateUrl: './provider-doctors.html',
  styleUrl: './provider-doctors.scss'
})
export class ProviderDoctors
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly doctorState =
    inject(DoctorManagementStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<DoctorStatusFilter>('ALL');

  readonly selectedDepartmentId =
    signal('ALL');

  readonly selectedSpecialization =
    signal('ALL');

  readonly selectedSort =
    signal<DoctorSortOption>(
      'NAME_ASCENDING'
    );

  readonly departmentOptions =
    computed(() => {

      const departments =
        this.doctorState
          .doctors()
          .map(doctor => {
            return {
              id: doctor.departmentId,
              name: doctor.departmentName
            };
          });

      const uniqueDepartments =
        new Map<string, string>();

      departments.forEach(department => {
        uniqueDepartments.set(
          department.id,
          department.name
        );
      });

      return Array.from(
        uniqueDepartments.entries()
      )
        .map(([id, name]) => {
          return {
            id,
            name
          };
        })
        .sort(
          (firstDepartment, secondDepartment) => {

            return firstDepartment.name
              .localeCompare(
                secondDepartment.name
              );
          }
        );
    });

  readonly specializationOptions =
    computed(() => {

      const specializations =
        this.doctorState
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

          return firstSpecialization
            .localeCompare(
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

      const status =
        this.selectedStatus();

      const departmentId =
        this.selectedDepartmentId();

      const specialization =
        this.selectedSpecialization();

      const sort =
        this.selectedSort();

      const filtered =
        this.doctorState
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
                .includes(search)
              || doctor.medicalRegistrationNumber
                .toLowerCase()
                .includes(search);

            const matchesStatus =
              status === 'ALL'
              || doctor.status === status;

            const matchesDepartment =
              departmentId === 'ALL'
              || doctor.departmentId
                === departmentId;

            const matchesSpecialization =
              specialization === 'ALL'
              || doctor.specialization
                === specialization;

            return matchesSearch
              && matchesStatus
              && matchesDepartment
              && matchesSpecialization;
          });

      return [...filtered].sort(
        (
          firstDoctor,
          secondDoctor
        ) => {

          switch (sort) {

            case 'EXPERIENCE_DESCENDING':
              return secondDoctor.yearsOfExperience
                - firstDoctor.yearsOfExperience;

            case 'FEE_ASCENDING':
              return firstDoctor.consultationFee
                - secondDoctor.consultationFee;

            case 'FEE_DESCENDING':
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
        || this.selectedStatus() !== 'ALL'
        || this.selectedDepartmentId() !== 'ALL'
        || this.selectedSpecialization() !== 'ALL'
        || this.selectedSort() !== 'NAME_ASCENDING';
    });

  ngOnInit(): void {

    this.loadDoctors();
  }

  loadDoctors(): void {

    this.doctorState.loadMyDoctors();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: DoctorStatusFilter
  ): void {

    this.selectedStatus.set(status);
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
    this.selectedStatus.set('ALL');
    this.selectedDepartmentId.set('ALL');
    this.selectedSpecialization.set('ALL');
    this.selectedSort.set(
      'NAME_ASCENDING'
    );
  }

  createDoctor(): void {

    this.router.navigate([
      '/provider/doctors/new'
    ]);
  }

  viewDoctor(
    doctor: Doctor
  ): void {

    this.router.navigate([
      '/provider/doctors',
      doctor.id
    ]);
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
      return '1 year';
    }

    return yearsOfExperience + ' years';
  }
}