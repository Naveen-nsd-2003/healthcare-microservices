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
  CreateEnrollmentRequest
} from '../models/create-enrollment-request';

import {
  Enrollment
} from '../models/enrollment';

import {
  ReviewEnrollmentRequest
} from '../models/review-enrollment-request';

@Injectable({
  providedIn: 'root'
})
export class EnrollmentApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/enrollments';

  createEnrollment(
    request: CreateEnrollmentRequest
  ): Observable<Enrollment> {

    return this.http.post<Enrollment>(
      this.baseUrl,
      request
    );
  }

  getMyEnrollments():
    Observable<Enrollment[]> {

    return this.http.get<Enrollment[]>(
      `${this.baseUrl}/me`
    );
  }

  getMyEnrollmentById(
    enrollmentId: string
  ): Observable<Enrollment> {

    return this.http.get<Enrollment>(
      `${this.baseUrl}/me/${enrollmentId}`
    );
  }

  cancelMyEnrollment(
    enrollmentId: string
  ): Observable<Enrollment> {

    return this.http.patch<Enrollment>(
      `${this.baseUrl}/me/${enrollmentId}/cancel`,
      null
    );
  }

  getPayerEnrollments():
    Observable<Enrollment[]> {

    return this.http.get<Enrollment[]>(
      `${this.baseUrl}/payer`
    );
  }

  getPayerEnrollmentById(
    enrollmentId: string
  ): Observable<Enrollment> {

    return this.http.get<Enrollment>(
      `${this.baseUrl}/payer/${enrollmentId}`
    );
  }

  approveEnrollment(
    enrollmentId: string,
    request: ReviewEnrollmentRequest
  ): Observable<Enrollment> {

    return this.http.patch<Enrollment>(
      `${this.baseUrl}/payer/${enrollmentId}/approve`,
      request
    );
  }

  rejectEnrollment(
    enrollmentId: string,
    request: ReviewEnrollmentRequest
  ): Observable<Enrollment> {

    return this.http.patch<Enrollment>(
      `${this.baseUrl}/payer/${enrollmentId}/reject`,
      request
    );
  }
}