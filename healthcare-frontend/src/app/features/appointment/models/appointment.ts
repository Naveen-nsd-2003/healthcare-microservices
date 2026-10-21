import {
    AppointmentStatus
  } from './appointment-status';
  
  import {
    CancelledBy
  } from './cancelled-by';
  
  export interface Appointment {
    id: string;
  
    memberId: string;
    memberEmail: string;
  
    providerId: string;
    providerOrganizationName: string;
  
    departmentId: string;
    departmentName: string;
  
    doctorId: string;
    doctorName: string;
    doctorSpecialization: string;
  
    consultationFee: number;
  
    appointmentDate: string;
    startTime: string;
    endTime: string;
    slotDurationMinutes: number;
  
    reasonForVisit: string;
    status: AppointmentStatus;
  
    providerNotes: string | null;
    confirmedAt: string | null;
  
    cancelledBy: CancelledBy | null;
    cancellationReason: string | null;
    cancelledAt: string | null;
  
    completedAt: string | null;
    noShowAt: string | null;
  
    createdAt: string;
    updatedAt: string;
  }
  