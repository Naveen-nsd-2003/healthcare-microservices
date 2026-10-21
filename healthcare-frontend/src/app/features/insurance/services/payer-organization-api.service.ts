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
  CreatePayerOrganizationRequest
} from '../models/create-payer-organization-request';

import {
  PayerOrganization
} from '../models/payer-organization';

import {
  UpdatePayerOrganizationRequest
} from '../models/update-payer-organization-request';

@Injectable({
  providedIn: 'root'
})
export class PayerOrganizationApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/payers/me';

  getMyOrganization():
    Observable<PayerOrganization> {

    return this.http.get<PayerOrganization>(
      this.baseUrl
    );
  }

  createMyOrganization(
    request: CreatePayerOrganizationRequest
  ): Observable<PayerOrganization> {

    return this.http.post<PayerOrganization>(
      this.baseUrl,
      request
    );
  }

  updateMyOrganization(
    request: UpdatePayerOrganizationRequest
  ): Observable<PayerOrganization> {

    return this.http.put<PayerOrganization>(
      this.baseUrl,
      request
    );
  }
}