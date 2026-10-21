import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  CreateDoctorAvailabilityRequest
} from '../models/create-doctor-availability-request';

import {
  DoctorAvailability
} from '../models/doctor-availability';

import {
  UpdateAvailabilityStatusRequest
} from '../models/update-availability-status-request';

import {
  UpdateDoctorAvailabilityRequest
} from '../models/update-doctor-availability-request';

@Injectable({
  providedIn: 'root'
})
export class DoctorAvailabilityApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/availability';

  createAvailability(
    request: CreateDoctorAvailabilityRequest
  ): Observable<DoctorAvailability> {

    return this.http.post<DoctorAvailability>(
      this.baseUrl,
      request
    );
  }

  getMyAvailabilities():
    Observable<DoctorAvailability[]> {

    return this.http.get<DoctorAvailability[]>(
      `${this.baseUrl}/provider`
    );
  }

  getMyDoctorAvailabilities(
    doctorId: string
  ): Observable<DoctorAvailability[]> {

    return this.http.get<DoctorAvailability[]>(
      `${this.baseUrl}/provider/doctor/${doctorId}`
    );
  }

  getMyAvailabilityById(
    availabilityId: string
  ): Observable<DoctorAvailability> {

    return this.http.get<DoctorAvailability>(
      `${this.baseUrl}/provider/${availabilityId}`
    );
  }

  updateMyAvailability(
    availabilityId: string,
    request: UpdateDoctorAvailabilityRequest
  ): Observable<DoctorAvailability> {

    return this.http.put<DoctorAvailability>(
      `${this.baseUrl}/provider/${availabilityId}`,
      request
    );
  }

  updateMyAvailabilityStatus(
    availabilityId: string,
    request: UpdateAvailabilityStatusRequest
  ): Observable<DoctorAvailability> {

    return this.http.patch<DoctorAvailability>(
      `${this.baseUrl}/provider/${availabilityId}/status`,
      request
    );
  }

  deleteMyAvailability(
    availabilityId: string
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.baseUrl}/provider/${availabilityId}`
    );
  }

  getAvailableDoctorSchedules(
    doctorId: string
  ): Observable<DoctorAvailability[]> {

    return this.http.get<DoctorAvailability[]>(
      `${this.baseUrl}/available/doctor/${doctorId}`
    );
  }
}
