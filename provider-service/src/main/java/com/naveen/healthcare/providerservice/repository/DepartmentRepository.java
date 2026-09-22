package com.naveen.healthcare.providerservice.repository;

import com.naveen.healthcare.providerservice.entity.Department;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DepartmentRepository
        extends JpaRepository<Department, UUID> {

    boolean existsByProviderOrganizationIdAndDepartmentCodeIgnoreCase(
            UUID providerId,
            String departmentCode
    );

    boolean existsByProviderOrganizationIdAndDepartmentNameIgnoreCase(
            UUID providerId,
            String departmentName
    );

    boolean existsByProviderOrganizationIdAndDepartmentNameIgnoreCaseAndIdNot(
            UUID providerId,
            String departmentName,
            UUID departmentId
    );

    List<Department>
    findAllByProviderOrganizationAdminEmailIgnoreCaseOrderByDepartmentNameAsc(
            String providerAdminEmail
    );

    Optional<Department>
    findByIdAndProviderOrganizationAdminEmailIgnoreCase(
            UUID departmentId,
            String providerAdminEmail
    );

    List<Department>
    findAllByProviderOrganizationIdAndStatusOrderByDepartmentNameAsc(
            UUID providerId,
            DepartmentStatus status
    );

    Optional<Department>
    findByIdAndStatusAndProviderOrganizationStatus(
            UUID departmentId,
            DepartmentStatus departmentStatus,
            com.naveen.healthcare.providerservice.enums.ProviderStatus
                    providerStatus
    );
}