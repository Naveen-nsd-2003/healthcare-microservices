package com.naveen.healthcare.providerservice.repository;

import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.enums.ProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProviderOrganizationRepository
        extends JpaRepository<ProviderOrganization, UUID> {

    boolean existsByRegistrationNumberIgnoreCase(
            String registrationNumber
    );

    boolean existsByLicenseNumberIgnoreCase(
            String licenseNumber
    );

    boolean existsByAdminEmailIgnoreCase(
            String adminEmail
    );

    Optional<ProviderOrganization> findByAdminEmailIgnoreCase(
            String adminEmail
    );

    List<ProviderOrganization> findAllByOrderByCreatedAtDesc();

    List<ProviderOrganization>
    findAllByStatusOrderByOrganizationNameAsc(
            ProviderStatus status
    );

    List<ProviderOrganization>
    findAllByStatusAndProviderTypeOrderByOrganizationNameAsc(
            ProviderStatus status,
            ProviderType providerType
    );

    List<ProviderOrganization>
    findAllByStatusAndCityIgnoreCaseOrderByOrganizationNameAsc(
            ProviderStatus status,
            String city
    );
}