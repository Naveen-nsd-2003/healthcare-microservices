import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import {
  inject
} from '@angular/core';

import {
  Router
} from '@angular/router';

import {
  catchError,
  throwError
} from 'rxjs';

import {
  TokenStorageService
} from '../auth/token-storage.service';

export const errorInterceptor: HttpInterceptorFn =
  (
    request,
    next
  ) => {

    const tokenStorage =
      inject(TokenStorageService);

    const router =
      inject(Router);

    return next(request).pipe(
      catchError(
        (
          error: HttpErrorResponse
        ) => {

          const loginRequest =
            request.url.includes(
              '/auth/login'
            );

          if (
            error.status === 401
            && !loginRequest
          ) {

            tokenStorage.clearSession();

            router.navigate(
              ['/login'],
              {
                queryParams: {
                  sessionExpired: true
                }
              }
            );
          }

          return throwError(
            () => error
          );
        }
      )
    );
  };