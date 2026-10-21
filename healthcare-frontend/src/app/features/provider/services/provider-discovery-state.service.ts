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
    finalize,
    forkJoin
  } from 'rxjs';
  
  import {
    Department
  } from '../models/department';
  
  import {
    Doctor
  } from '../models/doctor';
  
  import {
    DoctorAvailability
  } from '../models/doctor-availability';
  
  import {
    ProviderOrganization
  } from '../models/provider-organization';
  
  import {
    DepartmentApiService
  } from './department-api.service';
  
  import {
    DoctorApiService
  } from './doctor-api.service';
  
  import {
    DoctorAvailabilityApiService
  } from './doctor-availability-api.service';
  
  import {
    ProviderApiService
  } from './provider-api.service';
  
  @Injectable({
    providedIn: 'root'
  })
  export class ProviderDiscoveryStateService {
  
    private readonly providerApi =
      inject(ProviderApiService);
  
    private readonly departmentApi =
      inject(DepartmentApiService);
  
    private readonly doctorApi =
      inject(DoctorApiService);
  
    private readonly availabilityApi =
      inject(DoctorAvailabilityApiService);
  
    private readonly providersState =
      signal<ProviderOrganization[]>([]);
  
    private readonly selectedProviderState =
      signal<ProviderOrganization | null>(null);
  
    private readonly departmentsState =
      signal<Department[]>([]);
  
    private readonly doctorsState =
      signal<Doctor[]>([]);
  
    private readonly selectedDoctorState =
      signal<Doctor | null>(null);
  
    private readonly schedulesState =
      signal<DoctorAvailability[]>([]);
  
    private readonly loadingState =
      signal(false);
  
    private readonly providerDetailsLoadingState =
      signal(false);
  
    private readonly doctorDetailsLoadingState =
      signal(false);
  
    private readonly errorMessageState =
      signal<string | null>(null);
  
    readonly providers =
      this.providersState.asReadonly();
  
    readonly selectedProvider =
      this.selectedProviderState.asReadonly();
  
    readonly departments =
      this.departmentsState.asReadonly();
  
    readonly doctors =
      this.doctorsState.asReadonly();
  
    readonly selectedDoctor =
      this.selectedDoctorState.asReadonly();
  
    readonly schedules =
      this.schedulesState.asReadonly();
  
    readonly loading =
      this.loadingState.asReadonly();
  
    readonly providerDetailsLoading =
      this.providerDetailsLoadingState.asReadonly();
  
    readonly doctorDetailsLoading =
      this.doctorDetailsLoadingState.asReadonly();
  
    readonly errorMessage =
      this.errorMessageState.asReadonly();
  
    readonly providerCount =
      computed(() => {
        return this.providersState().length;
      });
  
    readonly hospitalCount =
      computed(() => {
        return this.providersState()
          .filter(provider => {
            return provider.providerType === 'HOSPITAL';
          })
          .length;
      });
  
    readonly emergencyProviderCount =
      computed(() => {
        return this.providersState()
          .filter(provider => {
            return provider.emergencyServicesAvailable;
          })
          .length;
      });
  
    readonly cityCount =
      computed(() => {
  
        const cities =
          this.providersState()
            .map(provider => {
              return provider.city
                .trim()
                .toLowerCase();
            })
            .filter(city => {
              return city.length > 0;
            });
  
        return new Set(cities).size;
      });
  
    loadAvailableProviders(): void {
  
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.providerApi
        .getAvailableProviders()
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: providers => {
  
            const sortedProviders =
              [...providers].sort(
                (firstProvider, secondProvider) => {
  
                  if (
                    firstProvider.emergencyServicesAvailable
                    !== secondProvider.emergencyServicesAvailable
                  ) {
                    return firstProvider.emergencyServicesAvailable
                      ? -1
                      : 1;
                  }
  
                  return firstProvider.organizationName
                    .localeCompare(
                      secondProvider.organizationName
                    );
                }
              );
  
            this.providersState.set(
              sortedProviders
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.providersState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadProviderDetails(
      providerId: string
    ): void {
  
      if (this.providerDetailsLoadingState()) {
        return;
      }
  
      this.providerDetailsLoadingState.set(true);
      this.errorMessageState.set(null);
      this.selectedProviderState.set(null);
      this.departmentsState.set([]);
      this.doctorsState.set([]);
  
      forkJoin({
        provider:
          this.providerApi
            .getAvailableProviderById(
              providerId
            ),
  
        departments:
          this.departmentApi
            .getAvailableDepartmentsByProvider(
              providerId
            ),
  
        doctors:
          this.doctorApi
            .getAvailableDoctorsByProvider(
              providerId
            )
      })
        .pipe(
          finalize(() => {
            this.providerDetailsLoadingState.set(
              false
            );
          })
        )
        .subscribe({
          next: result => {
  
            this.selectedProviderState.set(
              result.provider
            );
  
            this.departmentsState.set(
              this.sortDepartments(
                result.departments
              )
            );
  
            this.doctorsState.set(
              this.sortDoctors(
                result.doctors
              )
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.selectedProviderState.set(null);
            this.departmentsState.set([]);
            this.doctorsState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadDoctorDetails(
      doctorId: string
    ): void {
  
      if (this.doctorDetailsLoadingState()) {
        return;
      }
  
      this.doctorDetailsLoadingState.set(true);
      this.errorMessageState.set(null);
      this.selectedDoctorState.set(null);
      this.schedulesState.set([]);
  
      forkJoin({
        doctor:
          this.doctorApi
            .getAvailableDoctorById(
              doctorId
            ),
  
        schedules:
          this.availabilityApi
            .getAvailableDoctorSchedules(
              doctorId
            )
      })
        .pipe(
          finalize(() => {
            this.doctorDetailsLoadingState.set(
              false
            );
          })
        )
        .subscribe({
          next: result => {
  
            this.selectedDoctorState.set(
              result.doctor
            );
  
            this.schedulesState.set(
              this.sortSchedules(
                result.schedules
              )
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.selectedDoctorState.set(null);
            this.schedulesState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    clearProviderDetails(): void {
  
      this.selectedProviderState.set(null);
      this.departmentsState.set([]);
      this.doctorsState.set([]);
      this.errorMessageState.set(null);
    }
  
    clearDoctorDetails(): void {
  
      this.selectedDoctorState.set(null);
      this.schedulesState.set([]);
      this.errorMessageState.set(null);
    }
  
    clearError(): void {
  
      this.errorMessageState.set(null);
    }
  
    clearState(): void {
  
      this.providersState.set([]);
      this.selectedProviderState.set(null);
      this.departmentsState.set([]);
      this.doctorsState.set([]);
      this.selectedDoctorState.set(null);
      this.schedulesState.set([]);
      this.loadingState.set(false);
      this.providerDetailsLoadingState.set(false);
      this.doctorDetailsLoadingState.set(false);
      this.errorMessageState.set(null);
    }
  
    private sortDepartments(
      departments: Department[]
    ): Department[] {
  
      return [...departments].sort(
        (firstDepartment, secondDepartment) => {
  
          return firstDepartment.departmentName
            .localeCompare(
              secondDepartment.departmentName
            );
        }
      );
    }
  
    private sortDoctors(
      doctors: Doctor[]
    ): Doctor[] {
  
      return [...doctors].sort(
        (firstDoctor, secondDoctor) => {
  
          const specializationDifference =
            firstDoctor.specialization
              .localeCompare(
                secondDoctor.specialization
              );
  
          if (specializationDifference !== 0) {
            return specializationDifference;
          }
  
          return firstDoctor.fullName
            .localeCompare(
              secondDoctor.fullName
            );
        }
      );
    }
  
    private sortSchedules(
      schedules: DoctorAvailability[]
    ): DoctorAvailability[] {
  
      const dayOrder:
        Record<string, number> = {
          MONDAY: 1,
          TUESDAY: 2,
          WEDNESDAY: 3,
          THURSDAY: 4,
          FRIDAY: 5,
          SATURDAY: 6,
          SUNDAY: 7
        };
  
      return [...schedules].sort(
        (firstSchedule, secondSchedule) => {
  
          const dayDifference =
            dayOrder[firstSchedule.dayOfWeek]
            - dayOrder[secondSchedule.dayOfWeek];
  
          if (dayDifference !== 0) {
            return dayDifference;
          }
  
          return firstSchedule.startTime
            .localeCompare(
              secondSchedule.startTime
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
  
      if (error.status === 401) {
  
        return 'Your session has expired. '
          + 'Please sign in again.';
      }
  
      if (error.status === 403) {
  
        return 'You are not authorized to access '
          + 'Provider information.';
      }
  
      if (error.status === 404) {
  
        return 'The requested healthcare Provider '
          + 'or doctor could not be found.';
      }
  
      if (error.status >= 500) {
  
        return 'Provider information is temporarily '
          + 'unavailable. Please try again.';
      }
  
      return 'Unable to load healthcare Provider '
        + 'information. Please try again.';
    }
  }