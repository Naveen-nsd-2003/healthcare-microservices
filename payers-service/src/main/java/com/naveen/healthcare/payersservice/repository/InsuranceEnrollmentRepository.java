package com.naveen.healthcare.payersservice.repository;

import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceEnrollmentRepository
        extends JpaRepository<InsuranceEnrollment, UUID> {

    boolean existsByMemberIdAndInsurancePlanIdAndStatusIn(
            UUID memberId,
            UUID planId,
            Collection<EnrollmentStatus> statuses
    );

    List<InsuranceEnrollment>
    findAllByMemberEmailIgnoreCaseOrderByCreatedAtDesc(
            String memberEmail
    );

    Optional<InsuranceEnrollment>
    findByIdAndMemberEmailIgnoreCase(
            UUID enrollmentId,
            String memberEmail
    );

    List<InsuranceEnrollment>
    findAllByPayerOrganizationAdminEmailIgnoreCaseOrderByCreatedAtDesc(
            String payerAdminEmail
    );

    Optional<InsuranceEnrollment>
    findByIdAndPayerOrganizationAdminEmailIgnoreCase(
            UUID enrollmentId,
            String payerAdminEmail
    );
}