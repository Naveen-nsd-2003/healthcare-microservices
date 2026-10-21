package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.request.ApprovePayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.CreateMyPayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.CreatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.RejectPayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PayerResponse;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import com.naveen.healthcare.payersservice.exception.DuplicatePayerException;
import com.naveen.healthcare.payersservice.exception.InvalidPayerStatusException;
import com.naveen.healthcare.payersservice.exception.PayerNotFoundException;
import com.naveen.healthcare.payersservice.repository.PayerRepository;
import com.naveen.healthcare.payersservice.service.PayerOrganizationEventPublisher;
import com.naveen.healthcare.payersservice.service.PayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayerServiceImpl
        implements PayerService {

    private final PayerRepository payerRepository;

    private final PayerOrganizationEventPublisher
            payerOrganizationEventPublisher;

    @Override
    @Transactional
    public PayerResponse createPayer(
            CreatePayerRequest request
    ) {

        validateAdminCreateRequest(
                request
        );

        String normalizedAdminEmail =
                normalizeEmail(
                        request.getAdminEmail()
                );

        String normalizedRegistrationNumber =
                normalizeRequiredText(
                        request.getRegistrationNumber(),
                        "Registration number"
                );

        validateOrganizationDoesNotExist(
                normalizedAdminEmail,
                normalizedRegistrationNumber
        );

        PayerOrganization payerOrganization =
                buildPayerOrganization(
                        request.getOrganizationName(),
                        normalizedRegistrationNumber,
                        normalizedAdminEmail,
                        request.getContactNumber(),
                        request.getWebsite(),
                        request.getAddressLine(),
                        request.getCity(),
                        request.getState(),
                        request.getPostalCode(),
                        request.getCountry(),
                        PayerStatus.ACTIVE,
                        null
                );

        PayerOrganization savedPayerOrganization =
                payerRepository.saveAndFlush(
                        payerOrganization
                );

        return mapToResponse(
                savedPayerOrganization
        );
    }

    @Override
    @Transactional
    public PayerResponse createMyOrganization(
            String authenticatedEmail,
            CreateMyPayerOrganizationRequest request
    ) {

        validateSelfCreateRequest(
                request
        );

        String normalizedAdminEmail =
                normalizeEmail(
                        authenticatedEmail
                );

        String normalizedRegistrationNumber =
                normalizeRequiredText(
                        request.getRegistrationNumber(),
                        "Registration number"
                );

        validateOrganizationDoesNotExist(
                normalizedAdminEmail,
                normalizedRegistrationNumber
        );

        LocalDateTime submittedAt =
                LocalDateTime.now();

        PayerOrganization payerOrganization =
                buildPayerOrganization(
                        request.getOrganizationName(),
                        normalizedRegistrationNumber,
                        normalizedAdminEmail,
                        request.getContactNumber(),
                        request.getWebsite(),
                        request.getAddressLine(),
                        request.getCity(),
                        request.getState(),
                        request.getPostalCode(),
                        request.getCountry(),
                        PayerStatus.PENDING_APPROVAL,
                        submittedAt
                );

        PayerOrganization savedPayerOrganization =
                payerRepository.saveAndFlush(
                        payerOrganization
                );

        payerOrganizationEventPublisher
                .publishOrganizationSubmitted(
                        savedPayerOrganization
                );

        return mapToResponse(
                savedPayerOrganization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayerResponse> getAllPayers() {

        return payerRepository
                .findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PayerResponse getPayerById(
            UUID payerId
    ) {

        PayerOrganization payerOrganization =
                findPayerById(
                        payerId
                );

        return mapToResponse(
                payerOrganization
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PayerResponse getMyOrganization(
            String authenticatedEmail
    ) {

        PayerOrganization payerOrganization =
                findPayerByAdminEmail(
                        authenticatedEmail
                );

        return mapToResponse(
                payerOrganization
        );
    }

    @Override
    @Transactional
    public PayerResponse updateMyOrganization(
            String authenticatedEmail,
            UpdatePayerRequest request
    ) {

        validateUpdateRequest(
                request
        );

        PayerOrganization payerOrganization =
                findPayerByAdminEmail(
                        authenticatedEmail
                );

        validatePayerCanUpdateOrganization(
                payerOrganization
        );

        PayerStatus previousStatus =
                payerOrganization.getStatus();

        payerOrganization.setOrganizationName(
                normalizeRequiredText(
                        request.getOrganizationName(),
                        "Organization name"
                )
        );

        payerOrganization.setContactNumber(
                normalizeRequiredText(
                        request.getContactNumber(),
                        "Contact number"
                )
        );

        payerOrganization.setWebsite(
                normalizeOptionalText(
                        request.getWebsite()
                )
        );

        payerOrganization.setAddressLine(
                normalizeRequiredText(
                        request.getAddressLine(),
                        "Address"
                )
        );

        payerOrganization.setCity(
                normalizeRequiredText(
                        request.getCity(),
                        "City"
                )
        );

        payerOrganization.setState(
                normalizeRequiredText(
                        request.getState(),
                        "State"
                )
        );

        payerOrganization.setPostalCode(
                normalizeRequiredText(
                        request.getPostalCode(),
                        "Postal code"
                )
        );

        payerOrganization.setCountry(
                normalizeRequiredText(
                        request.getCountry(),
                        "Country"
                )
        );

        if (previousStatus == PayerStatus.REJECTED) {

            payerOrganization.setStatus(
                    PayerStatus.PENDING_APPROVAL
            );

            payerOrganization.setSubmittedAt(
                    LocalDateTime.now()
            );

            clearReviewInformation(
                    payerOrganization
            );
        }

        PayerOrganization updatedPayerOrganization =
                payerRepository.saveAndFlush(
                        payerOrganization
                );

        if (previousStatus == PayerStatus.REJECTED) {

            payerOrganizationEventPublisher
                    .publishOrganizationSubmitted(
                            updatedPayerOrganization
                    );
        }

        return mapToResponse(
                updatedPayerOrganization
        );
    }

    @Override
    @Transactional
    public PayerResponse approvePayerOrganization(
            UUID payerId,
            String authenticatedAdminEmail,
            ApprovePayerOrganizationRequest request
    ) {

        validateApprovalRequest(
                request
        );

        PayerOrganization payerOrganization =
                findPayerById(
                        payerId
                );

        validatePendingApprovalStatus(
                payerOrganization
        );

        payerOrganization.setStatus(
                PayerStatus.ACTIVE
        );

        payerOrganization.setReviewedAt(
                LocalDateTime.now()
        );

        payerOrganization.setReviewedByEmail(
                normalizeEmail(
                        authenticatedAdminEmail
                )
        );

        payerOrganization.setReviewRemarks(
                normalizeOptionalText(
                        request.getReviewRemarks()
                )
        );

        payerOrganization.setRejectionReason(
                null
        );

        PayerOrganization approvedPayerOrganization =
                payerRepository.saveAndFlush(
                        payerOrganization
                );

        payerOrganizationEventPublisher
                .publishOrganizationApproved(
                        approvedPayerOrganization
                );

        return mapToResponse(
                approvedPayerOrganization
        );
    }

    @Override
    @Transactional
    public PayerResponse rejectPayerOrganization(
            UUID payerId,
            String authenticatedAdminEmail,
            RejectPayerOrganizationRequest request
    ) {

        validateRejectionRequest(
                request
        );

        PayerOrganization payerOrganization =
                findPayerById(
                        payerId
                );

        validatePendingApprovalStatus(
                payerOrganization
        );

        payerOrganization.setStatus(
                PayerStatus.REJECTED
        );

        payerOrganization.setReviewedAt(
                LocalDateTime.now()
        );

        payerOrganization.setReviewedByEmail(
                normalizeEmail(
                        authenticatedAdminEmail
                )
        );

        payerOrganization.setReviewRemarks(
                normalizeOptionalText(
                        request.getReviewRemarks()
                )
        );

        payerOrganization.setRejectionReason(
                normalizeRequiredText(
                        request.getRejectionReason(),
                        "Rejection reason"
                )
        );

        PayerOrganization rejectedPayerOrganization =
                payerRepository.saveAndFlush(
                        payerOrganization
                );

        payerOrganizationEventPublisher
                .publishOrganizationRejected(
                        rejectedPayerOrganization
                );

        return mapToResponse(
                rejectedPayerOrganization
        );
    }

    @Override
    @Transactional
    public PayerResponse updatePayerStatus(
            UUID payerId,
            UpdatePayerStatusRequest request
    ) {

        validateStatusUpdateRequest(
                request
        );

        PayerOrganization payerOrganization =
                findPayerById(
                        payerId
                );

        PayerStatus previousStatus =
                payerOrganization.getStatus();

        PayerStatus requestedStatus =
                request.getStatus();

        if (previousStatus == requestedStatus) {

            throw new InvalidPayerStatusException(
                    "Payer organization is already in "
                            + requestedStatus
                            + " status"
            );
        }

        validateAdministrativeStatusTransition(
                previousStatus,
                requestedStatus
        );

        payerOrganization.setStatus(
                requestedStatus
        );

        PayerOrganization updatedPayerOrganization =
                payerRepository.saveAndFlush(
                        payerOrganization
                );

        publishAdministrativeStatusEvent(
                updatedPayerOrganization,
                previousStatus,
                requestedStatus
        );

        return mapToResponse(
                updatedPayerOrganization
        );
    }

    private void publishAdministrativeStatusEvent(
            PayerOrganization payerOrganization,
            PayerStatus previousStatus,
            PayerStatus requestedStatus
    ) {

        if (requestedStatus == PayerStatus.SUSPENDED) {

            payerOrganizationEventPublisher
                    .publishOrganizationSuspended(
                            payerOrganization
                    );

            return;
        }

        if (requestedStatus == PayerStatus.INACTIVE) {

            payerOrganizationEventPublisher
                    .publishOrganizationDeactivated(
                            payerOrganization
                    );

            return;
        }

        if (
                requestedStatus == PayerStatus.ACTIVE
                        && (
                        previousStatus
                                == PayerStatus.SUSPENDED
                                || previousStatus
                                == PayerStatus.INACTIVE
                )
        ) {

            payerOrganizationEventPublisher
                    .publishOrganizationReactivated(
                            payerOrganization
                    );
        }
    }

    private PayerOrganization buildPayerOrganization(
            String organizationName,
            String registrationNumber,
            String adminEmail,
            String contactNumber,
            String website,
            String addressLine,
            String city,
            String state,
            String postalCode,
            String country,
            PayerStatus status,
            LocalDateTime submittedAt
    ) {

        return PayerOrganization.builder()
                .organizationName(
                        normalizeRequiredText(
                                organizationName,
                                "Organization name"
                        )
                )
                .registrationNumber(
                        normalizeRequiredText(
                                registrationNumber,
                                "Registration number"
                        )
                )
                .adminEmail(
                        normalizeEmail(
                                adminEmail
                        )
                )
                .contactNumber(
                        normalizeRequiredText(
                                contactNumber,
                                "Contact number"
                        )
                )
                .website(
                        normalizeOptionalText(
                                website
                        )
                )
                .addressLine(
                        normalizeRequiredText(
                                addressLine,
                                "Address"
                        )
                )
                .city(
                        normalizeRequiredText(
                                city,
                                "City"
                        )
                )
                .state(
                        normalizeRequiredText(
                                state,
                                "State"
                        )
                )
                .postalCode(
                        normalizeRequiredText(
                                postalCode,
                                "Postal code"
                        )
                )
                .country(
                        normalizeRequiredText(
                                country,
                                "Country"
                        )
                )
                .status(
                        status
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

    private void validateOrganizationDoesNotExist(
            String normalizedAdminEmail,
            String normalizedRegistrationNumber
    ) {

        if (
                payerRepository
                        .existsByAdminEmailIgnoreCase(
                                normalizedAdminEmail
                        )
        ) {

            throw new DuplicatePayerException(
                    "A Payer organization already exists "
                            + "for administrator email: "
                            + normalizedAdminEmail
            );
        }

        if (
                payerRepository
                        .existsByRegistrationNumberIgnoreCase(
                                normalizedRegistrationNumber
                        )
        ) {

            throw new DuplicatePayerException(
                    "A Payer organization already exists "
                            + "with registration number: "
                            + normalizedRegistrationNumber
            );
        }
    }

    private void validatePayerCanUpdateOrganization(
            PayerOrganization payerOrganization
    ) {

        PayerStatus currentStatus =
                payerOrganization.getStatus();

        if (currentStatus == PayerStatus.SUSPENDED) {

            throw new InvalidPayerStatusException(
                    "A suspended Payer organization "
                            + "cannot update its information"
            );
        }

        if (currentStatus == PayerStatus.INACTIVE) {

            throw new InvalidPayerStatusException(
                    "An inactive Payer organization "
                            + "cannot update its information"
            );
        }
    }

    private void validatePendingApprovalStatus(
            PayerOrganization payerOrganization
    ) {

        if (
                payerOrganization.getStatus()
                        != PayerStatus.PENDING_APPROVAL
        ) {

            throw new InvalidPayerStatusException(
                    "Only a Payer organization in "
                            + "PENDING_APPROVAL status "
                            + "can be approved or rejected"
            );
        }
    }

    private void validateAdministrativeStatusTransition(
            PayerStatus currentStatus,
            PayerStatus requestedStatus
    ) {

        boolean validTransition =
                (
                        currentStatus == PayerStatus.ACTIVE
                                && (
                                requestedStatus
                                        == PayerStatus.SUSPENDED
                                        || requestedStatus
                                        == PayerStatus.INACTIVE
                        )
                )
                        || (
                        currentStatus == PayerStatus.SUSPENDED
                                && (
                                requestedStatus
                                        == PayerStatus.ACTIVE
                                        || requestedStatus
                                        == PayerStatus.INACTIVE
                        )
                )
                        || (
                        currentStatus == PayerStatus.INACTIVE
                                && requestedStatus
                                == PayerStatus.ACTIVE
                );

        if (!validTransition) {

            throw new InvalidPayerStatusException(
                    "Invalid Payer organization status "
                            + "transition from "
                            + currentStatus
                            + " to "
                            + requestedStatus
            );
        }
    }

    private void clearReviewInformation(
            PayerOrganization payerOrganization
    ) {

        payerOrganization.setReviewedAt(
                null
        );

        payerOrganization.setReviewedByEmail(
                null
        );

        payerOrganization.setReviewRemarks(
                null
        );

        payerOrganization.setRejectionReason(
                null
        );
    }

    private PayerOrganization findPayerById(
            UUID payerId
    ) {

        if (payerId == null) {

            throw new IllegalArgumentException(
                    "Payer organization ID is required"
            );
        }

        return payerRepository
                .findById(
                        payerId
                )
                .orElseThrow(() ->
                        new PayerNotFoundException(
                                "Payer organization not found with ID: "
                                        + payerId
                        )
                );
    }

    private PayerOrganization findPayerByAdminEmail(
            String adminEmail
    ) {

        String normalizedAdminEmail =
                normalizeEmail(
                        adminEmail
                );

        return payerRepository
                .findByAdminEmailIgnoreCase(
                        normalizedAdminEmail
                )
                .orElseThrow(() ->
                        new PayerNotFoundException(
                                "Payer organization not found "
                                        + "for authenticated email: "
                                        + normalizedAdminEmail
                        )
                );
    }

    private PayerResponse mapToResponse(
            PayerOrganization payerOrganization
    ) {

        return PayerResponse.builder()
                .id(
                        payerOrganization.getId()
                )
                .organizationName(
                        payerOrganization.getOrganizationName()
                )
                .registrationNumber(
                        payerOrganization.getRegistrationNumber()
                )
                .adminEmail(
                        payerOrganization.getAdminEmail()
                )
                .contactNumber(
                        payerOrganization.getContactNumber()
                )
                .website(
                        payerOrganization.getWebsite()
                )
                .addressLine(
                        payerOrganization.getAddressLine()
                )
                .city(
                        payerOrganization.getCity()
                )
                .state(
                        payerOrganization.getState()
                )
                .postalCode(
                        payerOrganization.getPostalCode()
                )
                .country(
                        payerOrganization.getCountry()
                )
                .status(
                        payerOrganization.getStatus()
                )
                .submittedAt(
                        payerOrganization.getSubmittedAt()
                )
                .reviewedAt(
                        payerOrganization.getReviewedAt()
                )
                .reviewedByEmail(
                        payerOrganization.getReviewedByEmail()
                )
                .reviewRemarks(
                        payerOrganization.getReviewRemarks()
                )
                .rejectionReason(
                        payerOrganization.getRejectionReason()
                )
                .createdAt(
                        payerOrganization.getCreatedAt()
                )
                .updatedAt(
                        payerOrganization.getUpdatedAt()
                )
                .build();
    }

    private void validateAdminCreateRequest(
            CreatePayerRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Payer organization creation request is required"
            );
        }
    }

    private void validateSelfCreateRequest(
            CreateMyPayerOrganizationRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Payer organization submission request is required"
            );
        }
    }

    private void validateUpdateRequest(
            UpdatePayerRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Payer organization update request is required"
            );
        }
    }

    private void validateApprovalRequest(
            ApprovePayerOrganizationRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Payer approval request is required"
            );
        }
    }

    private void validateRejectionRequest(
            RejectPayerOrganizationRequest request
    ) {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Payer rejection request is required"
            );
        }
    }

    private void validateStatusUpdateRequest(
            UpdatePayerStatusRequest request
    ) {

        if (request == null) {

            throw new InvalidPayerStatusException(
                    "Payer status update request is required"
            );
        }

        if (request.getStatus() == null) {

            throw new InvalidPayerStatusException(
                    "Payer organization status is required"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (
                email == null
                        || email.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String normalizeRequiredText(
            String value,
            String fieldName
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    fieldName + " is required"
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