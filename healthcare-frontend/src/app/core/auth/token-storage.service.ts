import {
  Injectable,
  inject
} from '@angular/core';

import {
  JwtService
} from './jwt.service';

import {
  LoginResponse
} from '../models/login-response';

import {
  UserRole
} from '../models/user-role';

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {

  private readonly jwtService =
    inject(JwtService);

  private readonly accessTokenKey =
    'healthcare_access_token';

  private readonly tokenTypeKey =
    'healthcare_token_type';

  private readonly emailKey =
    'healthcare_authenticated_email';

  private readonly roleKey =
    'healthcare_authenticated_role';

  private readonly firstLoginKey =
    'healthcare_first_login';

  saveSession(
    response: LoginResponse
  ): void {

    sessionStorage.setItem(
      this.accessTokenKey,
      response.accessToken
    );

    sessionStorage.setItem(
      this.tokenTypeKey,
      response.tokenType
    );

    sessionStorage.setItem(
      this.emailKey,
      response.email
    );

    sessionStorage.setItem(
      this.roleKey,
      response.role
    );

    sessionStorage.setItem(
      this.firstLoginKey,
      String(response.firstLogin)
    );
  }

  getAccessToken(): string | null {

    return sessionStorage.getItem(
      this.accessTokenKey
    );
  }

  getValidAccessToken(): string | null {

    const token =
      this.getAccessToken();

    if (!token) {
      return null;
    }

    if (
      this.jwtService.isTokenExpired(
        token
      )
    ) {

      this.clearSession();

      return null;
    }

    return token;
  }

  getTokenType(): string {

    return sessionStorage.getItem(
      this.tokenTypeKey
    ) ?? 'Bearer';
  }

  getEmail(): string | null {

    return sessionStorage.getItem(
      this.emailKey
    );
  }

  getRole(): UserRole | null {

    const storedRole =
      sessionStorage.getItem(
        this.roleKey
      );

    if (
      storedRole === 'ADMIN'
      || storedRole === 'MEMBER'
      || storedRole === 'PROVIDER'
      || storedRole === 'PAYER'
    ) {

      return storedRole;
    }

    return null;
  }

  isFirstLogin(): boolean {

    return sessionStorage.getItem(
      this.firstLoginKey
    ) === 'true';
  }

  hasValidSession(): boolean {

    const token =
      this.getValidAccessToken();

    const role =
      this.getRole();

    const email =
      this.getEmail();

    return token !== null
      && role !== null
      && email !== null
      && email.trim().length > 0;
  }

  getTokenExpirationDate(): Date | null {

    const token =
      this.getAccessToken();

    if (!token) {
      return null;
    }

    return this.jwtService
      .getExpirationDate(token);
  }

  clearSession(): void {

    sessionStorage.removeItem(
      this.accessTokenKey
    );

    sessionStorage.removeItem(
      this.tokenTypeKey
    );

    sessionStorage.removeItem(
      this.emailKey
    );

    sessionStorage.removeItem(
      this.roleKey
    );

    sessionStorage.removeItem(
      this.firstLoginKey
    );
  }
}


// localStorage.setItem(...)
// localStorage.getItem(...)
// localStorage.removeItem(...)