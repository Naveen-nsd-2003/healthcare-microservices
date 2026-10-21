export interface UpdateDoctorRequest {
    firstName: string;
    lastName: string;
    qualification: string;
    specialization: string;
    yearsOfExperience: number;
    consultationFee: number;
    biography: string | null;
    departmentId: string;
  }