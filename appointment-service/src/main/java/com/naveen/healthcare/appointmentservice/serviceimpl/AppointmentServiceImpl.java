package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.naveen.healthcare.appointmentservice.client.ProviderServiceClient;
import com.naveen.healthcare.appointmentservice.dto.request.BookAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.CancelAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.CompleteAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.ProviderCancelAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.response.AppointmentResponse;
import com.naveen.healthcare.appointmentservice.dto.response.MemberVerificationResponse;
import com.naveen.healthcare.appointmentservice.dto.response.ProviderBookingVerificationResponse;
import com.naveen.healthcare.appointmentservice.dto.response.ProviderIdentityResponse;
import com.naveen.healthcare.appointmentservice.entity.Appointment;
import com.naveen.healthcare.appointmentservice.enums.AppointmentStatus;
import com.naveen.healthcare.appointmentservice.enums.CancelledBy;
import com.naveen.healthcare.appointmentservice.exception.AppointmentNotFoundException;
import com.naveen.healthcare.appointmentservice.exception.DuplicateAppointmentException;
import com.naveen.healthcare.appointmentservice.exception.InvalidAppointmentOperationException;
import com.naveen.healthcare.appointmentservice.exception.ProviderVerificationException;
import com.naveen.healthcare.appointmentservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.appointmentservice.repository.AppointmentRepository;
import com.naveen.healthcare.appointmentservice.service.AppointmentEventPublisher;
import com.naveen.healthcare.appointmentservice.service.AppointmentService;
import com.naveen.healthcare.appointmentservice.service.MemberVerificationService;
import com.naveen.healthcare.appointmentservice.service.ProviderVerificationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl
        implements AppointmentService {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private static final List<AppointmentStatus>
            SLOT_BLOCKING_STATUSES = List.of(
            AppointmentStatus.BOOKED,
            AppointmentStatus.CONFIRMED
    );

    private final AppointmentRepository appointmentRepository;

    private final MemberVerificationService
            memberVerificationService;

    private final ProviderVerificationService
            providerVerificationService;

    private final ProviderServiceClient providerServiceClient;

    private final AppointmentEventPublisher
            appointmentEventPublisher;

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(
            String authenticatedMemberEmail,
            BookAppointmentRequest request
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        validateBookingDateAndTime(
                request.getAppointmentDate(),
                request.getStartTime()
        );

        MemberVerificationResponse memberVerification =
                memberVerificationService.verifyActiveMember(
                        normalizedMemberEmail
                );

        ProviderBookingVerificationResponse
                providerVerification =
                providerVerificationService.verifyBookingSlot(
                        request.getDoctorId(),
                        request.getAppointmentDate(),
                        request.getStartTime()
                );
        validateProviderBookingVerification(
                providerVerification
        );

        validateDoctorSlotIsNotBooked(
                providerVerification.getDoctorId(),
                providerVerification.getAppointmentDate(),
                providerVerification.getStartTime()
        );

        validateMemberHasNoTimeConflict(
                memberVerification.getMemberId(),
                providerVerification.getAppointmentDate(),
                providerVerification.getStartTime()
        );

        Appointment appointment =
                Appointment.builder()
                        .memberId(
                                memberVerification.getMemberId()
                        )
                        .memberEmail(
                                normalizeEmail(
                                        memberVerification.getEmail()
                                )
                        )
                        .providerId(
                                providerVerification.getProviderId()
                        )
                        .providerOrganizationName(
                                normalizeRequiredText(
                                        providerVerification
                                                .getProviderOrganizationName()
                                )
                        )
                        .providerAdminEmail(
                                normalizeEmail(
                                        providerVerification
                                                .getProviderAdminEmail()
                                )
                        )
                        .departmentId(
                                providerVerification.getDepartmentId()
                        )
                        .departmentName(
                                normalizeRequiredText(
                                        providerVerification
                                                .getDepartmentName()
                                )
                        )
                        .doctorId(
                                providerVerification.getDoctorId()
                        )
                        .doctorName(
                                normalizeRequiredText(
                                        providerVerification.getDoctorName()
                                )
                        )
                        .doctorSpecialization(
                                normalizeRequiredText(
                                        providerVerification
                                                .getSpecialization()
                                )
                        )
                        .consultationFee(
                                providerVerification.getConsultationFee()
                        )
                        .appointmentDate(
                                providerVerification.getAppointmentDate()
                        )
                        .startTime(
                                providerVerification.getStartTime()
                        )
                        .endTime(
                                providerVerification.getEndTime()
                        )
                        .slotDurationMinutes(
                                providerVerification
                                        .getSlotDurationMinutes()
                        )
                        .reasonForVisit(
                                normalizeRequiredText(
                                        request.getReasonForVisit()
                                )
                        )
                        .status(AppointmentStatus.BOOKED)
                        .build();

        Appointment savedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        appointmentEventPublisher
                .publishAppointmentBooked(
                        savedAppointment
                );

        return mapToResponse(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getMyAppointments(
            String authenticatedMemberEmail
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        return appointmentRepository
                .findAllByMemberEmailIgnoreCaseOrderByAppointmentDateDescStartTimeDesc(
                        normalizedMemberEmail
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getMyAppointmentById(
            String authenticatedMemberEmail,
            UUID appointmentId
    ) {

        Appointment appointment =
                findAppointmentOwnedByMember(
                        authenticatedMemberEmail,
                        appointmentId
                );

        return mapToResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelMyAppointment(
            String authenticatedMemberEmail,
            UUID appointmentId,
            CancelAppointmentRequest request
    ) {

        Appointment appointment =
                findAppointmentOwnedByMember(
                        authenticatedMemberEmail,
                        appointmentId
                );

        validateAppointmentCanBeCancelled(appointment);

        validateAppointmentHasNotStarted(appointment);

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointment.setCancelledBy(
                CancelledBy.MEMBER
        );

        appointment.setCancellationReason(
                normalizeRequiredText(
                        request.getCancellationReason()
                )
        );

        appointment.setCancelledAt(
                LocalDateTime.now()
        );

        Appointment updatedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        appointmentEventPublisher
                .publishAppointmentCancelled(
                        updatedAppointment
                );

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getProviderAppointments(
            String authenticatedProviderEmail
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveActiveProviderIdentity(
                        authenticatedProviderEmail
                );

        return appointmentRepository
                .findAllByProviderIdOrderByAppointmentDateDescStartTimeDesc(
                        providerIdentity.getProviderId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getProviderAppointmentById(
            String authenticatedProviderEmail,
            UUID appointmentId
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveActiveProviderIdentity(
                        authenticatedProviderEmail
                );

        Appointment appointment =
                findAppointmentOwnedByProvider(
                        providerIdentity.getProviderId(),
                        appointmentId
                );

        return mapToResponse(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponse confirmAppointment(
            String authenticatedProviderEmail,
            UUID appointmentId
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveActiveProviderIdentity(
                        authenticatedProviderEmail
                );

        Appointment appointment =
                findAppointmentOwnedByProvider(
                        providerIdentity.getProviderId(),
                        appointmentId
                );

        if (appointment.getStatus()
                != AppointmentStatus.BOOKED) {

            throw new InvalidAppointmentOperationException(
                    "Only a BOOKED appointment can be confirmed"
            );
        }

        validateAppointmentHasNotStarted(appointment);

        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        appointment.setConfirmedAt(
                LocalDateTime.now()
        );

        Appointment updatedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        appointmentEventPublisher
                .publishAppointmentConfirmed(
                        updatedAppointment
                );

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse cancelProviderAppointment(
            String authenticatedProviderEmail,
            UUID appointmentId,
            ProviderCancelAppointmentRequest request
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveActiveProviderIdentity(
                        authenticatedProviderEmail
                );

        Appointment appointment =
                findAppointmentOwnedByProvider(
                        providerIdentity.getProviderId(),
                        appointmentId
                );

        validateAppointmentCanBeCancelled(appointment);

        appointment.setStatus(
                AppointmentStatus.CANCELLED
        );

        appointment.setCancelledBy(
                CancelledBy.PROVIDER
        );

        appointment.setCancellationReason(
                normalizeRequiredText(
                        request.getCancellationReason()
                )
        );

        appointment.setCancelledAt(
                LocalDateTime.now()
        );

        Appointment updatedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        appointmentEventPublisher
                .publishAppointmentCancelled(
                        updatedAppointment
                );

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(
            String authenticatedProviderEmail,
            UUID appointmentId,
            CompleteAppointmentRequest request
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveActiveProviderIdentity(
                        authenticatedProviderEmail
                );

        Appointment appointment =
                findAppointmentOwnedByProvider(
                        providerIdentity.getProviderId(),
                        appointmentId
                );

        if (appointment.getStatus()
                != AppointmentStatus.CONFIRMED) {

            throw new InvalidAppointmentOperationException(
                    "Only a CONFIRMED appointment can be completed"
            );
        }

        validateAppointmentCanBeCompleted(appointment);

        appointment.setStatus(
                AppointmentStatus.COMPLETED
        );

        appointment.setProviderNotes(
                normalizeRequiredText(
                        request.getProviderNotes()
                )
        );

        appointment.setCompletedAt(
                LocalDateTime.now()
        );

        Appointment updatedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        appointmentEventPublisher
                .publishAppointmentCompleted(
                        updatedAppointment
                );

        return mapToResponse(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponse markAppointmentAsNoShow(
            String authenticatedProviderEmail,
            UUID appointmentId
    ) {

        ProviderIdentityResponse providerIdentity =
                retrieveActiveProviderIdentity(
                        authenticatedProviderEmail
                );

        Appointment appointment =
                findAppointmentOwnedByProvider(
                        providerIdentity.getProviderId(),
                        appointmentId
                );

        if (appointment.getStatus()
                != AppointmentStatus.CONFIRMED) {

            throw new InvalidAppointmentOperationException(
                    "Only a CONFIRMED appointment can be marked "
                            + "as NO_SHOW"
            );
        }

        validateAppointmentCanBeCompleted(appointment);

        appointment.setStatus(
                AppointmentStatus.NO_SHOW
        );

        appointment.setNoShowAt(
                LocalDateTime.now()
        );

        Appointment updatedAppointment =
                appointmentRepository.saveAndFlush(
                        appointment
                );

        appointmentEventPublisher
                .publishAppointmentNoShow(
                        updatedAppointment
                );

        return mapToResponse(updatedAppointment);
    }

    private ProviderIdentityResponse retrieveActiveProviderIdentity(
            String authenticatedProviderEmail
    ) {

        String normalizedProviderEmail =
                normalizeEmail(authenticatedProviderEmail);

        ProviderIdentityResponse response;

        try {

            response =
                    providerServiceClient
                            .getAuthenticatedProviderIdentity();

        } catch (RetryableException exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider identity verification is "
                            + "temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            handleProviderIdentityFeignException(exception);

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected response",
                    exception
            );
        }

        validateProviderIdentityResponse(
                response,
                normalizedProviderEmail
        );

        return response;
    }

    private void handleProviderIdentityFeignException(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400 || status == 404) {
            throw new ProviderVerificationException(
                    "An ACTIVE Provider organization is required "
                            + "to manage appointments"
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

    private void validateProviderIdentityResponse(
            ProviderIdentityResponse response,
            String authenticatedProviderEmail
    ) {

        if (response == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an empty "
                            + "identity response"
            );
        }

        if (response.getProviderId() == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "a Provider ID"
            );
        }

        if (response.getAdminEmail() == null
                || response.getAdminEmail().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "an administrator email"
            );
        }

        String returnedProviderEmail =
                normalizeEmail(response.getAdminEmail());

        if (!authenticatedProviderEmail.equals(
                returnedProviderEmail
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned identity details "
                            + "for an unexpected Provider"
            );
        }

        if (response.getProviderOrganizationName() == null
                || response.getProviderOrganizationName().isBlank()) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service response does not contain "
                            + "an organization name"
            );
        }

        if (!ACTIVE_STATUS.equalsIgnoreCase(
                response.getStatus()
        )) {

            throw new ProviderVerificationException(
                    "Only an ACTIVE Provider organization "
                            + "can manage appointments"
            );
        }
    }

    private Appointment findAppointmentOwnedByMember(
            String authenticatedMemberEmail,
            UUID appointmentId
    ) {

        validateAppointmentId(appointmentId);

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        return appointmentRepository
                .findByIdAndMemberEmailIgnoreCase(
                        appointmentId,
                        normalizedMemberEmail
                )
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "Appointment not found for "
                                        + "the authenticated Member"
                        )
                );
    }

    private Appointment findAppointmentOwnedByProvider(
            UUID providerId,
            UUID appointmentId
    ) {

        validateAppointmentId(appointmentId);

        if (providerId == null) {
            throw new InvalidAppointmentOperationException(
                    "Provider ID is required"
            );
        }

        return appointmentRepository
                .findByIdAndProviderId(
                        appointmentId,
                        providerId
                )
                .orElseThrow(() ->
                        new AppointmentNotFoundException(
                                "Appointment not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private void validateDoctorSlotIsNotBooked(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        boolean doctorSlotOccupied =
                appointmentRepository
                        .existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
                                doctorId,
                                appointmentDate,
                                startTime,
                                SLOT_BLOCKING_STATUSES
                        );

        if (doctorSlotOccupied) {
            throw new DuplicateAppointmentException(
                    "The selected Doctor timeslot is already booked"
            );
        }
    }

    private void validateMemberHasNoTimeConflict(
            UUID memberId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        boolean memberTimeConflict =
                appointmentRepository
                        .existsByMemberIdAndAppointmentDateAndStartTimeAndStatusIn(
                                memberId,
                                appointmentDate,
                                startTime,
                                SLOT_BLOCKING_STATUSES
                        );

        if (memberTimeConflict) {
            throw new DuplicateAppointmentException(
                    "The Member already has another appointment "
                            + "at the selected date and time"
            );
        }
    }

    private void validateBookingDateAndTime(
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        if (appointmentDate == null) {
            throw new InvalidAppointmentOperationException(
                    "Appointment date is required"
            );
        }

        if (startTime == null) {
            throw new InvalidAppointmentOperationException(
                    "Appointment start time is required"
            );
        }

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new InvalidAppointmentOperationException(
                    "Appointment date cannot be in the past"
            );
        }

        if (appointmentDate.equals(LocalDate.now())
                && !startTime.isAfter(LocalTime.now())) {

            throw new InvalidAppointmentOperationException(
                    "Appointment start time must be in the future"
            );
        }
    }

    private void validateAppointmentCanBeCancelled(
            Appointment appointment
    ) {

        AppointmentStatus status =
                appointment.getStatus();

        if (status != AppointmentStatus.BOOKED
                && status != AppointmentStatus.CONFIRMED) {

            throw new InvalidAppointmentOperationException(
                    "Only a BOOKED or CONFIRMED appointment "
                            + "can be cancelled"
            );
        }
    }

    private void validateAppointmentHasNotStarted(
            Appointment appointment
    ) {

        LocalDateTime appointmentStart =
                LocalDateTime.of(
                        appointment.getAppointmentDate(),
                        appointment.getStartTime()
                );

        if (!appointmentStart.isAfter(LocalDateTime.now())) {
            throw new InvalidAppointmentOperationException(
                    "The appointment has already started or passed"
            );
        }
    }

    private void validateAppointmentCanBeCompleted(
            Appointment appointment
    ) {

        LocalDateTime appointmentEnd =
                LocalDateTime.of(
                        appointment.getAppointmentDate(),
                        appointment.getEndTime()
                );

        if (appointmentEnd.isAfter(LocalDateTime.now())) {
            throw new InvalidAppointmentOperationException(
                    "The appointment cannot be completed or marked "
                            + "as NO_SHOW before its scheduled end time"
            );
        }
    }

    private AppointmentResponse mapToResponse(
            Appointment appointment
    ) {

        return AppointmentResponse.builder()
                .id(
                        appointment.getId()
                )
                .memberId(
                        appointment.getMemberId()
                )
                .memberEmail(
                        appointment.getMemberEmail()
                )
                .providerId(
                        appointment.getProviderId()
                )
                .providerOrganizationName(
                        appointment.getProviderOrganizationName()
                )
                .departmentId(
                        appointment.getDepartmentId()
                )
                .departmentName(
                        appointment.getDepartmentName()
                )
                .doctorId(
                        appointment.getDoctorId()
                )
                .doctorName(
                        appointment.getDoctorName()
                )
                .doctorSpecialization(
                        appointment.getDoctorSpecialization()
                )
                .consultationFee(
                        appointment.getConsultationFee()
                )
                .appointmentDate(
                        appointment.getAppointmentDate()
                )
                .startTime(
                        appointment.getStartTime()
                )
                .endTime(
                        appointment.getEndTime()
                )
                .slotDurationMinutes(
                        appointment.getSlotDurationMinutes()
                )
                .reasonForVisit(
                        appointment.getReasonForVisit()
                )
                .status(
                        appointment.getStatus()
                )
                .providerNotes(
                        appointment.getProviderNotes()
                )
                .confirmedAt(
                        appointment.getConfirmedAt()
                )
                .cancelledBy(
                        appointment.getCancelledBy()
                )
                .cancellationReason(
                        appointment.getCancellationReason()
                )
                .cancelledAt(
                        appointment.getCancelledAt()
                )
                .completedAt(
                        appointment.getCompletedAt()
                )
                .noShowAt(
                        appointment.getNoShowAt()
                )
                .createdAt(
                        appointment.getCreatedAt()
                )
                .updatedAt(
                        appointment.getUpdatedAt()
                )
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
                    "Authenticated email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private void validateProviderBookingVerification(
            ProviderBookingVerificationResponse response
    ) {

        if (response == null) {
            throw new ProviderVerificationException(
                    "Provider Service returned an empty "
                            + "booking verification response"
            );
        }

        if (response.getProviderId() == null) {
            throw new ProviderVerificationException(
                    "Provider booking verification does not "
                            + "contain a Provider ID"
            );
        }

        if (response.getProviderOrganizationName() == null
                || response
                .getProviderOrganizationName()
                .isBlank()) {

            throw new ProviderVerificationException(
                    "Provider booking verification does not "
                            + "contain an organization name"
            );
        }

        if (response.getProviderAdminEmail() == null
                || response
                .getProviderAdminEmail()
                .isBlank()) {

            throw new ProviderVerificationException(
                    "Provider booking verification does not "
                            + "contain an administrator email"
            );
        }

        if (response.getDepartmentId() == null
                || response.getDepartmentName() == null
                || response.getDepartmentName().isBlank()) {

            throw new ProviderVerificationException(
                    "Provider booking verification does not "
                            + "contain complete Department information"
            );
        }

        if (response.getDoctorId() == null
                || response.getDoctorName() == null
                || response.getDoctorName().isBlank()) {

            throw new ProviderVerificationException(
                    "Provider booking verification does not "
                            + "contain complete Doctor information"
            );
        }

        if (response.getAppointmentDate() == null
                || response.getStartTime() == null
                || response.getEndTime() == null) {

            throw new ProviderVerificationException(
                    "Provider booking verification does not "
                            + "contain complete appointment timing"
            );
        }

        if (!Boolean.TRUE.equals(
                response.getAvailable()
        )) {

            throw new ProviderVerificationException(
                    "The selected Doctor appointment slot "
                            + "is not available"
            );
        }
    }
    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidAppointmentOperationException(
                    "Required appointment information cannot be empty"
            );
        }

        return value.trim();
    }
}