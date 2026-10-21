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
  Department
} from '../../models/department';

import {
  DepartmentStatus
} from '../../models/department-status';

import {
  DepartmentManagementStateService
} from '../../services/department-management-state.service';

type DepartmentStatusFilter =
  | 'ALL'
  | DepartmentStatus;

type DepartmentSortOption =
  | 'NAME_ASCENDING'
  | 'NAME_DESCENDING'
  | 'NEWEST';

@Component({
  selector: 'app-provider-departments',
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
  templateUrl: './provider-departments.html',
  styleUrl: './provider-departments.scss'
})
export class ProviderDepartments
  implements OnInit {

  private readonly router =
    inject(Router);

  readonly departmentState =
    inject(DepartmentManagementStateService);

  readonly searchText =
    signal('');

  readonly selectedStatus =
    signal<DepartmentStatusFilter>('ALL');

  readonly selectedSort =
    signal<DepartmentSortOption>(
      'NAME_ASCENDING'
    );

  readonly filteredDepartments =
    computed(() => {

      const search =
        this.searchText()
          .trim()
          .toLowerCase();

      const status =
        this.selectedStatus();

      const sort =
        this.selectedSort();

      const filtered =
        this.departmentState
          .departments()
          .filter(department => {

            const matchesSearch =
              search.length === 0
              || department.departmentName
                .toLowerCase()
                .includes(search)
              || department.departmentCode
                .toLowerCase()
                .includes(search)
              || department.description
                .toLowerCase()
                .includes(search)
              || (
                department.location !== null
                && department.location
                  .toLowerCase()
                  .includes(search)
              );

            const matchesStatus =
              status === 'ALL'
              || department.status === status;

            return matchesSearch
              && matchesStatus;
          });

      return [...filtered].sort(
        (
          firstDepartment,
          secondDepartment
        ) => {

          switch (sort) {

            case 'NAME_DESCENDING':
              return secondDepartment.departmentName
                .localeCompare(
                  firstDepartment.departmentName
                );

            case 'NEWEST':
              return this.getDateValue(
                secondDepartment.createdAt
              )
                - this.getDateValue(
                  firstDepartment.createdAt
                );

            case 'NAME_ASCENDING':
            default:
              return firstDepartment.departmentName
                .localeCompare(
                  secondDepartment.departmentName
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
        || this.selectedSort() !== 'NAME_ASCENDING';
    });

  ngOnInit(): void {

    this.loadDepartments();
  }

  loadDepartments(): void {

    this.departmentState.loadMyDepartments();
  }

  updateSearch(
    value: string
  ): void {

    this.searchText.set(value);
  }

  updateStatus(
    status: DepartmentStatusFilter
  ): void {

    this.selectedStatus.set(status);
  }

  updateSort(
    sort: DepartmentSortOption
  ): void {

    this.selectedSort.set(sort);
  }

  clearFilters(): void {

    this.searchText.set('');
    this.selectedStatus.set('ALL');
    this.selectedSort.set(
      'NAME_ASCENDING'
    );
  }

  createDepartment(): void {

    this.router.navigate([
      '/provider/departments/new'
    ]);
  }

  viewDepartment(
    department: Department
  ): void {

    this.router.navigate([
      '/provider/departments',
      department.id
    ]);
  }

  getStatusClass(
    status: DepartmentStatus
  ): string {

    if (status === 'ACTIVE') {
      return 'status-badge--active';
    }

    return 'status-badge--inactive';
  }

  getStatusIcon(
    status: DepartmentStatus
  ): string {

    if (status === 'ACTIVE') {
      return 'check_circle';
    }

    return 'block';
  }

  private getDateValue(
    dateValue: string
  ): number {

    const timestamp =
      new Date(dateValue).getTime();

    if (Number.isNaN(timestamp)) {
      return 0;
    }

    return timestamp;
  }
}