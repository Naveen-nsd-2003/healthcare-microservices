// import {
//   inject
// } from '@angular/core';

// import {
//   CanActivateFn,
//   Router
// } from '@angular/router';

// import {
//   AuthService
// } from '../auth/auth.service';

// export const authGuard: CanActivateFn =
//   (
//     route,
//     state
//   ) => {

//     const authService =
//       inject(AuthService);

//     const router =
//       inject(Router);

//     if (
//       authService.isAuthenticated()
//     ) {

//       return true;
//     }

//     return router.createUrlTree(
//       ['/login'],
//       {
//         queryParams: {
//           returnUrl: state.url
//         }
//       }
//     );
//   };

import {
  inject
} from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  AuthService
} from '../auth/auth.service';

export const authGuard: CanActivateFn =
  (
    route,
    state
  ) => {

    const authService =
      inject(AuthService);

    const router =
      inject(Router);

    if (
      authService.validateSession()
    ) {

      return true;
    }

    return router.createUrlTree(
      ['/login'],
      {
        queryParams: {
          returnUrl: state.url
        }
      }
    );
  };