package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.request.CreateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.ProviderOrganizationResponse;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.DuplicateProviderException;
import com.naveen.healthcare.providerservice.exception.InvalidProviderOperationException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.ProviderOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import com.naveen.healthcare.providerservice.dto.response.ProviderIdentityResponse;


@Service
@RequiredArgsConstructor
public class ProviderOrganizationServiceImpl
        implements ProviderOrganizationService {

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    @Override
    @Transactional
    public ProviderOrganizationResponse createProviderOrganization(
            CreateProviderOrganizationRequest request
    ) {

        String normalizedRegistrationNumber =
                normalizeBusinessIdentifier(
                        request.getRegistrationNumber()
                );

        String normalizedLicenseNumber =
                normalizeBusinessIdentifier(
                        request.getLicenseNumber()
                );

        String normalizedAdminEmail =
                normalizeEmail(
                        request.getAdminEmail()
                );

        validateUniqueProviderDetails(
                normalizedRegistrationNumber,
                normalizedLicenseNumber,
                normalizedAdminEmail
        );

        ProviderOrganization providerOrganization =
                ProviderOrganization.builder()
                        .organizationName(
                                normalizeRequiredText(
                                        request.getOrganizationName()
                                )
                        )
                        .providerType(
                                request.getProviderType()
                        )
                        .registrationNumber(
                                normalizedRegistrationNumber
                        )
                        .licenseNumber(
                                normalizedLicenseNumber
                        )
                        .adminEmail(
                                normalizedAdminEmail
                        )
                        .contactNumber(
                                normalizeRequiredText(
                                        request.getContactNumber()
                                )
                        )
                        .website(
                                normalizeOptionalText(
                                        request.getWebsite()
                                )
                        )
                        .addressLine(
                                normalizeRequiredText(
                                        request.getAddressLine()
                                )
                        )
                        .city(
                                normalizeRequiredText(
                                        request.getCity()
                                )
                        )
                        .state(
                                normalizeRequiredText(
                                        request.getState()
                                )
                        )
                        .postalCode(
                                normalizeRequiredText(
                                        request.getPostalCode()
                                )
                        )
                        .country(
                                normalizeRequiredText(
                                        request.getCountry()
                                )
                        )
                        .emergencyServicesAvailable(
                                request.getEmergencyServicesAvailable()
                        )
                        .status(
                                ProviderStatus.ACTIVE
                        )
                        .build();

        ProviderOrganization savedProviderOrganization =
                providerOrganizationRepository.save(
                        providerOrganization
                );

        return mapToResponse(savedProviderOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderOrganizationResponse>
    getAllProviderOrganizations() {

        return providerOrganizationRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderOrganizationResponse getProviderOrganizationById(
            UUID providerId
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(providerId);

        return mapToResponse(providerOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderOrganizationResponse getMyProviderOrganization(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationByEmail(
                        authenticatedProviderEmail
                );

        return mapToResponse(providerOrganization);
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse updateMyProviderOrganization(
            String authenticatedProviderEmail,
            UpdateProviderOrganizationRequest request
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationByEmail(
                        authenticatedProviderEmail
                );

        validateProviderCanManageOrganization(
                providerOrganization
        );

        providerOrganization.setOrganizationName(
                normalizeRequiredText(
                        request.getOrganizationName()
                )
        );

        providerOrganization.setProviderType(
                request.getProviderType()
        );

        providerOrganization.setContactNumber(
                normalizeRequiredText(
                        request.getContactNumber()
                )
        );

        providerOrganization.setWebsite(
                normalizeOptionalText(
                        request.getWebsite()
                )
        );

        providerOrganization.setAddressLine(
                normalizeRequiredText(
                        request.getAddressLine()
                )
        );

        providerOrganization.setCity(
                normalizeRequiredText(
                        request.getCity()
                )
        );

        providerOrganization.setState(
                normalizeRequiredText(
                        request.getState()
                )
        );

        providerOrganization.setPostalCode(
                normalizeRequiredText(
                        request.getPostalCode()
                )
        );

        providerOrganization.setCountry(
                normalizeRequiredText(
                        request.getCountry()
                )
        );

        providerOrganization.setEmergencyServicesAvailable(
                request.getEmergencyServicesAvailable()
        );

        ProviderOrganization updatedProviderOrganization =
                providerOrganizationRepository.save(
                        providerOrganization
                );

        return mapToResponse(updatedProviderOrganization);
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse updateProviderStatus(
            UUID providerId,
            UpdateProviderStatusRequest request
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(providerId);

        ProviderStatus requestedStatus =
                request.getStatus();

        if (requestedStatus == null) {
            throw new InvalidProviderOperationException(
                    "Provider status is required"
            );
        }

        if (providerOrganization.getStatus()
                == requestedStatus) {

            throw new InvalidProviderOperationException(
                    "Provider organization is already in "
                            + requestedStatus
                            + " status"
            );
        }

        providerOrganization.setStatus(requestedStatus);

        ProviderOrganization updatedProviderOrganization =
                providerOrganizationRepository.save(
                        providerOrganization
                );

        return mapToResponse(updatedProviderOrganization);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderOrganizationResponse>
    getAvailableProviders() {

        return providerOrganizationRepository
                .findAllByStatusOrderByOrganizationNameAsc(
                        ProviderStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderOrganizationResponse getAvailableProviderById(
            UUID providerId
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(providerId);

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new ProviderNotFoundException(
                    "Active Provider organization not found with ID: "
                            + providerId
            );
        }

        return mapToResponse(providerOrganization);
    }

    private ProviderOrganization findProviderOrganizationById(
            UUID providerId
    ) {

        if (providerId == null) {
            throw new InvalidProviderOperationException(
                    "Provider ID is required"
            );
        }

        return providerOrganizationRepository
                .findById(providerId)
                .orElseThrow(() ->
                        new ProviderNotFoundException(
                                "Provider organization not found with ID: "
                                        + providerId
                        )
                );
    }

    private ProviderOrganization findProviderOrganizationByEmail(
            String authenticatedProviderEmail
    ) {

        String normalizedProviderEmail =
                normalizeEmail(
                        authenticatedProviderEmail
                );

        return providerOrganizationRepository
                .findByAdminEmailIgnoreCase(
                        normalizedProviderEmail
                )
                .orElseThrow(() ->
                        new ProviderNotFoundException(
                                "Provider organization not found for "
                                        + "authenticated email: "
                                        + normalizedProviderEmail
                        )
                );
    }

    private void validateUniqueProviderDetails(
            String registrationNumber,
            String licenseNumber,
            String adminEmail
    ) {

        if (providerOrganizationRepository
                .existsByRegistrationNumberIgnoreCase(
                        registrationNumber
                )) {

            throw new DuplicateProviderException(
                    "A Provider organization already exists "
                            + "with registration number: "
                            + registrationNumber
            );
        }

        if (providerOrganizationRepository
                .existsByLicenseNumberIgnoreCase(
                        licenseNumber
                )) {

            throw new DuplicateProviderException(
                    "A Provider organization already exists "
                            + "with license number: "
                            + licenseNumber
            );
        }

        if (providerOrganizationRepository
                .existsByAdminEmailIgnoreCase(
                        adminEmail
                )) {

            throw new DuplicateProviderException(
                    "A Provider organization already exists "
                            + "for administrator email: "
                            + adminEmail
            );
        }
    }

    private void validateProviderCanManageOrganization(
            ProviderOrganization providerOrganization
    ) {

        if (providerOrganization.getStatus()
                == ProviderStatus.SUSPENDED) {

            throw new InvalidProviderOperationException(
                    "A suspended Provider organization "
                            + "cannot be updated"
            );
        }

        if (providerOrganization.getStatus()
                == ProviderStatus.INACTIVE) {

            throw new InvalidProviderOperationException(
                    "An inactive Provider organization "
                            + "cannot be updated"
            );
        }
    }

    private ProviderOrganizationResponse mapToResponse(
            ProviderOrganization providerOrganization
    ) {

        return ProviderOrganizationResponse.builder()
                .id(
                        providerOrganization.getId()
                )
                .organizationName(
                        providerOrganization.getOrganizationName()
                )
                .providerType(
                        providerOrganization.getProviderType()
                )
                .registrationNumber(
                        providerOrganization.getRegistrationNumber()
                )
                .licenseNumber(
                        providerOrganization.getLicenseNumber()
                )
                .adminEmail(
                        providerOrganization.getAdminEmail()
                )
                .contactNumber(
                        providerOrganization.getContactNumber()
                )
                .website(
                        providerOrganization.getWebsite()
                )
                .addressLine(
                        providerOrganization.getAddressLine()
                )
                .city(
                        providerOrganization.getCity()
                )
                .state(
                        providerOrganization.getState()
                )
                .postalCode(
                        providerOrganization.getPostalCode()
                )
                .country(
                        providerOrganization.getCountry()
                )
                .emergencyServicesAvailable(
                        providerOrganization
                                .getEmergencyServicesAvailable()
                )
                .status(
                        providerOrganization.getStatus()
                )
                .createdAt(
                        providerOrganization.getCreatedAt()
                )
                .updatedAt(
                        providerOrganization.getUpdatedAt()
                )
                .build();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidProviderOperationException(
                    "Provider administrator email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeBusinessIdentifier(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidProviderOperationException(
                    "Provider business identifier is required"
            );
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidProviderOperationException(
                    "Required Provider information cannot be empty"
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    @Override
    @Transactional(readOnly = true)
    public ProviderIdentityResponse getInternalProviderIdentity(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationByEmail(
                        authenticatedProviderEmail
                );

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new InvalidProviderOperationException(
                    "Provider organization must be ACTIVE "
                            + "to manage appointments"
            );
        }

        return ProviderIdentityResponse.builder()
                .providerId(
                        providerOrganization.getId()
                )
                .adminEmail(
                        providerOrganization.getAdminEmail()
                )
                .providerOrganizationName(
                        providerOrganization.getOrganizationName()
                )
                .status(
                        providerOrganization.getStatus()
                )
                .build();
    }
}