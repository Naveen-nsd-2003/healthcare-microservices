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
  AdminDashboard
} from '../models/admin-dashboard';

import {
  CreateStaffRequest
} from '../models/create-staff-request';

@Injectable({
  providedIn: 'root'
})
export class AdminApiService {

  private readonly http =
    inject(HttpClient);

  private readonly authBaseUrl =
    '/auth';

  getDashboard():
    Observable<AdminDashboard> {

    return this.http.get<AdminDashboard>(
      `${this.authBaseUrl}/admin/dashboard`
    );
  }

  createProvider(
    request: CreateStaffRequest
  ): Observable<string> {

    return this.http.post(
      `${this.authBaseUrl}/providers`,
      request,
      {
        responseType: 'text'
      }
    );
  }

  createPayer(
    request: CreateStaffRequest
  ): Observable<string> {

    return this.http.post(
      `${this.authBaseUrl}/payers`,
      request,
      {
        responseType: 'text'
      }
    );
  }
}