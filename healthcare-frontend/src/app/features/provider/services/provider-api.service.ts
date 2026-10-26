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
  CreateProviderOrganizationRequest
} from '../models/create-provider-organization-request';

import {
  ProviderOrganization
} from '../models/provider-organization';

import {
  UpdateProviderOrganizationRequest
} from '../models/update-provider-organization-request';

@Injectable({
  providedIn: 'root'
})
export class ProviderApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/providers';

  getAvailableProviders():
    Observable<ProviderOrganization[]> {

    return this.http.get<ProviderOrganization[]>(
      `${this.baseUrl}/available`
    );
  }

  getAvailableProviderById(
    providerId: string
  ): Observable<ProviderOrganization> {

    return this.http.get<ProviderOrganization>(
      `${this.baseUrl}/available/${providerId}`
    );
  }

  getMyProviderOrganization():
    Observable<ProviderOrganization> {

    return this.http.get<ProviderOrganization>(
      `${this.baseUrl}/me`
    );
  }
  

  createMyProviderOrganization(
    request: CreateProviderOrganizationRequest
  ): Observable<ProviderOrganization> {

    return this.http.post<ProviderOrganization>(
      `${this.baseUrl}/me`,
      request
    );
  }

  updateMyProviderOrganization(
    request: UpdateProviderOrganizationRequest
  ): Observable<ProviderOrganization> {

    return this.http.put<ProviderOrganization>(
      `${this.baseUrl}/me`,
      request
    );
  }
}