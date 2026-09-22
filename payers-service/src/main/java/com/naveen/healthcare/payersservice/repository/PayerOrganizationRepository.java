package com.naveen.healthcare.payersservice.repository;

import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayerOrganizationRepository
        extends JpaRepository<PayerOrganization, UUID> {

    Optional<PayerOrganization> findByAdminEmailIgnoreCase(
            String adminEmail
    );

}