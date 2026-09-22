package com.naveen.healthcare.payersservice.repository;

import com.naveen.healthcare.payersservice.entity.InsurancePlan;
import com.naveen.healthcare.payersservice.enums.InsurancePlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsurancePlanRepository
        extends JpaRepository<InsurancePlan, UUID> {

    boolean existsByPlanCodeIgnoreCase(
            String planCode
    );

    Optional<InsurancePlan>
    findByIdAndPayerOrganizationAdminEmailIgnoreCase(
            UUID planId,
            String adminEmail
    );

    List<InsurancePlan>
    findAllByPayerOrganizationAdminEmailIgnoreCaseOrderByCreatedAtDesc(
            String adminEmail
    );

    List<InsurancePlan>
    findAllByStatusOrderByCreatedAtDesc(
            InsurancePlanStatus status
    );

    Optional<InsurancePlan> findByIdAndStatus(
            UUID planId,
            InsurancePlanStatus status
    );
}