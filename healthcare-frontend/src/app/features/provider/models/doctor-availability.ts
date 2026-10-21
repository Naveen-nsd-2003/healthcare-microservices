import {
    AvailabilityStatus
  } from './availability-status';
  
  import {
    DayOfWeek
  } from './day-of-week';
  
  export interface DoctorAvailability {
    id: string;
    doctorId: string;
    doctorName: string;
    specialization: string;
    departmentId: string;
    departmentName: string;
    providerId: string;
    providerOrganizationName: string;
    dayOfWeek: DayOfWeek;
    startTime: string;
    endTime: string;
    slotDurationMinutes: number;
    status: AvailabilityStatus;
    createdAt: string;
    updatedAt: string;
  }
  