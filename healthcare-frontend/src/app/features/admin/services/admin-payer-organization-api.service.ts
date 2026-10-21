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
  PayerOrganization
} from '../../insurance/models/payer-organization';

@Injectable({
  providedIn: 'root'
})
export class AdminPayerOrganizationApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/payers';

  getAllPayers():
    Observable<PayerOrganization[]> {

    return this.http.get<PayerOrganization[]>(
      this.baseUrl
    );
  }

  getPayerById(
    payerId: string
  ): Observable<PayerOrganization> {

    return this.http.get<PayerOrganization>(
      `${this.baseUrl}/${payerId}`
    );
  }

  approvePayer(
    payerId: string,
    request: {
      reviewRemarks: string | null;
    }
  ): Observable<PayerOrganization> {
  
    return this.http.post<PayerOrganization>(
      `${this.baseUrl}/${payerId}/approve`,
      request
    );
  }
  
  rejectPayer(
    payerId: string,
    request: {
      rejectionReason: string;
      reviewRemarks: string | null;
    }
  ): Observable<PayerOrganization> {
  
    return this.http.post<PayerOrganization>(
      `${this.baseUrl}/${payerId}/reject`,
      request
    );
  }
  
  updateStatus(
    payerId: string,
    request: {
      status: string;
    }
  ): Observable<PayerOrganization> {
  
    return this.http.patch<PayerOrganization>(
      `${this.baseUrl}/${payerId}/status`,
      request
    );
  }
}
