import {
  inject
} from '@angular/core';

import {
  HttpErrorResponse
} from '@angular/common/http';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  catchError,
  map,
  of
} from 'rxjs';

import {
  MemberProfileApiService
} from '../../features/member/services/member-profile-api.service';

import {
  NotificationMessageService
} from '../services/notification-message.service';

export const memberProfileGuard:
  CanActivateFn = (
    route,
    state
  ) => {

    const profileApi =
      inject(MemberProfileApiService);

    const router =
      inject(Router);

    const notificationMessage =
      inject(NotificationMessageService);

    return profileApi
      .getMyProfile()
      .pipe(
        map(
          profile => {

            if (
              profile.status === 'ACTIVE'
            ) {

              return true;
            }

            notificationMessage
              .showError(
                'Your Member profile must be active '
                + 'before using this healthcare service.'
              );

            return router.createUrlTree(
              ['/member/profile'],
              {
                queryParams: {
                  profileStatus:
                    profile.status,
                  returnUrl:
                    state.url
                }
              }
            );
          }
        ),

        catchError(
          (
            error: HttpErrorResponse
          ) => {

            if (error.status === 404) {

              notificationMessage
                .showInformation(
                  'Complete your healthcare profile '
                  + 'before continuing.'
                );

              return of(
                router.createUrlTree(
                  ['/member/profile'],
                  {
                    queryParams: {
                      profileRequired:
                        true,
                      returnUrl:
                        state.url
                    }
                  }
                )
              );
            }

            if (
              error.status === 401
              || error.status === 403
            ) {

              return of(
                router.createUrlTree([
                  '/login'
                ])
              );
            }

            notificationMessage
              .showError(
                'Unable to verify your Member profile. '
                + 'Please try again.'
              );

            return of(
              router.createUrlTree([
                '/member/profile'
              ])
            );
          }
        )
      );
  };