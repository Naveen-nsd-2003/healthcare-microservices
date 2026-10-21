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
  CreateDepartmentRequest
} from '../models/create-department-request';

import {
  Department
} from '../models/department';

import {
  UpdateDepartmentRequest
} from '../models/update-department-request';

import {
  UpdateDepartmentStatusRequest
} from '../models/update-department-status-request';

@Injectable({
  providedIn: 'root'
})
export class DepartmentApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/departments';

  createDepartment(
    request: CreateDepartmentRequest
  ): Observable<Department> {

    return this.http.post<Department>(
      this.baseUrl,
      request
    );
  }

  getMyDepartments():
    Observable<Department[]> {

    return this.http.get<Department[]>(
      `${this.baseUrl}/provider`
    );
  }

  getMyDepartmentById(
    departmentId: string
  ): Observable<Department> {

    return this.http.get<Department>(
      `${this.baseUrl}/provider/${departmentId}`
    );
  }

  updateMyDepartment(
    departmentId: string,
    request: UpdateDepartmentRequest
  ): Observable<Department> {

    return this.http.put<Department>(
      `${this.baseUrl}/provider/${departmentId}`,
      request
    );
  }

  updateMyDepartmentStatus(
    departmentId: string,
    request: UpdateDepartmentStatusRequest
  ): Observable<Department> {

    return this.http.patch<Department>(
      `${this.baseUrl}/provider/${departmentId}/status`,
      request
    );
  }

  getAvailableDepartmentsByProvider(
    providerId: string
  ): Observable<Department[]> {

    return this.http.get<Department[]>(
      `${this.baseUrl}/available/provider/${providerId}`
    );
  }

  getAvailableDepartmentById(
    departmentId: string
  ): Observable<Department> {

    return this.http.get<Department>(
      `${this.baseUrl}/available/${departmentId}`
    );
  }
}