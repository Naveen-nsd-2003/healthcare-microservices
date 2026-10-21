import {
    DayOfWeek
  } from './day-of-week';
  
  export interface UpdateDoctorAvailabilityRequest {
    dayOfWeek: DayOfWeek;
    startTime: string;
    endTime: string;
    slotDurationMinutes: number;
  }