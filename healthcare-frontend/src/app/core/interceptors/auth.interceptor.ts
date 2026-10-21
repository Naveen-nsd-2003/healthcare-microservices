
import {
  HttpInterceptorFn
} from '@angular/common/http';

import {
  inject
} from '@angular/core';

import {
  TokenStorageService
} from '../auth/token-storage.service';

export const authInterceptor: HttpInterceptorFn =
  (
    request,
    next
  ) => {

    const tokenStorage =
      inject(TokenStorageService);

    const accessToken =
      tokenStorage.getValidAccessToken();

    if (!accessToken) {

      return next(request);
    }

    const authenticatedRequest =
      request.clone({
        setHeaders: {
          Authorization:
            `${tokenStorage.getTokenType()} ${accessToken}`
        }
      });

    return next(
      authenticatedRequest
    );
  };