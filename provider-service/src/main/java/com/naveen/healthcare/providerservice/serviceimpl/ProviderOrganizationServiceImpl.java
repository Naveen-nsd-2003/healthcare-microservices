package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.request.ApproveProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.CreateMyProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.CreateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.RejectProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.ProviderIdentityResponse;
import com.naveen.healthcare.providerservice.dto.response.ProviderOrganizationResponse;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.enums.ProviderType;
import com.naveen.healthcare.providerservice.exception.DuplicateProviderException;
import com.naveen.healthcare.providerservice.exception.InvalidProviderOperationException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.ProviderOrganizationEventPublisher;
import com.naveen.healthcare.providerservice.service.ProviderOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderOrganizationServiceImpl
        implements ProviderOrganizationService {

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    private final ProviderOrganizationEventPublisher
            providerOrganizationEventPublisher;

    @Override
    @Transactional
    public ProviderOrganizationResponse createProviderOrganization(
            CreateProviderOrganizationRequest request
    ) {

        validateAdminCreateRequest(
                request
        );

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
                buildProviderOrganization(
                        request.getOrganizationName(),
                        request.getProviderType(),
                        normalizedRegistrationNumber,
                        normalizedLicenseNumber,
                        normalizedAdminEmail,
                        request.getContactNumber(),
                        request.getWebsite(),
                        request.getAddressLine(),
                        request.getCity(),
                        request.getState(),
                        request.getPostalCode(),
                        request.getCountry(),
                        request.getEmergencyServicesAvailable(),
                        ProviderStatus.ACTIVE,
                        null
                );

        ProviderOrganization savedProviderOrganization =
                providerOrganizationRepository.saveAndFlush(
                        providerOrganization
                );

        return mapToResponse(
                savedProviderOrganization
        );
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse createMyProviderOrganization(
            String authenticatedProviderEmail,
            CreateMyProviderOrganizationRequest request
    ) {

        validateSelfCreateRequest(
                request
        );

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
                        authenticatedProviderEmail
                );

        validateUniqueProviderDetails(
                normalizedRegistrationNumber,
                normalizedLicenseNumber,
                normalizedAdminEmail
        );

        ProviderOrganization providerOrganization =
                buildProviderOrganization(
                        request.getOrganizationName(),
                        request.getProviderType(),
                        normalizedRegistrationNumber,
                        normalizedLicenseNumber,
                        normalizedAdminEmail,
                        request.getContactNumber(),
                        request.getWebsite(),
                        request.getAddressLine(),
                        request.getCity(),
                        request.getState(),
                        request.getPostalCode(),
                        request.getCountry(),
                        request.getEmergencyServicesAvailable(),
                        ProviderStatus.PENDING_APPROVAL,
                        LocalDateTime.now()
                );

        ProviderOrganization savedProviderOrganization =
                providerOrganizationRepository.saveAndFlush(
                        providerOrganization
                );

        providerOrganizationEventPublisher
                .publishOrganizationSubmitted(
                        savedProviderOrganization
                );

        return mapToResponse(
                savedProviderOrganization
        );
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
    public ProviderOrganizationResponse
    getProviderOrganizationById(
            UUID providerId
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(
                        providerId
                );

        return mapToResponse(
                providerOrganization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderOrganizationResponse
    getMyProviderOrganization(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationByEmail(
                        authenticatedProviderEmail
                );

        return mapToResponse(
                providerOrganization
        );
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse
    updateMyProviderOrganization(
            String authenticatedProviderEmail,
            UpdateProviderOrganizationRequest request
    ) {

        validateUpdateRequest(
                request
        );

        ProviderOrganization providerOrganization =
                findProviderOrganizationByEmail(
                        authenticatedProviderEmail
                );

        validateProviderCanUpdateOrganization(
                providerOrganization
        );

        ProviderStatus previousStatus =
                providerOrganization.getStatus();

        providerOrganization.setOrganizationName(
                normalizeRequiredText(
                        request.getOrganizationName()
                )
        );

        providerOrganization.setProviderType(
                requireProviderType(
                        request.getProviderType()
                )
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
                requireEmergencyServicesAvailability(
                        request.getEmergencyServicesAvailable()
                )
        );

        if (previousStatus == ProviderStatus.REJECTED) {

            providerOrganization.setStatus(
                    ProviderStatus.PENDING_APPROVAL
            );

            providerOrganization.setSubmittedAt(
                    LocalDateTime.now()
            );

            clearReviewInformation(
                    providerOrganization
            );
        }

        ProviderOrganization updatedProviderOrganization =
                providerOrganizationRepository.saveAndFlush(
                        providerOrganization
                );

        if (previousStatus == ProviderStatus.REJECTED) {

            providerOrganizationEventPublisher
                    .publishOrganizationSubmitted(
                            updatedProviderOrganization
                    );
        }

        return mapToResponse(
                updatedProviderOrganization
        );
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse
    approveProviderOrganization(
            UUID providerId,
            String authenticatedAdminEmail,
            ApproveProviderOrganizationRequest request
    ) {

        validateApprovalRequest(
                request
        );

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(
                        providerId
                );

        validatePendingApprovalStatus(
                providerOrganization
        );

        providerOrganization.setStatus(
                ProviderStatus.ACTIVE
        );

        providerOrganization.setReviewedAt(
                LocalDateTime.now()
        );

        providerOrganization.setReviewedByEmail(
                normalizeEmail(
                        authenticatedAdminEmail
                )
        );

        providerOrganization.setReviewRemarks(
                normalizeOptionalText(
                        request.getReviewRemarks()
                )
        );

        providerOrganization.setRejectionReason(
                null
        );

        ProviderOrganization approvedProviderOrganization =
                providerOrganizationRepository.saveAndFlush(
                        providerOrganization
                );

        providerOrganizationEventPublisher
                .publishOrganizationApproved(
                        approvedProviderOrganization
                );

        return mapToResponse(
                approvedProviderOrganization
        );
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse
    rejectProviderOrganization(
            UUID providerId,
            String authenticatedAdminEmail,
            RejectProviderOrganizationRequest request
    ) {

        validateRejectionRequest(
                request
        );

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(
                        providerId
                );

        validatePendingApprovalStatus(
                providerOrganization
        );

        providerOrganization.setStatus(
                ProviderStatus.REJECTED
        );

        providerOrganization.setReviewedAt(
                LocalDateTime.now()
        );

        providerOrganization.setReviewedByEmail(
                normalizeEmail(
                        authenticatedAdminEmail
                )
        );

        providerOrganization.setReviewRemarks(
                normalizeOptionalText(
                        request.getReviewRemarks()
                )
        );

        providerOrganization.setRejectionReason(
                normalizeRequiredText(
                        request.getRejectionReason()
                )
        );

        ProviderOrganization rejectedProviderOrganization =
                providerOrganizationRepository.saveAndFlush(
                        providerOrganization
                );

        providerOrganizationEventPublisher
                .publishOrganizationRejected(
                        rejectedProviderOrganization
                );

        return mapToResponse(
                rejectedProviderOrganization
        );
    }

    @Override
    @Transactional
    public ProviderOrganizationResponse updateProviderStatus(
            UUID providerId,
            UpdateProviderStatusRequest request
    ) {

        validateStatusUpdateRequest(
                request
        );

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(
                        providerId
                );

        ProviderStatus previousStatus =
                providerOrganization.getStatus();

        ProviderStatus requestedStatus =
                request.getStatus();

        if (previousStatus == requestedStatus) {

            throw new InvalidProviderOperationException(
                    "Provider organization is already in "
                            + requestedStatus
                            + " status"
            );
        }

        validateAdministrativeStatusTransition(
                previousStatus,
                requestedStatus
        );

        providerOrganization.setStatus(
                requestedStatus
        );

        ProviderOrganization updatedProviderOrganization =
                providerOrganizationRepository.saveAndFlush(
                        providerOrganization
                );

        publishAdministrativeStatusEvent(
                updatedProviderOrganization,
                previousStatus,
                requestedStatus
        );

        return mapToResponse(
                updatedProviderOrganization
        );
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
    public ProviderOrganizationResponse
    getAvailableProviderById(
            UUID providerId
    ) {

        ProviderOrganization providerOrganization =
                findProviderOrganizationById(
                        providerId
                );

        if (
                providerOrganization.getStatus()
                        != ProviderStatus.ACTIVE
        ) {

            throw new ProviderNotFoundException(
                    "Active Provider organization not found with ID: "
                            + providerId
            );
        }

        return mapToResponse(
                providerOrganization
        );
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

        if (
                providerOrganization.getStatus()
                        != ProviderStatus.ACTIVE
        ) {

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

    private void publishAdministrativeStatusEvent(
            ProviderOrganization providerOrganization,
            ProviderStatus previousStatus,
            ProviderStatus requestedStatus
    ) {

        if (requestedStatus == ProviderStatus.SUSPENDED) {

            providerOrganizationEventPublisher
                    .publishOrganizationSuspended(
                            providerOrganization
                    );

            return;
        }

        if (requestedStatus == ProviderStatus.INACTIVE) {

            providerOrganizationEventPublisher
                    .publishOrganizationDeactivated(
                            providerOrganization
                    );

            return;
        }

        if (
                requestedStatus == ProviderStatus.ACTIVE
                        && (
                        previousStatus
                                == ProviderStatus.SUSPENDED
                                || previousStatus
                                == ProviderStatus.INACTIVE
                )
        ) {

            providerOrganizationEventPublisher
                    .publishOrganizationReactivated(
                            providerOrganization
                    );
        }
    }

    private ProviderOrganization buildProviderOrganization(
            String organizationName,
            ProviderType providerType,
            String registrationNumber,
            String licenseNumber,
            String adminEmail,
            String contactNumber,
            String website,
            String addressLine,
            String city,
            String state,
            String postalCode,
            String country,
            Boolean emergencyServicesAvailable,
            ProviderStatus providerStatus,
            LocalDateTime submittedAt
    ) {

        return ProviderOrganization.builder()
                .organizationName(
                        normalizeRequiredText(
                                organizationName
                        )
                )
                .providerType(
                        requireProviderType(
                                providerType
                        )
                )
                .registrationNumber(
                        normalizeBusinessIdentifier(
                                registrationNumber
                        )
                )
                .licenseNumber(
                        normalizeBusinessIdentifier(
                                licenseNumber
                        )
                )
                .adminEmail(
                        normalizeEmail(
                                adminEmail
                        )
                )
                .contactNumber(
                        normalizeRequiredText(
                                contactNumber
                        )
                )
                .website(
                        normalizeOptionalText(
                                website
                        )
                )
                .addressLine(
                        normalizeRequiredText(
                                addressLine
                        )
                )
                .city(
                        normalizeRequiredText(
                                city
                        )
                )
                .state(
                        normalizeRequiredText(
                                state
                        )
                )
                .postalCode(
                        normalizeRequiredText(
                                postalCode
                        )
                )
                .country(
                        normalizeRequiredText(
                                country
                        )
                )
                .emergencyServicesAvailable(
                        requireEmergencyServicesAvailability(
                                emergencyServicesAvailable
                        )
                )
                .status(
                        providerStatus
                )
                .submittedAt(
                        submittedAt
                )
                .reviewedAt(
                        null
                )
                .reviewedByEmail(
                        null
                )
                .reviewRemarks(
                        null
                )
                .rejectionReason(
                        null
                )
                .build();
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
                .findById(
                        providerId
                )
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

        if (
                providerOrganizationRepository
                        .existsByRegistrationNumberIgnoreCase(
                                registrationNumber
                        )
        ) {

            throw new DuplicateProviderException(
                    "A Provider organization already exists "
                            + "with registration number: "
                            + registrationNumber
            );
        }

        if (
                providerOrganizationRepository
                        .existsByLicenseNumberIgnoreCase(
                                licenseNumber
                        )
        ) {

            throw new DuplicateProviderException(
                    "A Provider organization already exists "
                            + "with license number: "
                            + licenseNumber
            );
        }

        if (
                providerOrganizationRepository
                        .existsByAdminEmailIgnoreCase(
                                adminEmail
                        )
        ) {

            throw new DuplicateProviderException(
                    "A Provider organization already exists "
                            + "for administrator email: "
                            + adminEmail
            );
        }
    }

    private void validateProviderCanUpdateOrganization(
            ProviderOrganization providerOrganization
    ) {

        ProviderStatus currentStatus =
                providerOrganization.getStatus();

        if (currentStatus == ProviderStatus.SUSPENDED) {

            throw new InvalidProviderOperationException(
                    "A suspended Provider organization "
                            + "cannot be updated"
            );
        }

        if (currentStatus == ProviderStatus.INACTIVE) {

            throw new InvalidProviderOperationException(
                    "An inactive Provider organization "
                            + "cannot be updated"
            );
        }
    }

    private void validatePendingApprovalStatus(
            ProviderOrganization providerOrganization
    ) {

        if (
                providerOrganization.getStatus()
                        != ProviderStatus.PENDING_APPROVAL
        ) {

            throw new InvalidProviderOperationException(
                    "Only a Provider organization in "
                            + "PENDING_APPROVAL status "
                            + "can be approved or rejected"
            );
        }
    }

    private void validateAdministrativeStatusTransition(
            ProviderStatus currentStatus,
            ProviderStatus requestedStatus
    ) {

        boolean validTransition =
                (
                        currentStatus == ProviderStatus.ACTIVE
                                && (
                                requestedStatus
                                        == ProviderStatus.SUSPENDED
                                        || requestedStatus
                                        == ProviderStatus.INACTIVE
                        )
                )
                        || (
                        currentStatus == ProviderStatus.SUSPENDED
                                && (
                                requestedStatus
                                        == ProviderStatus.ACTIVE
                                        || requestedStatus
                                        == ProviderStatus.INACTIVE
                        )
                )
                        || (
                        currentStatus == ProviderStatus.INACTIVE
                                && requestedStatus
                                == ProviderStatus.ACTIVE
                );

        if (!validTransition) {

            throw new InvalidProviderOperationException(
                    "Invalid Provider organization status "
                            + "transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }
    }

    private void clearReviewInformation(
            ProviderOrganization providerOrganization
    ) {

        providerOrganization.setReviewedAt(
                null
        );

        providerOrganization.setReviewedByEmail(
                null
        );

        providerOrganization.setReviewRemarks(
                null
        );

        providerOrganization.setRejectionReason(
                null
        );
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
                .submittedAt(
                        providerOrganization.getSubmittedAt()
                )
                .reviewedAt(
                        providerOrganization.getReviewedAt()
                )
                .reviewedByEmail(
                        providerOrganization.getReviewedByEmail()
                )
                .reviewRemarks(
                        providerOrganization.getReviewRemarks()
                )
                .rejectionReason(
                        providerOrganization.getRejectionReason()
                )
                .createdAt(
                        providerOrganization.getCreatedAt()
                )
                .updatedAt(
                        providerOrganization.getUpdatedAt()
                )
                .build();
    }

    private void validateAdminCreateRequest(
            CreateProviderOrganizationRequest request
    ) {

        if (request == null) {

            throw new InvalidProviderOperationException(
                    "Provider organization creation request is required"
            );
        }
    }

    private void validateSelfCreateRequest(
            CreateMyProviderOrganizationRequest request
    ) {

        if (request == null) {

            throw new InvalidProviderOperationException(
                    "Provider organization submission request is required"
            );
        }
    }

    private void validateUpdateRequest(
            UpdateProviderOrganizationRequest request
    ) {

        if (request == null) {

            throw new InvalidProviderOperationException(
                    "Provider organization update request is required"
            );
        }
    }

    private void validateApprovalRequest(
            ApproveProviderOrganizationRequest request
    ) {

        if (request == null) {

            throw new InvalidProviderOperationException(
                    "Provider approval request is required"
            );
        }
    }

    private void validateRejectionRequest(
            RejectProviderOrganizationRequest request
    ) {

        if (request == null) {

            throw new InvalidProviderOperationException(
                    "Provider rejection request is required"
            );
        }
    }

    private void validateStatusUpdateRequest(
            UpdateProviderStatusRequest request
    ) {

        if (request == null) {

            throw new InvalidProviderOperationException(
                    "Provider status update request is required"
            );
        }

        if (request.getStatus() == null) {

            throw new InvalidProviderOperationException(
                    "Provider status is required"
            );
        }
    }

    private ProviderType requireProviderType(
            ProviderType providerType
    ) {

        if (providerType == null) {

            throw new InvalidProviderOperationException(
                    "Provider type is required"
            );
        }

        return providerType;
    }

    private Boolean requireEmergencyServicesAvailability(
            Boolean emergencyServicesAvailable
    ) {

        if (emergencyServicesAvailable == null) {

            throw new InvalidProviderOperationException(
                    "Emergency services availability is required"
            );
        }

        return emergencyServicesAvailable;
    }

    private String normalizeEmail(
            String email
    ) {

        if (
                email == null
                        || email.isBlank()
        ) {

            throw new InvalidProviderOperationException(
                    "Provider administrator email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeBusinessIdentifier(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new InvalidProviderOperationException(
                    "Provider business identifier is required"
            );
        }

        return value
                .trim()
                .toUpperCase(
                        Locale.ROOT
                );
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new InvalidProviderOperationException(
                    "Required Provider information cannot be empty"
            );
        }

        return value.trim();
    }

    private String normalizeOptionalText(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            return null;
        }

        return value.trim();
    }
}