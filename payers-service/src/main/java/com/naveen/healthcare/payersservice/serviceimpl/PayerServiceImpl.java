package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.request.CreatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PayerResponse;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import com.naveen.healthcare.payersservice.exception.DuplicatePayerException;
import com.naveen.healthcare.payersservice.exception.PayerNotFoundException;
import com.naveen.healthcare.payersservice.repository.PayerRepository;
import com.naveen.healthcare.payersservice.service.PayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PayerServiceImpl
        implements PayerService {


    private final PayerRepository payerRepository;


    // =====================================================
    // CREATE PAYER ORGANIZATION
    // =====================================================

    @Override
    @Transactional
    public PayerResponse createPayer(
            CreatePayerRequest request
    ) {


        String normalizedEmail =
                normalizeEmail(request.getAdminEmail());


        String normalizedRegistrationNumber =
                normalizeRequiredText(
                        request.getRegistrationNumber()
                );


        // Check whether the PAYER email is already linked
        if (payerRepository.existsByAdminEmailIgnoreCase(
                normalizedEmail
        )) {

            throw new DuplicatePayerException(
                    "A payer organization already exists for admin email: "
                            + normalizedEmail
            );
        }


        // Check organization registration number
        if (payerRepository.existsByRegistrationNumberIgnoreCase(
                normalizedRegistrationNumber
        )) {

            throw new DuplicatePayerException(
                    "Payer registration number already exists: "
                            + normalizedRegistrationNumber
            );
        }


        PayerOrganization payerOrganization =
                PayerOrganization.builder()

                        .organizationName(
                                normalizeRequiredText(
                                        request.getOrganizationName()
                                )
                        )

                        .registrationNumber(
                                normalizedRegistrationNumber
                        )

                        .adminEmail(
                                normalizedEmail
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

                        .status(
                                PayerStatus.ACTIVE
                        )

                        .build();


        PayerOrganization savedPayer =
                payerRepository.save(payerOrganization);


        return mapToResponse(savedPayer);
    }


    // =====================================================
    // GET ALL PAYERS
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<PayerResponse> getAllPayers() {


        return payerRepository

                .findAll()

                .stream()

                .map(this::mapToResponse)

                .toList();
    }


    // =====================================================
    // GET PAYER BY ID
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PayerResponse getPayerById(
            UUID payerId
    ) {


        PayerOrganization payerOrganization =
                findPayerById(payerId);


        return mapToResponse(payerOrganization);
    }


    // =====================================================
    // GET AUTHENTICATED PAYER ORGANIZATION
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public PayerResponse getMyOrganization(
            String authenticatedEmail
    ) {


        PayerOrganization payerOrganization =
                findPayerByAdminEmail(
                        authenticatedEmail
                );


        return mapToResponse(payerOrganization);
    }


    // =====================================================
    // UPDATE AUTHENTICATED PAYER ORGANIZATION
    // =====================================================

    @Override
    @Transactional
    public PayerResponse updateMyOrganization(
            String authenticatedEmail,
            UpdatePayerRequest request
    ) {


        PayerOrganization payerOrganization =
                findPayerByAdminEmail(
                        authenticatedEmail
                );


        payerOrganization.setOrganizationName(
                normalizeRequiredText(
                        request.getOrganizationName()
                )
        );


        payerOrganization.setContactNumber(
                normalizeRequiredText(
                        request.getContactNumber()
                )
        );


        payerOrganization.setWebsite(
                normalizeOptionalText(
                        request.getWebsite()
                )
        );


        payerOrganization.setAddressLine(
                normalizeRequiredText(
                        request.getAddressLine()
                )
        );


        payerOrganization.setCity(
                normalizeRequiredText(
                        request.getCity()
                )
        );


        payerOrganization.setState(
                normalizeRequiredText(
                        request.getState()
                )
        );


        payerOrganization.setPostalCode(
                normalizeRequiredText(
                        request.getPostalCode()
                )
        );


        payerOrganization.setCountry(
                normalizeRequiredText(
                        request.getCountry()
                )
        );


        PayerOrganization updatedPayer =
                payerRepository.save(payerOrganization);


        return mapToResponse(updatedPayer);
    }


    // =====================================================
    // UPDATE PAYER STATUS
    // =====================================================

    @Override
    @Transactional
    public PayerResponse updatePayerStatus(
            UUID payerId,
            UpdatePayerStatusRequest request
    ) {


        PayerOrganization payerOrganization =
                findPayerById(payerId);


        payerOrganization.setStatus(
                request.getStatus()
        );


        PayerOrganization updatedPayer =
                payerRepository.save(payerOrganization);


        return mapToResponse(updatedPayer);
    }


    // =====================================================
    // FIND PAYER BY ID
    // =====================================================

    private PayerOrganization findPayerById(
            UUID payerId
    ) {


        return payerRepository

                .findById(payerId)

                .orElseThrow(() ->

                        new PayerNotFoundException(
                                "Payer organization not found with ID: "
                                        + payerId
                        )
                );
    }


    // =====================================================
    // FIND PAYER BY ADMIN EMAIL
    // =====================================================

    private PayerOrganization findPayerByAdminEmail(
            String adminEmail
    ) {


        String normalizedEmail =
                normalizeEmail(adminEmail);


        return payerRepository

                .findByAdminEmailIgnoreCase(
                        normalizedEmail
                )

                .orElseThrow(() ->

                        new PayerNotFoundException(
                                "Payer organization not found for authenticated email: "
                                        + normalizedEmail
                        )
                );
    }


    // =====================================================
    // ENTITY TO RESPONSE DTO
    // =====================================================

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

                .createdAt(
                        payerOrganization.getCreatedAt()
                )

                .updatedAt(
                        payerOrganization.getUpdatedAt()
                )

                .build();
    }


    // =====================================================
    // NORMALIZATION UTILITIES
    // =====================================================

    private String normalizeEmail(
            String email
    ) {


        return email

                .trim()

                .toLowerCase(Locale.ROOT);
    }


    private String normalizeRequiredText(
            String value
    ) {


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
}