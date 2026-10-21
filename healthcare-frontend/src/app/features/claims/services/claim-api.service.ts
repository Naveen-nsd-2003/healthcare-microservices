import {
  Injectable,
  inject
} from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import {
  Observable
} from 'rxjs';

import {
  ApproveClaimRequest
} from '../models/approve-claim-request';

import {
  CancelClaimRequest
} from '../models/cancel-claim-request';

import {
  Claim
} from '../models/claim';

import {
  ClaimSubmissionContext
} from '../models/claim-submission-context';

import {
  RejectClaimRequest
} from '../models/reject-claim-request';

import {
  SubmitClaimRequest
} from '../models/submit-claim-request';

@Injectable({
  providedIn: 'root'
})
export class ClaimApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/claims';

  getProviderClaimSubmissionContext(
    appointmentId: string,
    medicalRecordId: string
  ): Observable<ClaimSubmissionContext> {

    const params =
      new HttpParams()
        .set(
          'appointmentId',
          appointmentId
        )
        .set(
          'medicalRecordId',
          medicalRecordId
        );

    return this.http.get<ClaimSubmissionContext>(
      `${this.baseUrl}/provider/submission-context`,
      {
        params
      }
    );
  }

  submitClaim(
    request: SubmitClaimRequest
  ): Observable<Claim> {

    return this.http.post<Claim>(
      this.baseUrl,
      request
    );
  }

  getProviderClaims():
    Observable<Claim[]> {

    return this.http.get<Claim[]>(
      `${this.baseUrl}/provider`
    );
  }

  getProviderClaimById(
    claimId: string
  ): Observable<Claim> {

    return this.http.get<Claim>(
      `${this.baseUrl}/provider/${claimId}`
    );
  }

  cancelProviderClaim(
    claimId: string,
    request: CancelClaimRequest
  ): Observable<Claim> {

    return this.http.patch<Claim>(
      `${this.baseUrl}/provider/${claimId}/cancel`,
      request
    );
  }

  getPayerClaims():
    Observable<Claim[]> {

    return this.http.get<Claim[]>(
      `${this.baseUrl}/payer`
    );
  }

  getPayerClaimById(
    claimId: string
  ): Observable<Claim> {

    return this.http.get<Claim>(
      `${this.baseUrl}/payer/${claimId}`
    );
  }

  startPayerClaimReview(
    claimId: string
  ): Observable<Claim> {

    return this.http.patch<Claim>(
      `${this.baseUrl}/payer/${claimId}/review`,
      null
    );
  }

  approveClaim(
    claimId: string,
    request: ApproveClaimRequest
  ): Observable<Claim> {

    return this.http.patch<Claim>(
      `${this.baseUrl}/payer/${claimId}/approve`,
      request
    );
  }

  rejectClaim(
    claimId: string,
    request: RejectClaimRequest
  ): Observable<Claim> {

    return this.http.patch<Claim>(
      `${this.baseUrl}/payer/${claimId}/reject`,
      request
    );
  }

  getMyClaims():
    Observable<Claim[]> {

    return this.http.get<Claim[]>(
      `${this.baseUrl}/me`
    );
  }

  getMyClaimById(
    claimId: string
  ): Observable<Claim> {

    return this.http.get<Claim>(
      `${this.baseUrl}/me/${claimId}`
    );
  }
}