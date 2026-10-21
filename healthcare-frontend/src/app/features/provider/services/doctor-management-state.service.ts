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
    CreateDoctorRequest
  } from '../models/create-doctor-request';
  
  import {
    Doctor
  } from '../models/doctor';
  
  import {
    DoctorStatus
  } from '../models/doctor-status';
  
  import {
    UpdateDoctorRequest
  } from '../models/update-doctor-request';
  
  import {
    UpdateDoctorStatusRequest
  } from '../models/update-doctor-status-request';
  
  import {
    DoctorApiService
  } from './doctor-api.service';
  
  @Injectable({
    providedIn: 'root'
  })
  export class DoctorManagementStateService {
  
    private readonly doctorApi =
      inject(DoctorApiService);
  
    private readonly doctorsState =
      signal<Doctor[]>([]);
  
    private readonly selectedDoctorState =
      signal<Doctor | null>(null);
  
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
  
    readonly doctors =
      this.doctorsState.asReadonly();
  
    readonly selectedDoctor =
      this.selectedDoctorState.asReadonly();
  
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
        return this.doctorsState().length;
      });
  
    readonly activeCount =
      computed(() => {
        return this.countByStatus('ACTIVE');
      });
  
    readonly inactiveCount =
      computed(() => {
        return this.countByStatus('INACTIVE');
      });
  
    readonly departmentCount =
      computed(() => {
  
        const departmentIds =
          this.doctorsState()
            .map(doctor => {
              return doctor.departmentId;
            })
            .filter(departmentId => {
              return departmentId.length > 0;
            });
  
        return new Set(departmentIds).size;
      });
  
    readonly specializationCount =
      computed(() => {
  
        const specializations =
          this.doctorsState()
            .map(doctor => {
              return doctor.specialization
                .trim()
                .toLowerCase();
            })
            .filter(specialization => {
              return specialization.length > 0;
            });
  
        return new Set(specializations).size;
      });
  
    loadMyDoctors(): void {
  
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.doctorApi
        .getMyDoctors()
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: doctors => {
  
            this.doctorsState.set(
              this.sortDoctors(doctors)
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.doctorsState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadMyDoctorById(
      doctorId: string,
      afterSuccess?: (
        doctor: Doctor
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.detailsLoadingState()) {
        return;
      }
  
      this.detailsLoadingState.set(true);
      this.selectedDoctorState.set(null);
      this.errorMessageState.set(null);
  
      this.doctorApi
        .getMyDoctorById(doctorId)
        .pipe(
          finalize(() => {
            this.detailsLoadingState.set(false);
          })
        )
        .subscribe({
          next: doctor => {
  
            this.selectedDoctorState.set(
              doctor
            );
  
            this.updateDoctorInCollection(
              doctor
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(doctor);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            const message =
              this.resolveErrorMessage(error);
  
            this.selectedDoctorState.set(null);
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    createDoctor(
      request: CreateDoctorRequest,
      afterSuccess?: (
        doctor: Doctor
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
  
      this.doctorApi
        .createDoctor(request)
        .pipe(
          finalize(() => {
            this.creatingState.set(false);
          })
        )
        .subscribe({
          next: doctor => {
  
            this.selectedDoctorState.set(
              doctor
            );
  
            this.updateDoctorInCollection(
              doctor
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(doctor);
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
  
    updateMyDoctor(
      doctorId: string,
      request: UpdateDoctorRequest,
      afterSuccess?: (
        doctor: Doctor
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
  
      this.doctorApi
        .updateMyDoctor(
          doctorId,
          request
        )
        .pipe(
          finalize(() => {
            this.updatingState.set(false);
          })
        )
        .subscribe({
          next: doctor => {
  
            this.selectedDoctorState.set(
              doctor
            );
  
            this.updateDoctorInCollection(
              doctor
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(doctor);
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
  
    updateMyDoctorStatus(
      doctorId: string,
      status: DoctorStatus,
      afterSuccess?: (
        doctor: Doctor
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.statusUpdatingState()) {
        return;
      }
  
      const request:
        UpdateDoctorStatusRequest = {
          status
        };
  
      this.statusUpdatingState.set(true);
      this.errorMessageState.set(null);
  
      this.doctorApi
        .updateMyDoctorStatus(
          doctorId,
          request
        )
        .pipe(
          finalize(() => {
            this.statusUpdatingState.set(false);
          })
        )
        .subscribe({
          next: doctor => {
  
            this.selectedDoctorState.set(
              doctor
            );
  
            this.updateDoctorInCollection(
              doctor
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(doctor);
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
  
    clearSelectedDoctor(): void {
  
      this.selectedDoctorState.set(null);
      this.errorMessageState.set(null);
    }
  
    clearError(): void {
  
      this.errorMessageState.set(null);
    }
  
    clearState(): void {
  
      this.doctorsState.set([]);
      this.selectedDoctorState.set(null);
  
      this.loadingState.set(false);
      this.detailsLoadingState.set(false);
      this.creatingState.set(false);
      this.updatingState.set(false);
      this.statusUpdatingState.set(false);
  
      this.errorMessageState.set(null);
    }
  
    private countByStatus(
      status: DoctorStatus
    ): number {
  
      return this.doctorsState()
        .filter(doctor => {
          return doctor.status === status;
        })
        .length;
    }
  
    private updateDoctorInCollection(
      updatedDoctor: Doctor
    ): void {
  
      this.doctorsState.update(
        doctors => {
  
          const doctorExists =
            doctors.some(doctor => {
              return doctor.id
                === updatedDoctor.id;
            });
  
          if (!doctorExists) {
  
            return this.sortDoctors([
              updatedDoctor,
              ...doctors
            ]);
          }
  
          const updatedDoctors =
            doctors.map(doctor => {
  
              if (
                doctor.id
                === updatedDoctor.id
              ) {
                return updatedDoctor;
              }
  
              return doctor;
            });
  
          return this.sortDoctors(
            updatedDoctors
          );
        }
      );
    }
  
    private sortDoctors(
      doctors: Doctor[]
    ): Doctor[] {
  
      const statusOrder:
        Record<DoctorStatus, number> = {
          ACTIVE: 0,
          INACTIVE: 1
        };
  
      return [...doctors].sort(
        (
          firstDoctor,
          secondDoctor
        ) => {
  
          const statusDifference =
            statusOrder[firstDoctor.status]
            - statusOrder[secondDoctor.status];
  
          if (statusDifference !== 0) {
            return statusDifference;
          }
  
          const departmentDifference =
            firstDoctor.departmentName
              .localeCompare(
                secondDoctor.departmentName
              );
  
          if (departmentDifference !== 0) {
            return departmentDifference;
          }
  
          return firstDoctor.fullName
            .localeCompare(
              secondDoctor.fullName
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
  
        return 'Please review the doctor information '
          + 'and correct the invalid fields.';
      }
  
      if (error.status === 401) {
  
        return 'Your session has expired. '
          + 'Please sign in again.';
      }
  
      if (error.status === 403) {
  
        return 'You are not authorized to manage '
          + 'this doctor.';
      }
  
      if (error.status === 404) {
  
        return 'The requested doctor or department '
          + 'could not be found.';
      }
  
      if (error.status === 409) {
  
        return 'A doctor with the same medical '
          + 'registration number already exists.';
      }
  
      if (error.status >= 500) {
  
        return 'Doctor management is temporarily '
          + 'unavailable. Please try again.';
      }
  
      return 'Unable to process the doctor information. '
        + 'Please try again.';
    }
  }