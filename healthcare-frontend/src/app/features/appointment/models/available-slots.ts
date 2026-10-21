import {
    AppointmentSlot
  } from './appointment-slot';
  
  import {
    DayOfWeek
  } from '../../provider/models/day-of-week';
  
  export interface AvailableSlots {
    providerId: string;
    providerOrganizationName: string;
  
    departmentId: string;
    departmentName: string;
  
    doctorId: string;
    doctorName: string;
    specialization: string;
  
    appointmentDate: string;
    dayOfWeek: DayOfWeek;
  
    availableSlots: AppointmentSlot[];
  }