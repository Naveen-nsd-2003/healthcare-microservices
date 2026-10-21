import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';
import { roleGuard } from './core/guards/role.guard';

import { PublicLayout } from './layouts/public-layout/public-layout';
import { DashboardLayout } from './layouts/dashboard-layout/dashboard-layout';

import { Login } from './features/auth/pages/login/login';
import { Register } from './features/auth/pages/register/register';
import { VerifyOtp } from './features/auth/pages/verify-otp/verify-otp';
import { ChangePassword } from './features/auth/pages/change-password/change-password';
import { ForgotPassword } from './features/auth/pages/forgot-password/forgot-password';
import { ResetPassword } from './features/auth/pages/reset-password/reset-password';

import { MemberDashboard } from './features/member/pages/member-dashboard/member-dashboard';
import { ProviderDashboard } from './features/provider/pages/provider-dashboard/provider-dashboard';
import { PayerDashboard } from './features/payer/pages/payer-dashboard/payer-dashboard';
import { AdminDashboard } from './features/admin/pages/admin-dashboard/admin-dashboard';
import {
  firstLoginGuard
} from './core/guards/first-login.guard';

import {
  NotificationsPage
} from './features/notifications/pages/notifications-page/notifications-page';

import {
  MemberProfilePage
} from './features/member/pages/member-profile/member-profile';

import {
  memberProfileGuard
} from './core/guards/member-profile.guard';

import {
  AvailablePlans
} from './features/insurance/pages/available-plans/available-plans';

import {
  PlanDetails
} from './features/insurance/pages/plan-details/plan-details';

import {
  CreateEnrollment
} from './features/insurance/pages/create-enrollment/create-enrollment';

import {
  MyEnrollments
} from './features/insurance/pages/my-enrollments/my-enrollments';

import {
  EnrollmentDetails
} from './features/insurance/pages/enrollment-details/enrollment-details';

import {
  PayerEnrollments
} from './features/insurance/pages/payer-enrollments/payer-enrollments';

import {
  PayerEnrollmentDetails
} from './features/insurance/pages/payer-enrollment-details/payer-enrollment-details';

import {
  MyPolicies
} from './features/insurance/pages/my-policies/my-policies';

import {
  PolicyDetails
} from './features/insurance/pages/policy-details/policy-details';

import {
  PayerPolicies
} from './features/insurance/pages/payer-policies/payer-policies';

import {
  PayerPolicyDetails
} from './features/insurance/pages/payer-policy-details/payer-policy-details';

import {
  PayerOrganizationPage
} from './features/insurance/pages/payer-organization/payer-organization';

import {
  PayerInsurancePlans
} from './features/insurance/pages/payer-insurance-plans/payer-insurance-plans';

import {
  CreateInsurancePlan
} from './features/insurance/pages/create-insurance-plan/create-insurance-plan';

import {
  PayerPlanDetails
} from './features/insurance/pages/payer-plan-details/payer-plan-details';

import {
  EditInsurancePlan
} from './features/insurance/pages/edit-insurance-plan/edit-insurance-plan';

import {
  MemberProviders
} from './features/provider/pages/member-providers/member-providers';

import {
  MemberProviderDetails
} from './features/provider/pages/member-provider-details/member-provider-details';

import {
  MemberDoctorDetails
} from './features/provider/pages/member-doctor-details/member-doctor-details';

import {
  BookAppointment
} from './features/appointment/pages/book-appointment/book-appointment';

import {
  MemberAppointments
} from './features/appointment/pages/member-appointments/member-appointments';

import {
  MemberAppointmentDetails
} from './features/appointment/pages/member-appointment-details/member-appointment-details';


import {
  ProviderOrganizationPage
} from './features/provider/pages/provider-organization/provider-organization';

import {
  ProviderDepartments
} from './features/provider/pages/provider-departments/provider-departments';

import {
  CreateDepartment
} from './features/provider/pages/create-department/create-department';

import {
  ProviderDepartmentDetails
} from './features/provider/pages/provider-department-details/provider-department-details';

import {
  EditDepartment
} from './features/provider/pages/edit-department/edit-department';

import {
  ProviderDoctors
} from './features/provider/pages/provider-doctors/provider-doctors';

import {
  CreateDoctor
} from './features/provider/pages/create-doctor/create-doctor';

import {
  ProviderDoctorDetails
} from './features/provider/pages/provider-doctor-details/provider-doctor-details';

import {
  EditDoctor
} from './features/provider/pages/edit-doctor/edit-doctor';

import {
  ProviderAvailability
} from './features/provider/pages/provider-availability/provider-availability';

import {
  CreateAvailability
} from './features/provider/pages/create-availability/create-availability';

import {
  EditAvailability
} from './features/provider/pages/edit-availability/edit-availability';

import {
  ProviderAppointments
} from './features/appointment/pages/provider-appointments/provider-appointments';

import {
  ProviderAppointmentDetails
} from './features/appointment/pages/provider-appointment-details/provider-appointment-details';

import {
  CreateMedicalRecord
} from './features/medical-records/pages/create-medical-record/create-medical-record';

import {
  ProviderMedicalRecordDetails
} from './features/medical-records/pages/provider-medical-record-details/provider-medical-record-details';

import {
  ProviderMedicalRecords
} from './features/medical-records/pages/provider-medical-records/provider-medical-records';

import {
  EditMedicalRecord
} from './features/medical-records/pages/edit-medical-record/edit-medical-record';

import {
  MemberMedicalRecords
} from './features/medical-records/pages/member-medical-records/member-medical-records';


import {
  MemberMedicalRecordDetails
} from './features/medical-records/pages/member-medical-record-details/member-medical-record-details';

import {
  SubmitClaim
} from './features/claims/pages/submit-claim/submit-claim';

import {
  ProviderClaims
} from './features/claims/pages/provider-claims/provider-claims';

import {
  ProviderClaimDetails
} from './features/claims/pages/provider-claim-details/provider-claim-details';

import {
  PayerClaims
} from './features/claims/pages/payer-claims/payer-claims';

import {
  PayerClaimDetails
} from './features/claims/pages/payer-claim-details/payer-claim-details';

import {
  MemberClaims
} from './features/claims/pages/member-claims/member-claims';
import {
  MemberClaimDetails
} from './features/claims/pages/member-claim-details/member-claim-details';
import {
  CreateProvider
} from './features/admin/pages/create-provider/create-provider';


import {
  AdminPayers
} from './features/admin/pages/admin-payers/admin-payers';

import {
  AdminProviders
} from './features/admin/pages/admin-providers/admin-providers';

import {
  AdminPayerDetails
} from './features/admin/pages/admin-payer-details/admin-payer-details';

import {
  AdminProviderDetails
} from './features/admin/pages/admin-provider-details/admin-provider-details';

import {
  CreatePayer
} from './features/admin/pages/create-payer/create-payer';
import { NotFound } from './shared/components/not-found/not-found';
export const routes: Routes = [
  {
    path: '',
    component: PublicLayout,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login'
      },
      {
        path: 'login',
        component: Login,
        canActivate: [
          guestGuard
        ],
        title: 'Login | Healthcare Portal'
      },
      {
        path: 'register',
        component: Register,
        canActivate: [
          guestGuard
        ],
        title: 'Register | Healthcare Portal'
      },
      {
        path: 'verify-otp',
        component: VerifyOtp,
        canActivate: [
          guestGuard
        ],
        title: 'Verify OTP | Healthcare Portal'
      },
      {
        path: 'forgot-password',
        component: ForgotPassword,
        canActivate: [
          guestGuard
        ],
        title: 'Forgot Password | Healthcare Portal'
      },
      {
        path: 'reset-password',
        component: ResetPassword,
        canActivate: [
          guestGuard
        ],
        title: 'Reset Password | Healthcare Portal'
      },
      {
        path: 'change-password',
        component: ChangePassword,
        canActivate: [
          authGuard
        ],
        title: 'Change Password | Healthcare Portal'
      }
    ]
  },
  {
    path: '',
    component: DashboardLayout,
    canActivate: [
      authGuard,
      firstLoginGuard
    ],
    children: [
      {
        path: 'member/dashboard',
        component: MemberDashboard,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Member Dashboard'
      },
      {
        path: 'provider/dashboard',
        component: ProviderDashboard,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Provider Dashboard'
      },
      {
        path: 'payer/dashboard',
        component: PayerDashboard,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Payer Dashboard'
      },
      {
        path: 'admin/dashboard',
        component: AdminDashboard,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Admin Dashboard'
      },
      {
        path: 'member/notifications',
        component: NotificationsPage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Notifications | Healthcare Portal'
      },
      {
        path: 'provider/notifications',
        component: NotificationsPage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Notifications | Healthcare Portal'
      },
      {
        path: 'payer/notifications',
        component: NotificationsPage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Notifications | Healthcare Portal'
      },
      {
        path: 'admin/notifications',
        component: NotificationsPage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Notifications | Healthcare Portal'
      },
      {
        path: 'member/profile',
        component: MemberProfilePage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'My Profile | Healthcare Portal'
      },
      {
        path: 'member/insurance-plans',
        component: AvailablePlans,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Insurance Plans | Healthcare Portal'
      },
      {
        path: 'payer/insurance-plans/new',
        component: CreateInsurancePlan,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Create Insurance Plan | Healthcare Portal'
      },
      {
        path: 'payer/insurance-plans/:planId/edit',
        component: EditInsurancePlan,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Edit Insurance Plan | Healthcare Portal'
      },
      {
        path: 'payer/insurance-plans/:planId',
        component: PayerPlanDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Insurance Plan Details | Healthcare Portal'
      },
      {
        path: 'payer/insurance-plans',
        component: PayerInsurancePlans,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Insurance Plans | Healthcare Portal'
      },
      {
        path: 'member/insurance-plans/:planId',
        component: PlanDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Insurance Plan Details | Healthcare Portal'
      },
      {
        path: 'member/enrollments/new',
        component: CreateEnrollment,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'New Enrollment | Healthcare Portal'
      },
      {
        path: 'member/enrollments',
        component: MyEnrollments,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'My Enrollments | Healthcare Portal'
      },
      {
        path: 'member/enrollments/:enrollmentId',
        component: EnrollmentDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Enrollment Details | Healthcare Portal'
      },

      {
        path: 'payer/enrollments',
        component: PayerEnrollments,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Enrollment Requests | Healthcare Portal'
      },
      {
        path: 'payer/enrollments/:enrollmentId',
        component: PayerEnrollmentDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Enrollment Review | Healthcare Portal'
      },
      {
        path: 'member/policies',
        component: MyPolicies,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'My Policies | Healthcare Portal'
      },
      {
        path: 'member/policies/:policyId',
        component: PolicyDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Policy Details | Healthcare Portal'
      },

      {
        path: 'payer/policies',
        component: PayerPolicies,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Insurance Policies | Healthcare Portal'
      },
      {
        path: 'payer/policies/:policyId',
        component: PayerPolicyDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Policy Management | Healthcare Portal'
      },
      {
        path: 'payer/organization',
        component: PayerOrganizationPage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Payer Organization | Healthcare Portal'
      },
      {
        path: 'member/providers',
        component: MemberProviders,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Healthcare Providers | Healthcare Portal'
      },
      {
        path: 'member/providers/:providerId',
        component: MemberProviderDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Provider Details | Healthcare Portal'
      },
      {
        path: 'member/doctors/:doctorId',
        component: MemberDoctorDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Doctor Details | Healthcare Portal'
      },
      {
        path: 'member/appointments/new',
        component: BookAppointment,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Book Appointment | Healthcare Portal'
      },
      {
        path: 'member/appointments/:appointmentId',
        component: MemberAppointmentDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Appointment Details | Healthcare Portal'
      },
      {
        path: 'member/appointments',
        component: MemberAppointments,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'My Appointments | Healthcare Portal'
      },
      {
        path: 'provider/organization',
        component: ProviderOrganizationPage,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Provider Organization | Healthcare Portal'
      },{
        path: 'provider/departments/new',
        component: CreateDepartment,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Create Department | Healthcare Portal'
      },
      {
        path: 'provider/departments/:departmentId/edit',
        component: EditDepartment,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Edit Department | Healthcare Portal'
      },
      {
        path: 'provider/departments/:departmentId',
        component: ProviderDepartmentDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Department Details | Healthcare Portal'
      },
      {
        path: 'provider/departments',
        component: ProviderDepartments,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Departments | Healthcare Portal'
      },
      {
        path: 'provider/doctors/new',
        component: CreateDoctor,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Create Doctor | Healthcare Portal'
      },
      {
        path: 'provider/doctors/:doctorId/edit',
        component: EditDoctor,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Edit Doctor | Healthcare Portal'
      },
      {
        path: 'provider/doctors/:doctorId',
        component: ProviderDoctorDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Doctor Details | Healthcare Portal'
      },
      {
        path: 'provider/doctors',
        component: ProviderDoctors,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Doctors | Healthcare Portal'
      },
      {
        path: 'provider/availability/new',
        component: CreateAvailability,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Create Availability | Healthcare Portal'
      },
      {
        path: 'provider/availability/:availabilityId/edit',
        component: EditAvailability,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Edit Availability | Healthcare Portal'
      },
      {
        path: 'provider/availability',
        component: ProviderAvailability,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Doctor Availability | Healthcare Portal'
      },{
        path: 'provider/appointments/:appointmentId',
        component: ProviderAppointmentDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Appointment Details | Healthcare Portal'
      },
      {
        path: 'provider/appointments',
        component: ProviderAppointments,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Provider Appointments | Healthcare Portal'
      },
      {
        path: 'provider/medical-records/new',
        component: CreateMedicalRecord,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Create Medical Record | Healthcare Portal'
      },
      {
        path: 'provider/medical-records/:medicalRecordId',
        component: ProviderMedicalRecordDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Medical Record Details | Healthcare Portal'
      },
      {
        path: 'provider/medical-records',
        component: ProviderMedicalRecords,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Medical Records | Healthcare Portal'
      },
      {
        path: 'provider/medical-records/:medicalRecordId/edit',
        component: EditMedicalRecord,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Edit Medical Record | Healthcare Portal'
      },
      {
        path: 'member/medical-records',
        component: MemberMedicalRecords,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'My Medical Records | Healthcare Portal'
      },{
        path: 'member/medical-records/:medicalRecordId',
        component: MemberMedicalRecordDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Medical Record Details | Healthcare Portal'
      },{
        path: 'provider/claims/new',
        component: SubmitClaim,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Submit Claim | Healthcare Portal'
      },
      {
        path: 'provider/claims/:claimId',
        component: ProviderClaimDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Claim Details | Healthcare Portal'
      },
      {
        path: 'provider/claims',
        component: ProviderClaims,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PROVIDER'
          ]
        },
        title: 'Provider Claims | Healthcare Portal'
      },
      {
        path: 'payer/claims/:claimId',
        component: PayerClaimDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Claim Review | Healthcare Portal'
      },
      {
        path: 'payer/claims',
        component: PayerClaims,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'PAYER'
          ]
        },
        title: 'Payer Claims | Healthcare Portal'
      },
      {
        path: 'member/claims/:claimId',
        component: MemberClaimDetails,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'Claim Details | Healthcare Portal'
      },
      {
        path: 'member/claims',
        component: MemberClaims,
        canActivate: [
          roleGuard,
          memberProfileGuard
        ],
        data: {
          roles: [
            'MEMBER'
          ]
        },
        title: 'My Claims | Healthcare Portal'
      },
      {
        path: 'admin/providers/create',
        component: CreateProvider,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Create Provider | Healthcare Portal'
      },
      {
        path: 'admin/payers/create',
        component: CreatePayer,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Create Payer | Healthcare Portal'
      },
      {
        path: 'admin/payers',
        component: AdminPayers,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Payer Organizations'
      },
      {
        path: 'admin/providers',
        component: AdminProviders,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Provider Organizations'
      },
      
      {
        path: 'admin/payers/:payerId',
        component: AdminPayerDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Payer Organization Review'
      },
      {
        path: 'admin/providers/:providerId',
        component: AdminProviderDetails,
        canActivate: [
          roleGuard
        ],
        data: {
          roles: [
            'ADMIN'
          ]
        },
        title: 'Provider Organization Review'
      },
      
      // next
    ]
  },
  {
    path: '**',
    component: NotFound,
    title: 'Page Not Found'
  }
];