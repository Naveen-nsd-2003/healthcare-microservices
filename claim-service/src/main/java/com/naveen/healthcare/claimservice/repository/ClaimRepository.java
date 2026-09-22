package com.naveen.healthcare.claimservice.repository;

import com.naveen.healthcare.claimservice.entity.Claim;
import com.naveen.healthcare.claimservice.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClaimRepository
        extends JpaRepository<Claim, UUID> {

    boolean existsByClaimNumberIgnoreCase(
            String claimNumber
    );

    boolean existsByAppointmentId(
            UUID appointmentId
    );

    boolean existsByMedicalRecordId(
            UUID medicalRecordId
    );

    Optional<Claim> findByClaimNumberIgnoreCase(
            String claimNumber
    );

    List<Claim>
    findAllByProviderIdOrderBySubmittedAtDesc(
            UUID providerId
    );

    Optional<Claim> findByIdAndProviderId(
            UUID claimId,
            UUID providerId
    );

    List<Claim>
    findAllByPayerIdOrderBySubmittedAtDesc(
            UUID payerId
    );

    Optional<Claim> findByIdAndPayerId(
            UUID claimId,
            UUID payerId
    );

    List<Claim>
    findAllByPayerIdAndStatusOrderBySubmittedAtAsc(
            UUID payerId,
            ClaimStatus status
    );

    List<Claim>
    findAllByMemberEmailIgnoreCaseOrderBySubmittedAtDesc(
            String memberEmail
    );

    Optional<Claim> findByIdAndMemberEmailIgnoreCase(
            UUID claimId,
            String memberEmail
    );

    List<Claim>
    findAllByPolicyIdOrderBySubmittedAtDesc(
            UUID policyId
    );

    List<Claim>
    findAllByProviderIdAndStatusOrderBySubmittedAtDesc(
            UUID providerId,
            ClaimStatus status
    );
}