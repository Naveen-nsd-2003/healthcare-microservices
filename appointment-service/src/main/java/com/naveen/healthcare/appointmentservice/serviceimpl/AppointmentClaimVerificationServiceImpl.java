package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.naveen.healthcare.appointmentservice.client.ProviderServiceClient;
import com.naveen.healthcare.appointmentservice.dto.response.ClaimAppointmentVerificationResponse;
import com.naveen.healthcare.appointmentservice.dto.response.ProviderIdentityResponse;
import com.naveen.healthcare.appointmentservice.entity.Appointment;
import com.naveen.healthcare.appointmentservice.enums.AppointmentStatus;
import com.naveen.healthcare.appointmentservice.exception.AppointmentNotFoundException;
import com.naveen.healthcare.appointmentservice.exception.InvalidAppointmentOperationException;
import com.naveen.healthcare.appointmentservice.exception.ProviderVerificationException;
import com.naveen.healthcare.appointmentservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.appointmentservice.repository.AppointmentRepository;
import com.naveen.healthcare.appointmentservice.service.AppointmentClaimVerificationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentClaimVerificationServiceImpl
        implements AppointmentClaimVerificationService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final AppointmentRepository appointmentRepository;

    private final ProviderServiceClient providerServiceClient;

    @Override
    @Transactional(readOnly = true)
    public ClaimAppointmentVerificationResponse verifyAppointmentForClaim(
            String authenticatedProviderEmail,
            UUID appointmentId
    ) {

        validateAppointmentId(appointmentId);

        String normalizedProviderEmail =
                normalizeEmail(authenticatedProviderEmail);

        ProviderIdentityResponse providerIdentity =
                retrieveProviderIdentity();

        validateProviderIdentity(
                providerIdentity,
                normalizedProviderEmail
        );

        Appointment appointment =
                appointmentRepository
                        .findByIdAndProviderId(
                                appointmentId,
                                providerIdentity.getProviderId()
                        )
                        .orElseThrow(() ->
                                new AppointmentNotFoundException(
                                        "Completed appointment not found for "
                                                + "the authenticated Provider"
                                )
                        );

        validateCompletedAppointment(appointment);

        return mapToResponse(appointment);
    }

    private ProviderIdentityResponse retrieveProviderIdentity() {

        try {

            return providerServiceClient
                    .getAuthenticatedProviderIdentity();

        } catch (RetryableException exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider identity verification is "
                            + "temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            handleProviderServiceFailure(exception);

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected response",
                    exception
            );
        }
    }

    private void handleProviderServiceFailure(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400 || status == 404) {
            throw new ProviderVerificationException(
                    "An ACTIVE Provider organization is required "
                            + "to submit a claim"
            );
        }

        if (status == 401) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service rejected the forwarded "
                            + "authentication token",
                    exception
            );
        }

        if (status == 403) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service rejected the forwarded "
                            + "Provider authorization",
                    exception
            );
        }

        if (status == 503) {
            throw new RemoteServiceCommunicationException(
                    "Provider identity verification is "
                            + "temporarily unavailable",
                    exception
            );
        }

        throw new RemoteServiceCommunicationException(
                "Provider Service returned an unexpected response",
                exception
        );
    }

    private void validateProviderIdentity(
            ProviderIdentityResponse providerIdentity,
            String authenticatedProviderEmail
    ) {

        if (providerIdentity == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an empty identity response"
            );
        }

        if (providerIdentity.getProviderId() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Provider ID"
            );
        }

        if (providerIdentity.getAdminEmail() == null
                || providerIdentity.getAdminEmail().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Provider administrator email"
            );
        }

        String returnedEmail =
                normalizeEmail(providerIdentity.getAdminEmail());

        if (!authenticatedProviderEmail.equals(returnedEmail)) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned identity details "
                            + "for an unexpected Provider"
            );
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                providerIdentity.getStatus()
        )) {

            throw new ProviderVerificationException(
                    "Only an ACTIVE Provider organization "
                            + "can submit a claim"
            );
        }
    }

    private void validateCompletedAppointment(
            Appointment appointment
    ) {

        if (appointment.getStatus()
                != AppointmentStatus.COMPLETED) {

            throw new InvalidAppointmentOperationException(
                    "A claim can be submitted only for "
                            + "a COMPLETED appointment"
            );
        }

        if (appointment.getCompletedAt() == null) {
            throw new InvalidAppointmentOperationException(
                    "The completed appointment does not contain "
                            + "a completion timestamp"
            );
        }

        if (appointment.getMemberId() == null
                || appointment.getMemberEmail() == null
                || appointment.getMemberEmail().isBlank()) {

            throw new InvalidAppointmentOperationException(
                    "The appointment does not contain valid "
                            + "Member information"
            );
        }

        if (appointment.getProviderId() == null
                || appointment.getDepartmentId() == null
                || appointment.getDoctorId() == null) {

            throw new InvalidAppointmentOperationException(
                    "The appointment does not contain complete "
                            + "Provider information"
            );
        }

        if (appointment.getConsultationFee() == null
                || appointment.getConsultationFee().signum() < 0) {

            throw new InvalidAppointmentOperationException(
                    "The appointment contains an invalid "
                            + "consultation fee"
            );
        }

        if (appointment.getAppointmentDate() == null
                || appointment.getStartTime() == null
                || appointment.getEndTime() == null
                || !appointment.getStartTime().isBefore(
                appointment.getEndTime()
        )) {

            throw new InvalidAppointmentOperationException(
                    "The appointment contains invalid timing information"
            );
        }
    }

    private ClaimAppointmentVerificationResponse mapToResponse(
            Appointment appointment
    ) {

        return ClaimAppointmentVerificationResponse.builder()
                .appointmentId(appointment.getId())
                .memberId(appointment.getMemberId())
                .memberEmail(appointment.getMemberEmail())
                .providerId(appointment.getProviderId())
                .providerOrganizationName(
                        appointment.getProviderOrganizationName()
                )
                .departmentId(appointment.getDepartmentId())
                .departmentName(appointment.getDepartmentName())
                .doctorId(appointment.getDoctorId())
                .doctorName(appointment.getDoctorName())
                .doctorSpecialization(
                        appointment.getDoctorSpecialization()
                )
                .consultationFee(
                        appointment.getConsultationFee()
                )
                .appointmentDate(
                        appointment.getAppointmentDate()
                )
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus())
                .build();
    }

    private void validateAppointmentId(
            UUID appointmentId
    ) {

        if (appointmentId == null) {
            throw new InvalidAppointmentOperationException(
                    "Appointment ID is required"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidAppointmentOperationException(
                    "Authenticated Provider email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}