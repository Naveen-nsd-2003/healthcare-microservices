package com.naveen.healthcare.providerservice.repository;

import com.naveen.healthcare.providerservice.entity.Doctor;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, UUID> {

    boolean existsByMedicalRegistrationNumberIgnoreCase(
            String medicalRegistrationNumber
    );

    List<Doctor>
    findAllByProviderOrganizationAdminEmailIgnoreCaseOrderByFirstNameAscLastNameAsc(
            String providerAdminEmail
    );

    Optional<Doctor>
    findByIdAndProviderOrganizationAdminEmailIgnoreCase(
            UUID doctorId,
            String providerAdminEmail
    );

    List<Doctor>
    findAllByStatusAndDepartmentStatusAndProviderOrganizationStatusOrderByFirstNameAscLastNameAsc(
            DoctorStatus doctorStatus,
            DepartmentStatus departmentStatus,
            ProviderStatus providerStatus
    );

    List<Doctor>
    findAllByProviderOrganizationIdAndStatusAndDepartmentStatusAndProviderOrganizationStatusOrderByFirstNameAscLastNameAsc(
            UUID providerId,
            DoctorStatus doctorStatus,
            DepartmentStatus departmentStatus,
            ProviderStatus providerStatus
    );

    List<Doctor>
    findAllByDepartmentIdAndStatusAndDepartmentStatusAndProviderOrganizationStatusOrderByFirstNameAscLastNameAsc(
            UUID departmentId,
            DoctorStatus doctorStatus,
            DepartmentStatus departmentStatus,
            ProviderStatus providerStatus
    );

    Optional<Doctor>
    findByIdAndStatusAndDepartmentStatusAndProviderOrganizationStatus(
            UUID doctorId,
            DoctorStatus doctorStatus,
            DepartmentStatus departmentStatus,
            ProviderStatus providerStatus
    );
}