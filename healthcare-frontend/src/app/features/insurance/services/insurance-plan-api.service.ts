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
  CreateInsurancePlanRequest
} from '../models/create-insurance-plan-request';

import {
  InsurancePlan
} from '../models/insurance-plan';

import {
  UpdateInsurancePlanRequest
} from '../models/update-insurance-plan-request';

import {
  UpdateInsurancePlanStatusRequest
} from '../models/update-insurance-plan-status-request';

@Injectable({
  providedIn: 'root'
})
export class InsurancePlanApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/plans';

  createPlan(
    request: CreateInsurancePlanRequest
  ): Observable<InsurancePlan> {

    return this.http.post<InsurancePlan>(
      this.baseUrl,
      request
    );
  }

  getMyPlans():
    Observable<InsurancePlan[]> {

    return this.http.get<InsurancePlan[]>(
      `${this.baseUrl}/me`
    );
  }

  getMyPlanById(
    planId: string
  ): Observable<InsurancePlan> {

    return this.http.get<InsurancePlan>(
      `${this.baseUrl}/me/${planId}`
    );
  }

  updateMyPlan(
    planId: string,
    request: UpdateInsurancePlanRequest
  ): Observable<InsurancePlan> {

    return this.http.put<InsurancePlan>(
      `${this.baseUrl}/me/${planId}`,
      request
    );
  }

  updateMyPlanStatus(
    planId: string,
    request: UpdateInsurancePlanStatusRequest
  ): Observable<InsurancePlan> {

    return this.http.patch<InsurancePlan>(
      `${this.baseUrl}/me/${planId}/status`,
      request
    );
  }

  getAvailablePlans():
    Observable<InsurancePlan[]> {

    return this.http.get<InsurancePlan[]>(
      `${this.baseUrl}/available`
    );
  }

  getAvailablePlanById(
    planId: string
  ): Observable<InsurancePlan> {

    return this.http.get<InsurancePlan>(
      `${this.baseUrl}/available/${planId}`
    );
  }
}