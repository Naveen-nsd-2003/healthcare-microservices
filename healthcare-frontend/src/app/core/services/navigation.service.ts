import {
  Injectable
} from '@angular/core';

import {
  NavigationItem
} from '../../shared/models/navigation-item';

import {
  UserRole
} from '../models/user-role';

@Injectable({
  providedIn: 'root'
})
export class NavigationService {

  private readonly navigationItems:
    NavigationItem[] = [

    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/member/dashboard',
      roles: [
        'MEMBER'
      ],
      exact: true
    },

    {
      label: 'My Profile',
      icon: 'person',
      route: '/member/profile',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Insurance Plans',
      icon: 'health_and_safety',
      route: '/member/insurance-plans',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'My Enrollments',
      icon: 'assignment',
      route: '/member/enrollments',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'My Policies',
      icon: 'policy',
      route: '/member/policies',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Find Providers',
      icon: 'local_hospital',
      route: '/member/providers',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Appointments',
      icon: 'calendar_month',
      route: '/member/appointments',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Medical Records',
      icon: 'medical_information',
      route: '/member/medical-records',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Claims',
      icon: 'receipt_long',
      route: '/member/claims',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Notifications',
      icon: 'notifications',
      route: '/member/notifications',
      roles: [
        'MEMBER'
      ]
    },

    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/provider/dashboard',
      roles: [
        'PROVIDER'
      ],
      exact: true
    },

    {
      label: 'Organization',
      icon: 'business',
      route: '/provider/organization',
      roles: [
        'PROVIDER'
      ]
    },

    {
      label: 'Departments',
      icon: 'account_tree',
      route: '/provider/departments',
      roles: [
        'PROVIDER'
      ]
    },

  {
  label: 'Doctors',
  icon: 'medical_services',
  route: '/provider/doctors',
  roles: [
    'PROVIDER'
  ]
},

    {
      label: 'Availability',
      icon: 'schedule',
      route: '/provider/availability',
      roles: [
        'PROVIDER'
      ]
    },

    {
      label: 'Appointments',
      icon: 'calendar_month',
      route: '/provider/appointments',
      roles: [
        'PROVIDER'
      ]
    },

    {
      label: 'Medical Records',
      icon: 'medical_information',
      route: '/provider/medical-records',
      roles: [
        'PROVIDER'
      ]
    },

    {
      label: 'Claims',
      icon: 'receipt_long',
      route: '/provider/claims',
      roles: [
        'PROVIDER'
      ]
    },

    {
      label: 'Notifications',
      icon: 'notifications',
      route: '/provider/notifications',
      roles: [
        'PROVIDER'
      ]
    },

    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/payer/dashboard',
      roles: [
        'PAYER'
      ],
      exact: true
    },

    {
      label: 'Organization',
      icon: 'business',
      route: '/payer/organization',
      roles: [
        'PAYER'
      ]
    },

    {
      label: 'Insurance Plans',
      icon: 'health_and_safety',
      route: '/payer/insurance-plans',
      roles: [
        'PAYER'
      ]
    },

    {
      label: 'Enrollments',
      icon: 'assignment',
      route: '/payer/enrollments',
      roles: [
        'PAYER'
      ]
    },

    {
      label: 'Policies',
      icon: 'policy',
      route: '/payer/policies',
      roles: [
        'PAYER'
      ]
    },

    {
      label: 'Claims',
      icon: 'receipt_long',
      route: '/payer/claims',
      roles: [
        'PAYER'
      ]
    },

    {
      label: 'Notifications',
      icon: 'notifications',
      route: '/payer/notifications',
      roles: [
        'PAYER'
      ]
    },
    {
      label: 'Dashboard',
      icon: 'dashboard',
      route: '/admin/dashboard',
      roles: [
        'ADMIN'
      ],
      exact: true
    },
    
    {
      label: 'Create Provider',
      icon: 'add_business',
      route: '/admin/providers/create',
      roles: [
        'ADMIN'
      ]
    },
    
    {
      label: 'Create Payer',
      icon: 'account_balance',
      route: '/admin/payers/create',
      roles: [
        'ADMIN'
      ]
    },
    
    {
      label: 'Manage Providers',
      icon: 'local_hospital',
      route: '/admin/providers',
      roles: [
        'ADMIN'
      ]
    },
    
    {
      label: 'Manage Payers',
      icon: 'business',
      route: '/admin/payers',
      roles: [
        'ADMIN'
      ]
    },
    
    {
      label: 'Notifications',
      icon: 'notifications',
      route: '/admin/notifications',
      roles: [
        'ADMIN'
      ]
    }
    
    
  ];

  getNavigationItemsForRole(
    role: UserRole | null
  ): NavigationItem[] {

    if (!role) {
      return [];
    }

    return this.navigationItems.filter(
      item =>
        item.roles.includes(role)
    );
  }
}