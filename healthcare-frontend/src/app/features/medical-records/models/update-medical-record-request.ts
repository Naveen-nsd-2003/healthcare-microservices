export interface UpdateMedicalRecordRequest {

    chiefComplaint: string;
  
    diagnosis: string;
  
    clinicalObservations: string | null;
  
    treatmentPlan: string;
  
    prescriptionNotes: string | null;
  
    followUpDate: string | null;
  }