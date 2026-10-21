import {
    Injectable,
    inject
  } from '@angular/core';
  
  import {
    HttpClient,
    HttpParams
  } from '@angular/common/http';
  
  import {
    Observable
  } from 'rxjs';
  
  import {
    Appointment
  } from '../models/appointment';
  
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
    CompleteAppointmentRequest
  } from '../models/complete-appointment-request';
  
  import {
    ProviderCancelAppointmentRequest
  } from '../models/provider-cancel-appointment-request';
  
  @Injectable({
    providedIn: 'root'
  })
  export class AppointmentApiService {
  
    private readonly http =
      inject(HttpClient);
  
    private readonly baseUrl =
      '/api/appointments';
  
    getAvailableSlots(
      doctorId: string,
      appointmentDate: string
    ): Observable<AvailableSlots> {
  
      const params =
        new HttpParams()
          .set(
            'date',
            appointmentDate
          );
  
      return this.http.get<AvailableSlots>(
        `${this.baseUrl}/slots/doctor/${doctorId}`,
        {
          params
        }
      );
    }
  
    bookAppointment(
      request: BookAppointmentRequest
    ): Observable<Appointment> {
  
      return this.http.post<Appointment>(
        this.baseUrl,
        request
      );
    }
  
    getMyAppointments():
      Observable<Appointment[]> {
  
      return this.http.get<Appointment[]>(
        `${this.baseUrl}/me`
      );
    }
  
    getMyAppointmentById(
      appointmentId: string
    ): Observable<Appointment> {
  
      return this.http.get<Appointment>(
        `${this.baseUrl}/me/${appointmentId}`
      );
    }
  
    cancelMyAppointment(
      appointmentId: string,
      request: CancelAppointmentRequest
    ): Observable<Appointment> {
  
      return this.http.patch<Appointment>(
        `${this.baseUrl}/me/${appointmentId}/cancel`,
        request
      );
    }
  
    getProviderAppointments():
      Observable<Appointment[]> {
  
      return this.http.get<Appointment[]>(
        `${this.baseUrl}/provider`
      );
    }
  
    getProviderAppointmentById(
      appointmentId: string
    ): Observable<Appointment> {
  
      return this.http.get<Appointment>(
        `${this.baseUrl}/provider/${appointmentId}`
      );
    }
  
    confirmAppointment(
      appointmentId: string
    ): Observable<Appointment> {
  
      return this.http.patch<Appointment>(
        `${this.baseUrl}/provider/${appointmentId}/confirm`,
        null
      );
    }
  
    cancelProviderAppointment(
      appointmentId: string,
      request: ProviderCancelAppointmentRequest
    ): Observable<Appointment> {
  
      return this.http.patch<Appointment>(
        `${this.baseUrl}/provider/${appointmentId}/cancel`,
        request
      );
    }
  
    completeAppointment(
      appointmentId: string,
      request: CompleteAppointmentRequest
    ): Observable<Appointment> {
  
      return this.http.patch<Appointment>(
        `${this.baseUrl}/provider/${appointmentId}/complete`,
        request
      );
    }
  
    markAppointmentAsNoShow(
      appointmentId: string
    ): Observable<Appointment> {
  
      return this.http.patch<Appointment>(
        `${this.baseUrl}/provider/${appointmentId}/no-show`,
        null
      );
    }
  }