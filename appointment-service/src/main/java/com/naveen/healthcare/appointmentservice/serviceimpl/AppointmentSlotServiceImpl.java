package com.naveen.healthcare.appointmentservice.serviceimpl;

import com.naveen.healthcare.appointmentservice.client.ProviderServiceClient;
import com.naveen.healthcare.appointmentservice.dto.response.AppointmentSlotResponse;
import com.naveen.healthcare.appointmentservice.dto.response.AvailabilityWindowResponse;
import com.naveen.healthcare.appointmentservice.dto.response.AvailableSlotsResponse;
import com.naveen.healthcare.appointmentservice.dto.response.DoctorScheduleResponse;
import com.naveen.healthcare.appointmentservice.entity.Appointment;
import com.naveen.healthcare.appointmentservice.enums.AppointmentStatus;
import com.naveen.healthcare.appointmentservice.exception.ProviderVerificationException;
import com.naveen.healthcare.appointmentservice.exception.RemoteServiceCommunicationException;
import com.naveen.healthcare.appointmentservice.repository.AppointmentRepository;
import com.naveen.healthcare.appointmentservice.service.AppointmentSlotService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentSlotServiceImpl
        implements AppointmentSlotService {

    private static final List<AppointmentStatus>
            SLOT_BLOCKING_STATUSES = List.of(
            AppointmentStatus.BOOKED,
            AppointmentStatus.CONFIRMED
    );

    private final ProviderServiceClient providerServiceClient;

    private final AppointmentRepository appointmentRepository;

    @Override
    @Transactional(readOnly = true)
    public AvailableSlotsResponse getAvailableSlots(
            UUID doctorId,
            LocalDate appointmentDate
    ) {

        validateRequest(
                doctorId,
                appointmentDate
        );

        DoctorScheduleResponse schedule =
                retrieveDoctorSchedule(
                        doctorId,
                        appointmentDate
                );

        validateScheduleResponse(
                schedule,
                doctorId,
                appointmentDate
        );

        List<Appointment> occupiedAppointments =
                appointmentRepository
                        .findAllByDoctorIdAndAppointmentDateAndStatusInOrderByStartTimeAsc(
                                doctorId,
                                appointmentDate,
                                SLOT_BLOCKING_STATUSES
                        );

        Set<LocalTime> occupiedStartTimes =
                new HashSet<>();

        for (Appointment appointment : occupiedAppointments) {
            occupiedStartTimes.add(
                    appointment.getStartTime()
            );
        }

        List<AppointmentSlotResponse> availableSlots =
                generateAvailableSlots(
                        schedule.getAvailabilityWindows(),
                        appointmentDate,
                        occupiedStartTimes
                );

        return AvailableSlotsResponse.builder()
                .providerId(schedule.getProviderId())
                .providerOrganizationName(
                        schedule.getProviderOrganizationName()
                )
                .departmentId(schedule.getDepartmentId())
                .departmentName(schedule.getDepartmentName())
                .doctorId(schedule.getDoctorId())
                .doctorName(schedule.getDoctorName())
                .specialization(schedule.getSpecialization())
                .appointmentDate(schedule.getAppointmentDate())
                .dayOfWeek(schedule.getDayOfWeek())
                .availableSlots(availableSlots)
                .build();
    }

    private DoctorScheduleResponse retrieveDoctorSchedule(
            UUID doctorId,
            LocalDate appointmentDate
    ) {

        try {

            return providerServiceClient
                    .getDoctorScheduleForDate(
                            doctorId,
                            appointmentDate
                    );

        } catch (RetryableException exception) {

            throw new RemoteServiceCommunicationException(
                    "Provider schedule verification is "
                            + "temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            if (exception.status() == 400
                    || exception.status() == 404) {

                throw new ProviderVerificationException(
                        "The selected Doctor is not available "
                                + "for the requested date"
                );
            }

            if (exception.status() == 401
                    || exception.status() == 403) {

                throw new RemoteServiceCommunicationException(
                        "Provider Service rejected the forwarded "
                                + "Member authorization",
                        exception
                );
            }

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an unexpected response",
                    exception
            );
        }
    }

    private List<AppointmentSlotResponse>
    generateAvailableSlots(
            List<AvailabilityWindowResponse> windows,
            LocalDate appointmentDate,
            Set<LocalTime> occupiedStartTimes
    ) {

        List<AppointmentSlotResponse> availableSlots =
                new ArrayList<>();

        if (windows == null || windows.isEmpty()) {
            return availableSlots;
        }

        for (AvailabilityWindowResponse window : windows) {

            validateWindow(window);

            LocalTime currentStart =
                    window.getStartTime();

            while (true) {

                LocalTime currentEnd =
                        currentStart.plusMinutes(
                                window.getSlotDurationMinutes()
                        );

                if (currentEnd.isAfter(window.getEndTime())) {
                    break;
                }

                if (isFutureSlot(
                        appointmentDate,
                        currentStart
                ) && !occupiedStartTimes.contains(currentStart)) {

                    availableSlots.add(
                            AppointmentSlotResponse.builder()
                                    .startTime(currentStart)
                                    .endTime(currentEnd)
                                    .build()
                    );
                }

                currentStart = currentEnd;
            }
        }

        return availableSlots;
    }

    private boolean isFutureSlot(
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        if (appointmentDate.isAfter(LocalDate.now())) {
            return true;
        }

        return startTime.isAfter(LocalTime.now());
    }

    private void validateRequest(
            UUID doctorId,
            LocalDate appointmentDate
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
    }

    private void validateScheduleResponse(
            DoctorScheduleResponse schedule,
            UUID requestedDoctorId,
            LocalDate requestedAppointmentDate
    ) {

        if (schedule == null) {
            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an empty schedule"
            );
        }

        if (schedule.getDoctorId() == null
                || !requestedDoctorId.equals(
                schedule.getDoctorId()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned a schedule "
                            + "for an unexpected Doctor"
            );
        }

        if (schedule.getAppointmentDate() == null
                || !requestedAppointmentDate.equals(
                schedule.getAppointmentDate()
        )) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned a schedule "
                            + "for an unexpected date"
            );
        }

        if (schedule.getProviderId() == null
                || schedule.getDepartmentId() == null) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned incomplete "
                            + "schedule information"
            );
        }
    }

    private void validateWindow(
            AvailabilityWindowResponse window
    ) {

        if (window == null
                || window.getStartTime() == null
                || window.getEndTime() == null
                || window.getSlotDurationMinutes() == null
                || window.getSlotDurationMinutes() <= 0
                || !window.getStartTime()
                .isBefore(window.getEndTime())) {

            throw new RemoteServiceCommunicationException(
                    "Provider Service returned an invalid "
                            + "availability window"
            );
        }
    }
}