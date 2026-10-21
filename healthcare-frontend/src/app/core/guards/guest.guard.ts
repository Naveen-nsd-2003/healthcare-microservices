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

// import {
//   UserRole
// } from '../models/user-role';

// export const guestGuard: CanActivateFn =
//   () => {

//     const authService =
//       inject(AuthService);

//     const router =
//       inject(Router);

//     if (
//       !authService.isAuthenticated()
//     ) {

//       return true;
//     }

//     const role =
//       authService.currentRole();

//     if (!role) {

//       authService.logout();

//       return router.createUrlTree([
//         '/login'
//       ]);
//     }

//     return router.createUrlTree([
//       getDashboardRoute(role)
//     ]);
//   };

// function getDashboardRoute(
//   role: UserRole
// ): string {

//   switch (role) {

//     case 'MEMBER':
//       return '/member/dashboard';

//     case 'PROVIDER':
//       return '/provider/dashboard';

//     case 'PAYER':
//       return '/payer/dashboard';

//     case 'ADMIN':
//       return '/admin/dashboard';
//   }
// }

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

export const guestGuard: CanActivateFn =
  () => {

    const authService =
      inject(AuthService);

    const router =
      inject(Router);

    if (
      !authService.validateSession()
    ) {

      return true;
    }

    const role =
      authService.currentRole();

    if (!role) {

      authService.logout();

      return router.createUrlTree([
        '/login'
      ]);
    }

    return router.createUrlTree([
      authService.getDashboardRoute(
        role
      )
    ]);
  };