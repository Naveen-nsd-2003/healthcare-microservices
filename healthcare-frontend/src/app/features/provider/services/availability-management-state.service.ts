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
    AvailabilityStatus
  } from '../models/availability-status';
  
  import {
    CreateDoctorAvailabilityRequest
  } from '../models/create-doctor-availability-request';
  
  import {
    DayOfWeek
  } from '../models/day-of-week';
  
  import {
    DoctorAvailability
  } from '../models/doctor-availability';
  
  import {
    UpdateAvailabilityStatusRequest
  } from '../models/update-availability-status-request';
  
  import {
    UpdateDoctorAvailabilityRequest
  } from '../models/update-doctor-availability-request';
  
  import {
    DoctorAvailabilityApiService
  } from './doctor-availability-api.service';
  
  @Injectable({
    providedIn: 'root'
  })
  export class AvailabilityManagementStateService {
  
    private readonly availabilityApi =
      inject(DoctorAvailabilityApiService);
  
    private readonly availabilitiesState =
      signal<DoctorAvailability[]>([]);
  
    private readonly selectedAvailabilityState =
      signal<DoctorAvailability | null>(null);
  
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
  
    private readonly deletingState =
      signal(false);
  
    private readonly errorMessageState =
      signal<string | null>(null);
  
    readonly availabilities =
      this.availabilitiesState.asReadonly();
  
    readonly selectedAvailability =
      this.selectedAvailabilityState.asReadonly();
  
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
  
    readonly deleting =
      this.deletingState.asReadonly();
  
    readonly errorMessage =
      this.errorMessageState.asReadonly();
  
    readonly totalCount =
      computed(() => {
        return this.availabilitiesState().length;
      });
  
    readonly activeCount =
      computed(() => {
        return this.countByStatus('ACTIVE');
      });
  
    readonly inactiveCount =
      computed(() => {
        return this.countByStatus('INACTIVE');
      });
  
    readonly doctorCount =
      computed(() => {
  
        const doctorIds =
          this.availabilitiesState()
            .map(availability => {
              return availability.doctorId;
            })
            .filter(doctorId => {
              return doctorId.length > 0;
            });
  
        return new Set(doctorIds).size;
      });
  
    loadMyAvailabilities(): void {
  
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.availabilityApi
        .getMyAvailabilities()
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: availabilities => {
  
            this.availabilitiesState.set(
              this.sortAvailabilities(
                availabilities
              )
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.availabilitiesState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadMyDoctorAvailabilities(
      doctorId: string
    ): void {
  
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.availabilityApi
        .getMyDoctorAvailabilities(doctorId)
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: availabilities => {
  
            this.availabilitiesState.set(
              this.sortAvailabilities(
                availabilities
              )
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.availabilitiesState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadMyAvailabilityById(
      availabilityId: string,
      afterSuccess?: (
        availability: DoctorAvailability
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.detailsLoadingState()) {
        return;
      }
  
      this.detailsLoadingState.set(true);
      this.selectedAvailabilityState.set(null);
      this.errorMessageState.set(null);
  
      this.availabilityApi
        .getMyAvailabilityById(
          availabilityId
        )
        .pipe(
          finalize(() => {
            this.detailsLoadingState.set(false);
          })
        )
        .subscribe({
          next: availability => {
  
            this.selectedAvailabilityState.set(
              availability
            );
  
            this.updateAvailabilityInCollection(
              availability
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(availability);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            const message =
              this.resolveErrorMessage(error);
  
            this.selectedAvailabilityState.set(null);
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    createAvailability(
      request: CreateDoctorAvailabilityRequest,
      afterSuccess?: (
        availability: DoctorAvailability
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
  
      this.availabilityApi
        .createAvailability(request)
        .pipe(
          finalize(() => {
            this.creatingState.set(false);
          })
        )
        .subscribe({
          next: availability => {
  
            this.selectedAvailabilityState.set(
              availability
            );
  
            this.updateAvailabilityInCollection(
              availability
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(availability);
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
  
    updateMyAvailability(
      availabilityId: string,
      request: UpdateDoctorAvailabilityRequest,
      afterSuccess?: (
        availability: DoctorAvailability
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
  
      this.availabilityApi
        .updateMyAvailability(
          availabilityId,
          request
        )
        .pipe(
          finalize(() => {
            this.updatingState.set(false);
          })
        )
        .subscribe({
          next: availability => {
  
            this.selectedAvailabilityState.set(
              availability
            );
  
            this.updateAvailabilityInCollection(
              availability
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(availability);
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
  
    updateMyAvailabilityStatus(
      availabilityId: string,
      status: AvailabilityStatus,
      afterSuccess?: (
        availability: DoctorAvailability
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.statusUpdatingState()) {
        return;
      }
  
      const request:
        UpdateAvailabilityStatusRequest = {
          status
        };
  
      this.statusUpdatingState.set(true);
      this.errorMessageState.set(null);
  
      this.availabilityApi
        .updateMyAvailabilityStatus(
          availabilityId,
          request
        )
        .pipe(
          finalize(() => {
            this.statusUpdatingState.set(false);
          })
        )
        .subscribe({
          next: availability => {
  
            this.selectedAvailabilityState.set(
              availability
            );
  
            this.updateAvailabilityInCollection(
              availability
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(availability);
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
  
    deleteMyAvailability(
      availabilityId: string,
      afterSuccess?: () => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.deletingState()) {
        return;
      }
  
      this.deletingState.set(true);
      this.errorMessageState.set(null);
  
      this.availabilityApi
        .deleteMyAvailability(
          availabilityId
        )
        .pipe(
          finalize(() => {
            this.deletingState.set(false);
          })
        )
        .subscribe({
          next: () => {
  
            this.availabilitiesState.update(
              availabilities => {
  
                return availabilities.filter(
                  availability => {
  
                    return availability.id
                      !== availabilityId;
                  }
                );
              }
            );
  
            if (
              this.selectedAvailabilityState()?.id
              === availabilityId
            ) {
              this.selectedAvailabilityState.set(null);
            }
  
            this.errorMessageState.set(null);
  
            afterSuccess?.();
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
  
    clearSelectedAvailability(): void {
  
      this.selectedAvailabilityState.set(null);
      this.errorMessageState.set(null);
    }
  
    clearError(): void {
  
      this.errorMessageState.set(null);
    }
  
    clearState(): void {
  
      this.availabilitiesState.set([]);
      this.selectedAvailabilityState.set(null);
  
      this.loadingState.set(false);
      this.detailsLoadingState.set(false);
      this.creatingState.set(false);
      this.updatingState.set(false);
      this.statusUpdatingState.set(false);
      this.deletingState.set(false);
  
      this.errorMessageState.set(null);
    }
  
    private countByStatus(
      status: AvailabilityStatus
    ): number {
  
      return this.availabilitiesState()
        .filter(availability => {
  
          return availability.status === status;
        })
        .length;
    }
  
    private updateAvailabilityInCollection(
      updatedAvailability: DoctorAvailability
    ): void {
  
      this.availabilitiesState.update(
        availabilities => {
  
          const availabilityExists =
            availabilities.some(availability => {
  
              return availability.id
                === updatedAvailability.id;
            });
  
          if (!availabilityExists) {
  
            return this.sortAvailabilities([
              updatedAvailability,
              ...availabilities
            ]);
          }
  
          const updatedAvailabilities =
            availabilities.map(availability => {
  
              if (
                availability.id
                === updatedAvailability.id
              ) {
                return updatedAvailability;
              }
  
              return availability;
            });
  
          return this.sortAvailabilities(
            updatedAvailabilities
          );
        }
      );
    }
  
    private sortAvailabilities(
      availabilities: DoctorAvailability[]
    ): DoctorAvailability[] {
  
      const statusOrder:
        Record<AvailabilityStatus, number> = {
          ACTIVE: 0,
          INACTIVE: 1
        };
  
      const dayOrder:
        Record<DayOfWeek, number> = {
          MONDAY: 1,
          TUESDAY: 2,
          WEDNESDAY: 3,
          THURSDAY: 4,
          FRIDAY: 5,
          SATURDAY: 6,
          SUNDAY: 7
        };
  
      return [...availabilities].sort(
        (
          firstAvailability,
          secondAvailability
        ) => {
  
          const statusDifference =
            statusOrder[firstAvailability.status]
            - statusOrder[secondAvailability.status];
  
          if (statusDifference !== 0) {
            return statusDifference;
          }
  
          const doctorDifference =
            firstAvailability.doctorName
              .localeCompare(
                secondAvailability.doctorName
              );
  
          if (doctorDifference !== 0) {
            return doctorDifference;
          }
  
          const dayDifference =
            dayOrder[firstAvailability.dayOfWeek]
            - dayOrder[secondAvailability.dayOfWeek];
  
          if (dayDifference !== 0) {
            return dayDifference;
          }
  
          return firstAvailability.startTime
            .localeCompare(
              secondAvailability.startTime
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
  
        return 'Please review the availability information. '
          + 'The ending time must be later than the starting '
          + 'time, and the slot duration must fit the schedule.';
      }
  
      if (error.status === 401) {
  
        return 'Your session has expired. '
          + 'Please sign in again.';
      }
  
      if (error.status === 403) {
  
        return 'You are not authorized to manage '
          + 'this doctor availability.';
      }
  
      if (error.status === 404) {
  
        return 'The requested doctor or availability '
          + 'record could not be found.';
      }
  
      if (error.status === 409) {
  
        return 'This availability overlaps with an existing '
          + 'schedule for the selected doctor and day.';
      }
  
      if (error.status >= 500) {
  
        return 'Availability management is temporarily '
          + 'unavailable. Please try again.';
      }
  
      return 'Unable to process doctor availability. '
        + 'Please try again.';
    }
  }