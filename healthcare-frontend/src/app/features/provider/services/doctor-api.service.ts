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
  CreateDoctorRequest
} from '../models/create-doctor-request';

import {
  Doctor
} from '../models/doctor';

import {
  UpdateDoctorRequest
} from '../models/update-doctor-request';

import {
  UpdateDoctorStatusRequest
} from '../models/update-doctor-status-request';

@Injectable({
  providedIn: 'root'
})
export class DoctorApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/doctors';

  createDoctor(
    request: CreateDoctorRequest
  ): Observable<Doctor> {

    return this.http.post<Doctor>(
      this.baseUrl,
      request
    );
  }

  getMyDoctors():
    Observable<Doctor[]> {

    return this.http.get<Doctor[]>(
      `${this.baseUrl}/provider`
    );
  }

  getMyDoctorById(
    doctorId: string
  ): Observable<Doctor> {

    return this.http.get<Doctor>(
      `${this.baseUrl}/provider/${doctorId}`
    );
  }

  updateMyDoctor(
    doctorId: string,
    request: UpdateDoctorRequest
  ): Observable<Doctor> {

    return this.http.put<Doctor>(
      `${this.baseUrl}/provider/${doctorId}`,
      request
    );
  }

  updateMyDoctorStatus(
    doctorId: string,
    request: UpdateDoctorStatusRequest
  ): Observable<Doctor> {

    return this.http.patch<Doctor>(
      `${this.baseUrl}/provider/${doctorId}/status`,
      request
    );
  }

  getAvailableDoctors():
    Observable<Doctor[]> {

    return this.http.get<Doctor[]>(
      `${this.baseUrl}/available`
    );
  }

  getAvailableDoctorsByProvider(
    providerId: string
  ): Observable<Doctor[]> {

    return this.http.get<Doctor[]>(
      `${this.baseUrl}/available/provider/${providerId}`
    );
  }

  getAvailableDoctorsByDepartment(
    departmentId: string
  ): Observable<Doctor[]> {

    return this.http.get<Doctor[]>(
      `${this.baseUrl}/available/department/${departmentId}`
    );
  }

  getAvailableDoctorById(
    doctorId: string
  ): Observable<Doctor> {

    return this.http.get<Doctor>(
      `${this.baseUrl}/available/${doctorId}`
    );
  }
}