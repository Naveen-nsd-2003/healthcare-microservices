package com.naveen.healthcare.payersservice.repository;

import com.naveen.healthcare.payersservice.entity.InsurancePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.naveen.healthcare.payersservice.enums.PolicyStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsurancePolicyRepository
        extends JpaRepository<InsurancePolicy, UUID> {

    boolean existsByEnrollmentId(
            UUID enrollmentId
    );

    boolean existsByPolicyNumberIgnoreCase(
            String policyNumber
    );

    Optional<InsurancePolicy> findByPolicyNumberIgnoreCase(
            String policyNumber
    );

    List<InsurancePolicy>
    findAllByMemberEmailIgnoreCaseOrderByCreatedAtDesc(
            String memberEmail
    );

    Optional<InsurancePolicy>
    findByIdAndMemberEmailIgnoreCase(
            UUID policyId,
            String memberEmail
    );

    List<InsurancePolicy>
    findAllByPayerOrganizationAdminEmailIgnoreCaseOrderByCreatedAtDesc(
            String payerAdminEmail
    );

    Optional<InsurancePolicy>
    findByIdAndPayerOrganizationAdminEmailIgnoreCase(
            UUID policyId,
            String payerAdminEmail
    );
    List<InsurancePolicy>
    findAllByStatusInAndCoverageEndDateBefore(
            Collection<PolicyStatus> statuses,
            LocalDate coverageEndDate
    );
}