package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.naveen.healthcare.appointmentservice.client.MemberServiceClient;
import com.naveen.healthcare.appointmentservice.dto.response.MemberVerificationResponse;
import com.naveen.healthcare.appointmentservice.exception.MemberVerificationException;
import com.naveen.healthcare.appointmentservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.appointmentservice.service.MemberVerificationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;


import java.util.Locale;

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
            String authenticatedMemberEmail
    ) {

        String normalizedAuthenticatedEmail =
                normalizeEmail(authenticatedMemberEmail);

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
            String authenticatedMemberEmail,
            Throwable throwable
    ) {

        throw new RemoteServiceCommunicationException(
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

            throw new RemoteServiceCommunicationException(
                    "Member verification is temporarily unavailable",
                    exception
            );

        } catch (FeignException.NotFound exception) {

            throw new MemberVerificationException(
                    "Member healthcare profile must be completed "
                            + "before booking an appointment"
            );

        } catch (FeignException.Unauthorized exception) {

            throw new RemoteServiceCommunicationException(
                    "Member Service rejected the forwarded "
                            + "authentication token",
                    exception
            );

        } catch (FeignException.Forbidden exception) {

            throw new RemoteServiceCommunicationException(
                    "Member Service rejected the forwarded "
                            + "Member authorization",
                    exception
            );

        } catch (FeignException exception) {

            throw new RemoteServiceCommunicationException(
                    "Member Service returned an unexpected response",
                    exception
            );
        }
    }

    private void validateVerificationResponse(
            MemberVerificationResponse response,
            String authenticatedMemberEmail
    ) {

        if (response == null) {
            throw new RemoteServiceCommunicationException(
                    "Member Service returned an empty response"
            );
        }

        if (response.getMemberId() == null) {
            throw new RemoteServiceCommunicationException(
                    "Member Service response does not contain "
                            + "a Member ID"
            );
        }

        if (response.getEmail() == null
                || response.getEmail().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Member Service response does not contain "
                            + "a Member email"
            );
        }

        if (response.getDateOfBirth() == null) {
            throw new MemberVerificationException(
                    "Member date of birth is required "
                            + "for appointment booking"
            );
        }

        if (response.getStatus() == null
                || response.getStatus().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Member Service response does not contain "
                            + "a Member profile status"
            );
        }

        String returnedEmail =
                normalizeEmail(response.getEmail());

        if (!authenticatedMemberEmail.equals(returnedEmail)) {
            throw new RemoteServiceCommunicationException(
                    "Member Service returned verification details "
                            + "for an unexpected identity"
            );
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                response.getStatus()
        )) {

            throw new MemberVerificationException(
                    "Only an ACTIVE Member profile can book "
                            + "an appointment"
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