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
  InsurancePolicy
} from '../models/insurance-policy';

import {
  UpdatePolicyStatusRequest
} from '../models/update-policy-status-request';

@Injectable({
  providedIn: 'root'
})
export class PolicyApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/policies';

  getMyPolicies():
    Observable<InsurancePolicy[]> {

    return this.http.get<InsurancePolicy[]>(
      `${this.baseUrl}/me`
    );
  }

  getMyPolicyById(
    policyId: string
  ): Observable<InsurancePolicy> {

    return this.http.get<InsurancePolicy>(
      `${this.baseUrl}/me/${policyId}`
    );
  }

  getPayerPolicies():
    Observable<InsurancePolicy[]> {

    return this.http.get<InsurancePolicy[]>(
      `${this.baseUrl}/payer`
    );
  }

  getPayerPolicyById(
    policyId: string
  ): Observable<InsurancePolicy> {

    return this.http.get<InsurancePolicy>(
      `${this.baseUrl}/payer/${policyId}`
    );
  }

  updatePolicyStatus(
    policyId: string,
    request: UpdatePolicyStatusRequest
  ): Observable<InsurancePolicy> {

    return this.http.patch<InsurancePolicy>(
      `${this.baseUrl}/payer/${policyId}/status`,
      request
    );
  }
}