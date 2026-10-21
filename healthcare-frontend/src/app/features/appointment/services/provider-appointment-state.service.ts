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
    CompleteAppointmentRequest
  } from '../models/complete-appointment-request';
  
  import {
    ProviderCancelAppointmentRequest
  } from '../models/provider-cancel-appointment-request';
  
  import {
    AppointmentApiService
  } from './appointment-api.service';
  
  @Injectable({
    providedIn: 'root'
  })
  export class ProviderAppointmentStateService {
  
    private readonly appointmentApi =
      inject(AppointmentApiService);
  
    private readonly appointmentsState =
      signal<Appointment[]>([]);
  
    private readonly selectedAppointmentState =
      signal<Appointment | null>(null);
  
    private readonly loadingState =
      signal(false);
  
    private readonly detailsLoadingState =
      signal(false);
  
    private readonly confirmingState =
      signal(false);
  
    private readonly cancellingState =
      signal(false);
  
    private readonly completingState =
      signal(false);
  
    private readonly markingNoShowState =
      signal(false);
  
    private readonly errorMessageState =
      signal<string | null>(null);
  
    readonly appointments =
      this.appointmentsState.asReadonly();
  
    readonly selectedAppointment =
      this.selectedAppointmentState.asReadonly();
  
    readonly loading =
      this.loadingState.asReadonly();
  
    readonly detailsLoading =
      this.detailsLoadingState.asReadonly();
  
    readonly confirming =
      this.confirmingState.asReadonly();
  
    readonly cancelling =
      this.cancellingState.asReadonly();
  
    readonly completing =
      this.completingState.asReadonly();
  
    readonly markingNoShow =
      this.markingNoShowState.asReadonly();
  
    readonly errorMessage =
      this.errorMessageState.asReadonly();
  
    readonly actionInProgress =
      computed(() => {
        return this.confirmingState()
          || this.cancellingState()
          || this.completingState()
          || this.markingNoShowState();
      });
  
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
  
    readonly doctorCount =
      computed(() => {
        const doctorIds = this.appointmentsState()
          .map(appointment => appointment.doctorId)
          .filter(doctorId => doctorId.length > 0);
  
        return new Set(doctorIds).size;
      });
  
    readonly memberCount =
      computed(() => {
        const memberIds = this.appointmentsState()
          .map(appointment => appointment.memberId)
          .filter(memberId => memberId.length > 0);
  
        return new Set(memberIds).size;
      });
  
    loadProviderAppointments(): void {
      if (this.loadingState()) {
        return;
      }
  
      this.loadingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .getProviderAppointments()
        .pipe(
          finalize(() => {
            this.loadingState.set(false);
          })
        )
        .subscribe({
          next: appointments => {
            this.appointmentsState.set(
              this.sortAppointments(appointments)
            );
  
            this.errorMessageState.set(null);
          },
  
          error: (error: HttpErrorResponse) => {
            this.appointmentsState.set([]);
  
            this.errorMessageState.set(
              this.resolveErrorMessage(error)
            );
          }
        });
    }
  
    loadProviderAppointmentById(
      appointmentId: string,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
      if (this.detailsLoadingState()) {
        return;
      }
  
      this.detailsLoadingState.set(true);
      this.selectedAppointmentState.set(null);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .getProviderAppointmentById(appointmentId)
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
  
          error: (error: HttpErrorResponse) => {
            const message =
              this.resolveErrorMessage(error);
  
            this.selectedAppointmentState.set(null);
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    confirmAppointment(
      appointmentId: string,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
      if (this.actionInProgress()) {
        return;
      }
  
      this.confirmingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .confirmAppointment(appointmentId)
        .pipe(
          finalize(() => {
            this.confirmingState.set(false);
          })
        )
        .subscribe({
          next: appointment => {
            this.handleUpdatedAppointment(
              appointment
            );
  
            afterSuccess?.(appointment);
          },
  
          error: (error: HttpErrorResponse) => {
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    cancelProviderAppointment(
      appointmentId: string,
      request: ProviderCancelAppointmentRequest,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
      if (this.actionInProgress()) {
        return;
      }
  
      this.cancellingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .cancelProviderAppointment(
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
            this.handleUpdatedAppointment(
              appointment
            );
  
            afterSuccess?.(appointment);
          },
  
          error: (error: HttpErrorResponse) => {
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    completeAppointment(
      appointmentId: string,
      request: CompleteAppointmentRequest,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
      if (this.actionInProgress()) {
        return;
      }
  
      this.completingState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .completeAppointment(
          appointmentId,
          request
        )
        .pipe(
          finalize(() => {
            this.completingState.set(false);
          })
        )
        .subscribe({
          next: appointment => {
            this.handleUpdatedAppointment(
              appointment
            );
  
            afterSuccess?.(appointment);
          },
  
          error: (error: HttpErrorResponse) => {
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
    }
  
    markAppointmentAsNoShow(
      appointmentId: string,
      afterSuccess?: (
        appointment: Appointment
      ) => void,
      afterError?: (
        message: string
      ) => void
    ): void {
      if (this.actionInProgress()) {
        return;
      }
  
      this.markingNoShowState.set(true);
      this.errorMessageState.set(null);
  
      this.appointmentApi
        .markAppointmentAsNoShow(
          appointmentId
        )
        .pipe(
          finalize(() => {
            this.markingNoShowState.set(false);
          })
        )
        .subscribe({
          next: appointment => {
            this.handleUpdatedAppointment(
              appointment
            );
  
            afterSuccess?.(appointment);
          },
  
          error: (error: HttpErrorResponse) => {
            const message =
              this.resolveErrorMessage(error);
  
            this.errorMessageState.set(message);
  
            afterError?.(message);
          }
        });
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
  
      this.loadingState.set(false);
      this.detailsLoadingState.set(false);
      this.confirmingState.set(false);
      this.cancellingState.set(false);
      this.completingState.set(false);
      this.markingNoShowState.set(false);
  
      this.errorMessageState.set(null);
    }
  
    private handleUpdatedAppointment(
      appointment: Appointment
    ): void {
      this.selectedAppointmentState.set(
        appointment
      );
  
      this.updateAppointmentInCollection(
        appointment
      );
  
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
        (
          firstAppointment,
          secondAppointment
        ) => {
          const firstDateTime =
            this.getAppointmentDateTime(
              firstAppointment
            );
  
          const secondDateTime =
            this.getAppointmentDateTime(
              secondAppointment
            );
  
          return secondDateTime
            - firstDateTime;
        }
      );
    }
  
    private getAppointmentDateTime(
      appointment: Appointment
    ): number {
      const timestamp = new Date(
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
  
      const backendError = error.error;
  
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
        return 'The appointment action is invalid for '
          + 'the current appointment status or schedule.';
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
        return 'The requested appointment could not be found.';
      }
  
      if (error.status === 409) {
        return 'The appointment was changed by another action. '
          + 'Please refresh and try again.';
      }
  
      if (error.status >= 500) {
        return 'Appointment services are temporarily '
          + 'unavailable. Please try again.';
      }
  
      return 'Unable to process the appointment. '
        + 'Please try again.';
    }
  }