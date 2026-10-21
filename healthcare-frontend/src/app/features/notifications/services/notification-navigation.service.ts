import {
    Injectable,
    inject
  } from '@angular/core';
  
  import {
    Router
  } from '@angular/router';
  
  import {
    AuthService
  } from '../../../core/auth/auth.service';
  
  import {
    UserRole
  } from '../../../core/models/user-role';
  
  import {
    Notification
  } from '../models/notification';
  
  @Injectable({
    providedIn: 'root'
  })
  export class NotificationNavigationService {
  
    private readonly router =
      inject(Router);
  
    private readonly authService =
      inject(AuthService);
  
    navigateToNotification(
      notification: Notification
    ): Promise<boolean> {
  
      const role =
        this.authService.currentRole();
  
      if (!role) {
        return Promise.resolve(false);
      }
  
      const route =
        this.resolveReferenceRoute(
          role,
          notification
        );
  
      if (route) {
        return this.router.navigate(route);
      }
  
      return this.router.navigate([
        this.resolveNotificationsRoute(role)
      ]);
    }
  
    private resolveReferenceRoute(
      role: UserRole,
      notification: Notification
    ): string[] | null {
  
      const referenceId =
        notification.referenceId?.trim();
  
      if (!referenceId) {
        return null;
      }
  
      switch (notification.referenceType) {
  
        case 'APPOINTMENT':
          return this.resolveAppointmentRoute(
            role,
            referenceId
          );
  
        case 'ENROLLMENT':
          return this.resolveEnrollmentRoute(
            role,
            referenceId
          );
  
        case 'POLICY':
          return this.resolvePolicyRoute(
            role,
            referenceId
          );
  
        case 'CLAIM':
          return this.resolveClaimRoute(
            role,
            referenceId
          );
  
        case 'MEDICAL_RECORD':
          return this.resolveMedicalRecordRoute(
            role,
            referenceId
          );
  
        default:
          return null;
      }
    }
  
    private resolveAppointmentRoute(
      role: UserRole,
      referenceId: string
    ): string[] | null {
  
      switch (role) {
  
        case 'MEMBER':
          return [
            '/member/appointments',
            referenceId
          ];
  
        case 'PROVIDER':
          return [
            '/provider/appointments',
            referenceId
          ];
  
        default:
          return null;
      }
    }
  
    private resolveEnrollmentRoute(
      role: UserRole,
      referenceId: string
    ): string[] | null {
  
      switch (role) {
  
        case 'MEMBER':
          return [
            '/member/enrollments',
            referenceId
          ];
  
        case 'PAYER':
          return [
            '/payer/enrollments',
            referenceId
          ];
  
        default:
          return null;
      }
    }
  
    private resolvePolicyRoute(
      role: UserRole,
      referenceId: string
    ): string[] | null {
  
      switch (role) {
  
        case 'MEMBER':
          return [
            '/member/policies',
            referenceId
          ];
  
        case 'PAYER':
          return [
            '/payer/policies',
            referenceId
          ];
  
        default:
          return null;
      }
    }
  
    private resolveClaimRoute(
      role: UserRole,
      referenceId: string
    ): string[] | null {
  
      switch (role) {
  
        case 'MEMBER':
          return [
            '/member/claims',
            referenceId
          ];
  
        case 'PROVIDER':
          return [
            '/provider/claims',
            referenceId
          ];
  
        case 'PAYER':
          return [
            '/payer/claims',
            referenceId
          ];
  
        default:
          return null;
      }
    }
  
    private resolveMedicalRecordRoute(
      role: UserRole,
      referenceId: string
    ): string[] | null {
  
      switch (role) {
  
        case 'MEMBER':
          return [
            '/member/medical-records',
            referenceId
          ];
  
        case 'PROVIDER':
          return [
            '/provider/medical-records',
            referenceId
          ];
  
        default:
          return null;
      }
    }
  
    private resolveNotificationsRoute(
      role: UserRole
    ): string {
  
      switch (role) {
  
        case 'MEMBER':
          return '/member/notifications';
  
        case 'PROVIDER':
          return '/provider/notifications';
  
        case 'PAYER':
          return '/payer/notifications';
  
        case 'ADMIN':
          return '/admin/notifications';
  
        default:
          return '/login';
      }
    }
  }