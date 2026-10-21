import {
  inject
} from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  TokenStorageService
} from '../auth/token-storage.service';

export const firstLoginGuard: CanActivateFn =
  () => {

    const tokenStorage =
      inject(TokenStorageService);

    const router =
      inject(Router);

    if (
      tokenStorage.isFirstLogin()
    ) {

      return router.createUrlTree([
        '/change-password'
      ]);
    }

    return true;
  };