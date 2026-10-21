export interface CreateDoctorRequest {
    firstName: string;
    lastName: string;
    medicalRegistrationNumber: string;
    qualification: string;
    specialization: string;
    yearsOfExperience: number;
    consultationFee: number;
    biography: string | null;
    departmentId: string;
  }