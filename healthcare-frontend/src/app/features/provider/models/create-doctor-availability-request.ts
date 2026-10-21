import {
    DayOfWeek
  } from './day-of-week';
  
  export interface CreateDoctorAvailabilityRequest {
    doctorId: string;
    dayOfWeek: DayOfWeek;
    startTime: string;
    endTime: string;
    slotDurationMinutes: number;
  }