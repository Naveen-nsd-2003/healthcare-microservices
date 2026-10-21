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
  MemberProfile
} from '../models/member-profile';

import {
  MemberProfileRequest
} from '../models/member-profile-request';

@Injectable({
  providedIn: 'root'
})
export class MemberProfileApiService {

  private readonly http =
    inject(HttpClient);

  private readonly baseUrl =
    '/api/members/me';

  getMyProfile():
    Observable<MemberProfile> {

    return this.http.get<MemberProfile>(
      this.baseUrl
    );
  }

  createMyProfile(
    request: MemberProfileRequest
  ): Observable<MemberProfile> {

    return this.http.post<MemberProfile>(
      this.baseUrl,
      request
    );
  }

  updateMyProfile(
    request: MemberProfileRequest
  ): Observable<MemberProfile> {

    return this.http.put<MemberProfile>(
      this.baseUrl,
      request
    );
  }
}