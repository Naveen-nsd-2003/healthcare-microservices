package com.naveen.healthcare.payersservice.serviceimpl;

import com.naveen.healthcare.payersservice.client.MemberServiceClient;
import com.naveen.healthcare.payersservice.dto.response.MemberVerificationResponse;
import com.naveen.healthcare.payersservice.exception.MemberServiceCommunicationException;
import com.naveen.healthcare.payersservice.exception.MemberVerificationException;
import com.naveen.healthcare.payersservice.service.MemberVerificationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
@Service
@RequiredArgsConstructor
public class MemberVerificationServiceImpl
        implements MemberVerificationService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final MemberServiceClient memberServiceClient;

    @Override
    @Retry(name = "memberVerification")
    @CircuitBreaker(
            name = "memberVerification",
            fallbackMethod = "memberVerificationFallback"
    )
    public MemberVerificationResponse verifyActiveMember(
            String authenticatedEmail
    ) {

        String normalizedAuthenticatedEmail =
                normalizeEmail(authenticatedEmail);

        MemberVerificationResponse response =
                retrieveMemberVerification();

        validateVerificationResponse(
                response,
                normalizedAuthenticatedEmail
        );

        return response;
    }

    private MemberVerificationResponse
    memberVerificationFallback(
            String authenticatedEmail,
            Throwable throwable
    ) {

        throw new MemberServiceCommunicationException(
                "Member verification is temporarily unavailable. "
                        + "Please try again later.",
                throwable
        );
    }

    private MemberVerificationResponse retrieveMemberVerification() {

        try {

            return memberServiceClient
                    .getAuthenticatedMemberVerification();

        } catch (RetryableException exception) {

            throw new MemberServiceCommunicationException(
                    "Member verification is temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            handleFeignException(exception);

            throw new MemberServiceCommunicationException(
                    "Member verification failed because of "
                            + "an unexpected remote response",
                    exception
            );
        }
    }

    private void handleFeignException(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 404) {

            throw new MemberVerificationException(
                    "Member healthcare profile must be completed "
                            + "before applying for insurance"
            );
        }

        if (status == 401) {

            throw new MemberServiceCommunicationException(
                    "Member Service rejected the forwarded "
                            + "authentication token",
                    exception
            );
        }

        if (status == 403) {

            throw new MemberServiceCommunicationException(
                    "Member Service rejected the forwarded "
                            + "Member authorization",
                    exception
            );
        }

        if (status == 503) {

            throw new MemberServiceCommunicationException(
                    "Member verification is temporarily unavailable",
                    exception
            );
        }

        throw new MemberServiceCommunicationException(
                "Member Service returned an unexpected response",
                exception
        );
    }

    private void validateVerificationResponse(
            MemberVerificationResponse response,
            String authenticatedEmail
    ) {

        if (response == null) {

            throw new MemberServiceCommunicationException(
                    "Member Service returned an empty verification response"
            );
        }

        if (response.getMemberId() == null) {

            throw new MemberServiceCommunicationException(
                    "Member Service response does not contain "
                            + "a Member profile ID"
            );
        }

        if (response.getEmail() == null
                || response.getEmail().isBlank()) {

            throw new MemberServiceCommunicationException(
                    "Member Service response does not contain "
                            + "a Member email"
            );
        }

        if (response.getDateOfBirth() == null) {

            throw new MemberVerificationException(
                    "Member date of birth is required "
                            + "for insurance eligibility verification"
            );
        }

        if (response.getStatus() == null
                || response.getStatus().isBlank()) {

            throw new MemberServiceCommunicationException(
                    "Member Service response does not contain "
                            + "a Member profile status"
            );
        }

        String returnedEmail =
                normalizeEmail(response.getEmail());

        if (!authenticatedEmail.equals(returnedEmail)) {

            throw new MemberServiceCommunicationException(
                    "Member Service returned verification details "
                            + "for an unexpected identity"
            );
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                response.getStatus()
        )) {

            throw new MemberVerificationException(
                    "Only an ACTIVE Member profile can apply "
                            + "for insurance"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new MemberVerificationException(
                    "Authenticated Member email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}