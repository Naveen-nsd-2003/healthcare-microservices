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
    Appointment
  } from '../models/appointment';
  
  import {
    AppointmentStatus
  } from '../models/appointment-status';
  
  import {
    AvailableSlots
  } from '../models/available-slots';
  
  import {
    BookAppointmentRequest
  } from '../models/book-appointment-request';
  
  import {
    CancelAppointmentRequest
  } from '../models/cancel-appointment-request';
  
  import {
    AppointmentApiService
  } from './appointment-api.service';
  
  @Injectable({
    providedIn: 'root'
  })
  export class AppointmentStateService {
  
    private readonly appointmentApi =
      inject(AppointmentApiService);
  
    private readonly appointmentsState =
      signal<Appointment[]>([]);
  
    private readonly selectedAppointmentState =
      signal<Appointment | null>(null);
  
    private readonly availableSlotsState =
      signal<AvailableSlots | null>(null);
  
    private readonly loadingState =
      signal(false);
  
    private readonly detailsLoadingState =
      signal(false);
  
    private readonly slotsLoadingState =
      signal(false);
  
    private readonly bookingState =
      signal(false);
  
    private readonly cancellingState =
      signal(false);
  
    private readonly errorMessageState =
      signal<string | null>(null);
  
    readonly appointments =
      this.appointmentsState.asReadonly();
  
    readonly selectedAppointment =
      this.selectedAppointmentState.asReadonly();
  
    readonly availableSlots =
      this.availableSlotsState.asReadonly();
  
    readonly loading =
      this.loadingState.asReadonly();
  
    readonly detailsLoading =
      this.detailsLoadingState.asReadonly();
  
    readonly slotsLoading =
      this.slotsLoadingState.asReadonly();
  
    readonly booking =
      this.bookingState.asReadonly();
  
    readonly cancelling =
      this.cancellingState.asReadonly();
  
    readonly errorMessage =
      this.errorMessageState.asReadonly();
  
    readonly totalCount =
      computed(() => {
        return this.appointmentsState().length;
      });
  
    readonly bookedCount =
      computed(() => {
        return this.countByStatus('BOOKED');
      });
  
    readonly confirmedCount =
      computed(() => {
        return this.countByStatus('CONFIRMED');
      });
  
    readonly completedCount =
      computed(() => {
        return this.countByStatus('COMPLETED');
      });
  
    readonly cancelledCount =
      computed(() => {
        return this.countByStatus('CANCELLED');
      });
  
    readonly noShowCount =
      computed(() => {
        return this.countByStatus('NO_SHOW');
      });
  
    readonly availableSlotCount =
      computed(() => {
        return this.availableSlotsState()
          ?.availableSlots.length ?? 0;
      });
  
    loadAvailableSlots(
      doctorId: string,
      appointmentDate: string,
      afterSuccess?: (
        slots: AvailableSlots
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.slotsLoadingState()) {
        return;
      }
  
      this.slotsLoadingState.set(true);
      this.errorMessageState.set(null);
      this.availableSlotsState.set(null);
  
      this.appointmentApi
        .getAvailableSlots(
          doctorId,
          appointmentDate
        )
        .pipe(
          finalize(() => {
            this.slotsLoadingState.set(false);
          })
        )
        .subscribe({
          next: slots => {
  
            const sortedSlots: AvailableSlots = {
              ...slots,
  
              availableSlots:
                [...slots.availableSlots].sort(
                  (firstSlot, secondSlot) => {
                    return firstSlot.startTime
                      .localeCompare(
                        secondSlot.startTime
                      );
                  }
                )
            };
  
            this.availableSlotsState.set(
              sortedSlots
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(sortedSlots);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            const message =
              this.resolveErrorMessage(error);
  
            this.availableSlotsState.set(null);
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    bookAppointment(
      request: BookAppointmentRequest,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.bookingState()) {
        return;
      }
  
      this.bookingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .bookAppointment(request)
        .pipe(
          finalize(() => {
            this.bookingState.set(false);
          })
        )
        .subscribe({
          next: appointment => {
  
            this.selectedAppointmentState.set(
              appointment
            );
  
            this.updateAppointmentInCollection(
              appointment
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(appointment);
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
  
    loadMyAppointments(): void {
  
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .getMyAppointments()
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: appointments => {
  
            this.appointmentsState.set(
              this.sortAppointments(
                appointments
              )
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.appointmentsState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadMyAppointmentById(
      appointmentId: string,
      afterSuccess?: (
        appointment: Appointment
      ) => void
    ): void {
  
      if (this.detailsLoadingState()) {
        return;
      }
  
      this.detailsLoadingState.set(true);
      this.errorMessageState.set(null);
      this.selectedAppointmentState.set(null);
  
      this.appointmentApi
        .getMyAppointmentById(
          appointmentId
        )
        .pipe(
          finalize(() => {
            this.detailsLoadingState.set(false);
          })
        )
        .subscribe({
          next: appointment => {
  
            this.selectedAppointmentState.set(
              appointment
            );
  
            this.updateAppointmentInCollection(
              appointment
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(appointment);
          },
  
          error: (
            error: HttpErrorResponse
          ) => {
  
            this.selectedAppointmentState.set(null);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    cancelMyAppointment(
      appointmentId: string,
      request: CancelAppointmentRequest,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
  
      if (this.cancellingState()) {
        return;
      }
  
      this.cancellingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .cancelMyAppointment(
          appointmentId,
          request
        )
        .pipe(
          finalize(() => {
            this.cancellingState.set(false);
          })
        )
        .subscribe({
          next: appointment => {
  
            this.selectedAppointmentState.set(
              appointment
            );
  
            this.updateAppointmentInCollection(
              appointment
            );
  
            this.errorMessageState.set(null);
  
            afterSuccess?.(appointment);
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
  
    clearAvailableSlots(): void {
  
      this.availableSlotsState.set(null);
    }
  
    clearSelectedAppointment(): void {
  
      this.selectedAppointmentState.set(null);
      this.errorMessageState.set(null);
    }
  
    clearError(): void {
  
      this.errorMessageState.set(null);
    }
  
    clearState(): void {
  
      this.appointmentsState.set([]);
      this.selectedAppointmentState.set(null);
      this.availableSlotsState.set(null);
  
      this.loadingState.set(false);
      this.detailsLoadingState.set(false);
      this.slotsLoadingState.set(false);
      this.bookingState.set(false);
      this.cancellingState.set(false);
  
      this.errorMessageState.set(null);
    }
  
    private countByStatus(
      status: AppointmentStatus
    ): number {
  
      return this.appointmentsState()
        .filter(appointment => {
          return appointment.status === status;
        })
        .length;
    }
  
    private updateAppointmentInCollection(
      updatedAppointment: Appointment
    ): void {
  
      this.appointmentsState.update(
        appointments => {
  
          const appointmentExists =
            appointments.some(appointment => {
              return appointment.id
                === updatedAppointment.id;
            });
  
          if (!appointmentExists) {
  
            return this.sortAppointments([
              updatedAppointment,
              ...appointments
            ]);
          }
  
          const updatedAppointments =
            appointments.map(appointment => {
  
              if (
                appointment.id
                === updatedAppointment.id
              ) {
                return updatedAppointment;
              }
  
              return appointment;
            });
  
          return this.sortAppointments(
            updatedAppointments
          );
        }
      );
    }
  
    private sortAppointments(
      appointments: Appointment[]
    ): Appointment[] {
  
      return [...appointments].sort(
        (firstAppointment, secondAppointment) => {
  
          const firstDateTime =
            this.getAppointmentDateTime(
              firstAppointment
            );
  
          const secondDateTime =
            this.getAppointmentDateTime(
              secondAppointment
            );
  
          return secondDateTime - firstDateTime;
        }
      );
    }
  
    private getAppointmentDateTime(
      appointment: Appointment
    ): number {
  
      const timestamp =
        new Date(
          appointment.appointmentDate
          + 'T'
          + appointment.startTime
        ).getTime();
  
      if (Number.isNaN(timestamp)) {
        return 0;
      }
  
      return timestamp;
    }
  
    private resolveErrorMessage(
      error: HttpErrorResponse
    ): string {
  
      if (error.status === 0) {
  
        return 'Unable to connect to the Appointment Service. '
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
  
        return 'The appointment request is invalid. '
          + 'Please review the selected date, time, '
          + 'and reason for visit.';
      }
  
      if (error.status === 401) {
  
        return 'Your session has expired. '
          + 'Please sign in again.';
      }
  
      if (error.status === 403) {
  
        return 'You are not authorized to manage '
          + 'this appointment.';
      }
  
      if (error.status === 404) {
  
        return 'The requested doctor, schedule, slot, '
          + 'or appointment could not be found.';
      }
  
      if (error.status === 409) {
  
        return 'The selected appointment slot is no '
          + 'longer available. Please select another slot.';
      }
  
      if (error.status >= 500) {
  
        return 'Appointment services are temporarily '
          + 'unavailable. Please try again.';
      }
  
      return 'Unable to process the appointment. '
        + 'Please try again.';
    }
  }