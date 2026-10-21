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
  ProviderOrganization
} from '../../provider/models/provider-organization';

@Injectable({
  providedIn: 'root'
})
export class AdminProviderOrganizationApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/providers';

  getAllProviders():
    Observable<ProviderOrganization[]> {

    return this.http.get<ProviderOrganization[]>(
      this.baseUrl
    );
  }

  getProviderById(
    providerId: string
  ): Observable<ProviderOrganization> {

    return this.http.get<ProviderOrganization>(
      `${this.baseUrl}/${providerId}`
    );
  }

  approveProvider(
    providerId: string,
    request: {
      reviewRemarks: string | null;
    }
  ): Observable<ProviderOrganization> {
  
    return this.http.post<ProviderOrganization>(
      `${this.baseUrl}/${providerId}/approve`,
      request
    );
  }
  
  rejectProvider(
    providerId: string,
    request: {
      rejectionReason: string;
      reviewRemarks: string | null;
    }
  ): Observable<ProviderOrganization> {
  
    return this.http.post<ProviderOrganization>(
      `${this.baseUrl}/${providerId}/reject`,
      request
    );
  }
  
  updateStatus(
    providerId: string,
    request: {
      status: string;
    }
  ): Observable<ProviderOrganization> {
  
    return this.http.patch<ProviderOrganization>(
      `${this.baseUrl}/${providerId}/status`,
      request
    );
  }
}