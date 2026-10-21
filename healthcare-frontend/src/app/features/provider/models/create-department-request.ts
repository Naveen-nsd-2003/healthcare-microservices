export interface CreateDepartmentRequest {
    departmentName: string;
    departmentCode: string;
    description: string;
    contactNumber: string | null;
    location: string | null;
  }