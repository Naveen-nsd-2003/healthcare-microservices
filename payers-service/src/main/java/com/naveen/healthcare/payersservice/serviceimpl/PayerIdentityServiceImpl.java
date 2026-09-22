package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.dto.response.PayerIdentityResponse;
import com.naveen.healthcare.payersservice.entity.PayerOrganization;
import com.naveen.healthcare.payersservice.enums.PayerStatus;
import com.naveen.healthcare.payersservice.exception.InvalidPayerOperationException;
import com.naveen.healthcare.payersservice.exception.PayerNotFoundException;
import com.naveen.healthcare.payersservice.repository.PayerOrganizationRepository;
import com.naveen.healthcare.payersservice.service.PayerIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PayerIdentityServiceImpl
        implements PayerIdentityService {

    private final PayerOrganizationRepository
            payerOrganizationRepository;

    @Override
    @Transactional(readOnly = true)
    public PayerIdentityResponse getAuthenticatedPayerIdentity(
            String authenticatedPayerEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedPayerEmail);

        PayerOrganization payerOrganization =
                payerOrganizationRepository
                        .findByAdminEmailIgnoreCase(
                                normalizedEmail
                        )
                        .orElseThrow(() ->
                                new PayerNotFoundException(
                                        "Payer organization not found "
                                                + "for authenticated email: "
                                                + normalizedEmail
                                )
                        );

        if (payerOrganization.getStatus()
                != PayerStatus.ACTIVE) {

            throw new InvalidPayerOperationException(
                    "Payer organization must be ACTIVE "
                            + "to manage claims"
            );
        }

        return PayerIdentityResponse.builder()
                .payerId(
                        payerOrganization.getId()
                )
                .adminEmail(
                        payerOrganization.getAdminEmail()
                )
                .payerOrganizationName(
                        payerOrganization.getOrganizationName()
                )
                .status(
                        payerOrganization.getStatus()
                )
                .build();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidPayerOperationException(
                    "Authenticated Payer email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}