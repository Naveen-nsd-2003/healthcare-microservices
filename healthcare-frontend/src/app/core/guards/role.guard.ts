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

import {
  UserRole
} from '../models/user-role';

export const roleGuard: CanActivateFn =
  route => {

    const authService =
      inject(AuthService);

    const router =
      inject(Router);

    const currentRole =
      authService.currentRole();

    const allowedRoles =
      route.data['roles'] as
        UserRole[] | undefined;

    if (!currentRole) {

      return router.createUrlTree([
        '/login'
      ]);
    }

    if (
      allowedRoles?.includes(
        currentRole
      )
    ) {

      return true;
    }

    return router.createUrlTree(
      [
        getDashboardRoute(
          currentRole
        )
      ]
    );
  };

function getDashboardRoute(
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