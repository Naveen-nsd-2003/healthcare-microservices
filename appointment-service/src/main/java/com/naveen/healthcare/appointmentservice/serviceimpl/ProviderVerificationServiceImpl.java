package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.naveen.healthcare.appointmentservice.client.ProviderServiceClient;
import com.naveen.healthcare.appointmentservice.dto.response.ProviderBookingVerificationResponse;
import com.naveen.healthcare.appointmentservice.exception.ProviderVerificationException;
import com.naveen.healthcare.appointmentservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.appointmentservice.service.ProviderVerificationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProviderVerificationServiceImpl
        implements ProviderVerificationService {

    private final ProviderServiceClient providerServiceClient;

    @Override
    public ProviderBookingVerificationResponse verifyBookingSlot(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        validateRequest(
                doctorId,
                appointmentDate,
                startTime
        );

        ProviderBookingVerificationResponse response =
                retrieveProviderVerification(
                        doctorId,
                        appointmentDate,
                        startTime
                );

        validateVerificationResponse(
                response,
                doctorId,
                appointmentDate,
                startTime
        );

        return response;
    }

    private ProviderBookingVerificationResponse
    retrieveProviderVerification(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        try {

            return providerServiceClient.verifyBookingSlot(
                    doctorId,
                    appointmentDate,
                    startTime
            );

        } catch (RetryableException exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider verification is temporarily unavailable",
                    exception
            );

        } catch (FeignException.NotFound exception) {

            throw new ProviderVerificationException(
                    "The selected Doctor is not currently available "
                            + "for appointment booking"
            );

        } catch (FeignException.BadRequest exception) {

            throw new ProviderVerificationException(
                    "The requested appointment time does not match "
                            + "an available Doctor schedule"
            );

        } catch (FeignException.Unauthorized exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service rejected the forwarded "
                            + "authentication token",
                    exception
            );

        } catch (FeignException.Forbidden exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service rejected the forwarded "
                            + "Member authorization",
                    exception
            );

        } catch (FeignException exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected response",
                    exception
            );
        }
    }

    private void validateRequest(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        if (doctorId == null) {
            throw new ProviderVerificationException(
                    "Doctor ID is required"
            );
        }

        if (appointmentDate == null) {
            throw new ProviderVerificationException(
                    "Appointment date is required"
            );
        }

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new ProviderVerificationException(
                    "Appointment date cannot be in the past"
            );
        }

        if (startTime == null) {
            throw new ProviderVerificationException(
                    "Appointment start time is required"
            );
        }
    }

    private void validateVerificationResponse(
            ProviderBookingVerificationResponse response,
            UUID requestedDoctorId,
            LocalDate requestedAppointmentDate,
            LocalTime requestedStartTime
    ) {

        if (response == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an empty response"
            );
        }

        if (response.getProviderId() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Provider ID"
            );
        }

        if (response.getProviderOrganizationName() == null
                || response.getProviderOrganizationName().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Provider organization name"
            );
        }

        if (response.getDepartmentId() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Department ID"
            );
        }

        if (response.getDepartmentName() == null
                || response.getDepartmentName().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Department name"
            );
        }

        if (response.getDoctorId() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Doctor ID"
            );
        }

        if (!requestedDoctorId.equals(response.getDoctorId())) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned details for "
                            + "an unexpected Doctor"
            );
        }

        if (response.getDoctorName() == null
                || response.getDoctorName().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Doctor name"
            );
        }

        if (response.getSpecialization() == null
                || response.getSpecialization().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "the Doctor specialization"
            );
        }

        if (response.getConsultationFee() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "the consultation fee"
            );
        }

        if (response.getConsultationFee().signum() < 0) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an invalid "
                            + "consultation fee"
            );
        }

        if (response.getAppointmentDate() == null
                || !requestedAppointmentDate.equals(
                response.getAppointmentDate()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected "
                            + "appointment date"
            );
        }

        if (response.getStartTime() == null
                || !requestedStartTime.equals(
                response.getStartTime()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected "
                            + "appointment start time"
            );
        }

        if (response.getEndTime() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "an appointment end time"
            );
        }

        if (!response.getStartTime().isBefore(
                response.getEndTime()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an invalid "
                            + "appointment time range"
            );
        }

        if (response.getSlotDurationMinutes() == null
                || response.getSlotDurationMinutes() <= 0) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an invalid "
                            + "slot duration"
            );
        }

        if (!Boolean.TRUE.equals(response.getAvailable())) {
            throw new ProviderVerificationException(
                    "The requested Doctor timeslot is not available"
            );
        }
    }
}