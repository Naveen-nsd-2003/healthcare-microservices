import {
  Injectable,
  computed,
  inject,
  signal
} from '@angular/core';

import {
  HttpClient
} from '@angular/common/http';

import {
  Router
} from '@angular/router';

import {
  Observable,
  tap
} from 'rxjs';

import {
  LoginRequest
} from '../models/login-request';

import {
  LoginResponse
} from '../models/login-response';

import {
  UserRole
} from '../models/user-role';

import {
  TokenStorageService
} from './token-storage.service';


import {
  RegisterRequest
} from '../models/auth/register-request';

import {
  VerifyOtpRequest
} from '../models/auth/verify-otp-request';

import {
  ChangePasswordRequest
} from '../models/auth/change-password-request';

import {
  ForgotPasswordRequest
} from '../models/auth/forgot-password-request';

import {
  ResetPasswordRequest
} from '../models/auth/reset-password-request';

import {
  CreateStaffRequest
} from '../models/auth/create-staff-request';
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly http =
    inject(HttpClient);

  private readonly router =
    inject(Router);

  private readonly tokenStorage =
    inject(TokenStorageService);

  private readonly authenticatedState =
    signal(
      this.tokenStorage.hasValidSession()
    );

  private readonly currentRoleState =
    signal<UserRole | null>(
      this.tokenStorage.getRole()
    );

  private readonly currentEmailState =
    signal<string | null>(
      this.tokenStorage.getEmail()
    );

  readonly isAuthenticated =
    computed(
      () => this.authenticatedState()
    );

  readonly currentRole =
    computed(
      () => this.currentRoleState()
    );

  readonly currentEmail =
    computed(
      () => this.currentEmailState()
    );

  login(
    request: LoginRequest
  ): Observable<LoginResponse> {

    return this.http
      .post<LoginResponse>(
        '/auth/login',
        request
      )
      .pipe(
        tap(response => {

          this.tokenStorage.saveSession(
            response
          );

          this.authenticatedState.set(
            true
          );

          this.currentRoleState.set(
            response.role
          );

          this.currentEmailState.set(
            response.email
          );
        })
      );
  }

  validateSession(): boolean {

    const sessionValid =
      this.tokenStorage.hasValidSession();

    if (!sessionValid) {

      this.clearAuthenticationState();

      return false;
    }

    this.authenticatedState.set(
      true
    );

    this.currentRoleState.set(
      this.tokenStorage.getRole()
    );

    this.currentEmailState.set(
      this.tokenStorage.getEmail()
    );

    return true;
  }

  navigateToRoleDashboard(
    role: UserRole
  ): void {

    this.router.navigate([
      this.getDashboardRoute(role)
    ]);
  }

  getDashboardRoute(
    role: UserRole
  ): string {

    switch (role) {

      case 'MEMBER':
        return '/member/dashboard';

      case 'PROVIDER':
        return '/provider/dashboard';

      case 'PAYER':
        return '/payer/dashboard';

      case 'ADMIN':
        return '/admin/dashboard';
    }
  }

  restoreSession(): void {

    this.validateSession();
  }

  logout(): void {

    this.clearAuthenticationState();

    this.router.navigate([
      '/login'
    ]);
  }

  logoutWithReturnUrl(
    returnUrl: string
  ): void {

    this.clearAuthenticationState();

    this.router.navigate(
      ['/login'],
      {
        queryParams: {
          returnUrl
        }
      }
    );
  }

  completePasswordChange(): void {

  this.clearAuthenticationState();

  this.router.navigate(
    ['/login'],
    {
      queryParams: {
        passwordChanged: true
      }
    }
  );
}

  private clearAuthenticationState(): void {

    this.tokenStorage.clearSession();

    this.authenticatedState.set(
      false
    );

    this.currentRoleState.set(
      null
    );

    this.currentEmailState.set(
      null
    );
  }


  register(
  request: RegisterRequest
): Observable<string> {

  return this.http.post(
    '/auth/register',
    request,
    {
      responseType: 'text'
    }
  );
}

verifyOtp(
  request: VerifyOtpRequest
): Observable<string> {

  return this.http.post(
    '/auth/verify-otp',
    request,
    {
      responseType: 'text'
    }
  );
}

changePassword(
  request: ChangePasswordRequest
): Observable<string> {

  return this.http.post(
    '/auth/change-password',
    request,
    {
      responseType: 'text'
    }
  );
}

forgotPassword(
  request: ForgotPasswordRequest
): Observable<string> {

  return this.http.post(
    '/auth/forgot-password',
    request,
    {
      responseType: 'text'
    }
  );
}

resetPassword(
  request: ResetPasswordRequest
): Observable<string> {

  return this.http.post(
    '/auth/reset-password',
    request,
    {
      responseType: 'text'
    }
  );
}


createProvider(
  request: CreateStaffRequest
): Observable<string> {

  return this.http.post(
    '/auth/providers',
    request,
    {
      responseType: 'text'
    }
  );
}

createPayer(
  request: CreateStaffRequest
): Observable<string> {

  return this.http.post(
    '/auth/payers',
    request,
    {
      responseType: 'text'
    }
  );
}

}