package com.naveen.healthcare.payersservice.repository;

import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PayerRepository
        extends JpaRepository<PayerOrganization, UUID> {


    Optional<PayerOrganization> findByAdminEmailIgnoreCase(
            String adminEmail
    );


    boolean existsByAdminEmailIgnoreCase(
            String adminEmail
    );


    boolean existsByRegistrationNumberIgnoreCase(
            String registrationNumber
    );


    List<PayerOrganization> findAllByStatus(
            PayerStatus status
    );
}