import {
    Injectable,
    computed,
    inject,
    signal
  } from '@angular/core';
  
  import {
    HttpErrorResponse
  } from '@angular/common/http';
  
  import {
    finalize
  } from 'rxjs';
  
  import {
    CreateDepartmentRequest
  } from '../models/create-department-request';
  
  import {
    Department
  } from '../models/department';
  
  import {
    DepartmentStatus
  } from '../models/department-status';
  
  import {
    UpdateDepartmentRequest
  } from '../models/update-department-request';
  
  import {
    UpdateDepartmentStatusRequest
  } from '../models/update-department-status-request';
  
  import {
    DepartmentApiService
  } from './department-api.service';
  
  @Injectable({
    providedIn: 'root'
  })
  export class DepartmentManagementStateService {
  
    private readonly departmentApi =
      inject(DepartmentApiService);
  
    private readonly departmentsState =
      signal<Department[]>([]);
  
    private readonly selectedDepartmentState =
      signal<Department | null>(null);
  
    private readonly loadingState =
      signal(false);
  
    private readonly detailsLoadingState =
      signal(false);
  
    private readonly creatingState =
      signal(false);
  
    private readonly updatingState =
      signal(false);
  
    private readonly statusUpdatingState =
      signal(false);
  
    private readonly errorMessageState =
      signal<string | null>(null);
  
    readonly departments =
      this.departmentsState.asReadonly();
  
    readonly selectedDepartment =
      this.selectedDepartmentState.asReadonly();
  
    readonly loading =
      this.loadingState.asReadonly();
  
    readonly detailsLoading =
      this.detailsLoadingState.asReadonly();
  
    readonly creating =
      this.creatingState.asReadonly();
  
    readonly updating =
      this.updatingState.asReadonly();
  
    readonly statusUpdating =
      this.statusUpdatingState.asReadonly();
  
    readonly errorMessage =
      this.errorMessageState.asReadonly();
  
    readonly totalCount =
      computed(() => {
        return this.departmentsState().length;
      });
  
    readonly activeCount =
      computed(() => {
        return this.countByStatus('ACTIVE');
      });
  
    readonly inactiveCount =
      computed(() => {
        return this.countByStatus('INACTIVE');
      });
  
    loadMyDepartments(): void {
  
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.departmentApi
        .getMyDepartments()
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: departments => {
  
            this.departmentsState.set(
              this.sortDepartments(departments)
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.departmentsState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadMyDepartmentById(
      departmentId: string,
      afterSuccess?: (
        department: Department
      ) => void
    ): void {
  
      if (this.detailsLoadingState()) {
        return;
      }
  
      this.detailsLoadingState.set(true);
      this.selectedDepartmentState.set(null);
      this.errorMessageState.set(null);
  
      this.departmentApi
        .getMyDepartmentById(departmentId)
        .pipe(
          finalize(() => {
            this.detailsLoadingState.set(false);
          })
        )
        .subscribe({
          next: department => {
  
            this.selectedDepartmentState.set(
              department
            );
  
            this.updateDepartmentInCollection(
              department
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(department);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.selectedDepartmentState.set(null);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    createDepartment(
      request: CreateDepartmentRequest,
      afterSuccess?: (
        department: Department
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.creatingState()) {
        return;
      }
  
      this.creatingState.set(true);
      this.errorMessageState.set(null);
  
      this.departmentApi
        .createDepartment(request)
        .pipe(
          finalize(() => {
            this.creatingState.set(false);
          })
        )
        .subscribe({
          next: department => {
  
            this.selectedDepartmentState.set(
              department
            );
  
            this.updateDepartmentInCollection(
              department
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(department);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    updateMyDepartment(
      departmentId: string,
      request: UpdateDepartmentRequest,
      afterSuccess?: (
        department: Department
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.updatingState()) {
        return;
      }
  
      this.updatingState.set(true);
      this.errorMessageState.set(null);
  
      this.departmentApi
        .updateMyDepartment(
          departmentId,
          request
        )
        .pipe(
          finalize(() => {
            this.updatingState.set(false);
          })
        )
        .subscribe({
          next: department => {
  
            this.selectedDepartmentState.set(
              department
            );
  
            this.updateDepartmentInCollection(
              department
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(department);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    updateMyDepartmentStatus(
      departmentId: string,
      status: DepartmentStatus,
      afterSuccess?: (
        department: Department
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.statusUpdatingState()) {
        return;
      }
  
      const request:
        UpdateDepartmentStatusRequest = {
          status
        };
  
      this.statusUpdatingState.set(true);
      this.errorMessageState.set(null);
  
      this.departmentApi
        .updateMyDepartmentStatus(
          departmentId,
          request
        )
        .pipe(
          finalize(() => {
            this.statusUpdatingState.set(false);
          })
        )
        .subscribe({
          next: department => {
  
            this.selectedDepartmentState.set(
              department
            );
  
            this.updateDepartmentInCollection(
              department
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(department);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    clearSelectedDepartment(): void {
  
      this.selectedDepartmentState.set(null);
      this.errorMessageState.set(null);
    }
  
    clearError(): void {
  
      this.errorMessageState.set(null);
    }
  
    clearState(): void {
  
      this.departmentsState.set([]);
      this.selectedDepartmentState.set(null);
  
      this.loadingState.set(false);
      this.detailsLoadingState.set(false);
      this.creatingState.set(false);
      this.updatingState.set(false);
      this.statusUpdatingState.set(false);
  
      this.errorMessageState.set(null);
    }
  
    private countByStatus(
      status: DepartmentStatus
    ): number {
  
      return this.departmentsState()
        .filter(department => {
          return department.status === status;
        })
        .length;
    }
  
    private updateDepartmentInCollection(
      updatedDepartment: Department
    ): void {
  
      this.departmentsState.update(
        departments => {
  
          const departmentExists =
            departments.some(department => {
              return department.id
                === updatedDepartment.id;
            });
  
          if (!departmentExists) {
  
            return this.sortDepartments([
              updatedDepartment,
              ...departments
            ]);
          }
  
          const updatedDepartments =
            departments.map(department => {
  
              if (
                department.id
                === updatedDepartment.id
              ) {
                return updatedDepartment;
              }
  
              return department;
            });
  
          return this.sortDepartments(
            updatedDepartments
          );
        }
      );
    }
  
    private sortDepartments(
      departments: Department[]
    ): Department[] {
  
      const statusOrder:
        Record<DepartmentStatus, number> = {
          ACTIVE: 0,
          INACTIVE: 1
        };
  
      return [...departments].sort(
        (
          firstDepartment,
          secondDepartment
        ) => {
  
          const statusDifference =
            statusOrder[firstDepartment.status]
            - statusOrder[secondDepartment.status];
  
          if (statusDifference !== 0) {
            return statusDifference;
          }
  
          return firstDepartment.departmentName
            .localeCompare(
              secondDepartment.departmentName
            );
        }
      );
    }
  
    private resolveErrorMessage(
      error: HttpErrorResponse
    ): string {
  
      if (error.status === 0) {
  
        return 'Unable to connect to the Provider Service. '
          + 'Please verify that the service is running.';
      }
  
      const backendError =
        error.error;
  
      if (
        typeof backendError === 'string'
        && backendError.trim().length > 0
      ) {
        return backendError;
      }
  
      if (
        backendError !== null
        && typeof backendError === 'object'
        && 'message' in backendError
        && typeof backendError.message === 'string'
        && backendError.message.trim().length > 0
      ) {
        return backendError.message;
      }
  
      if (
        backendError !== null
        && typeof backendError === 'object'
        && 'detail' in backendError
        && typeof backendError.detail === 'string'
        && backendError.detail.trim().length > 0
      ) {
        return backendError.detail;
      }
  
      if (error.status === 400) {
  
        return 'Please review the department information '
          + 'and correct the invalid fields.';
      }
  
      if (error.status === 401) {
  
        return 'Your session has expired. '
          + 'Please sign in again.';
      }
  
      if (error.status === 403) {
  
        return 'You are not authorized to manage '
          + 'this department.';
      }
  
      if (error.status === 404) {
  
        return 'The requested department could not be found.';
      }
  
      if (error.status === 409) {
  
        return 'A department with the same name or code '
          + 'already exists in this organization.';
      }
  
      if (error.status >= 500) {
  
        return 'Department management is temporarily '
          + 'unavailable. Please try again.';
      }
  
      return 'Unable to process the department. '
        + 'Please try again.';
    }
  }