package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.response.ProviderBookingVerificationResponse;
import com.naveen.healthcare.providerservice.entity.Department;
import com.naveen.healthcare.providerservice.entity.Doctor;
import com.naveen.healthcare.providerservice.entity.DoctorAvailability;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.AvailabilityStatus;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.DoctorNotFoundException;
import com.naveen.healthcare.providerservice.exception.InvalidDoctorAvailabilityException;
import com.naveen.healthcare.providerservice.repository.DoctorAvailabilityRepository;
import com.naveen.healthcare.providerservice.repository.DoctorRepository;
import com.naveen.healthcare.providerservice.service.ProviderBookingVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.naveen.healthcare.providerservice.dto.response.InternalAvailabilityWindowResponse;
import com.naveen.healthcare.providerservice.dto.response.InternalDoctorScheduleResponse;

@Service
@RequiredArgsConstructor
public class ProviderBookingVerificationServiceImpl
        implements ProviderBookingVerificationService {

    private final DoctorRepository doctorRepository;

    private final DoctorAvailabilityRepository
            doctorAvailabilityRepository;

    @Override
    @Transactional(readOnly = true)
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

        Doctor doctor =
                findAvailableDoctor(doctorId);

        List<DoctorAvailability> schedules =
                findAvailableSchedules(
                        doctorId
                );

        DoctorAvailability matchingSchedule =
                findMatchingSchedule(
                        schedules,
                        appointmentDate,
                        startTime
                );

        LocalTime endTime =
                startTime.plusMinutes(
                        matchingSchedule.getSlotDurationMinutes()
                );

        return mapToResponse(
                doctor,
                matchingSchedule,
                appointmentDate,
                startTime,
                endTime
        );
    }

    private Doctor findAvailableDoctor(
            UUID doctorId
    ) {

        return doctorRepository
                .findByIdAndStatusAndDepartmentStatusAndProviderOrganizationStatus(
                        doctorId,
                        DoctorStatus.ACTIVE,
                        DepartmentStatus.ACTIVE,
                        ProviderStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new DoctorNotFoundException(
                                "Active Doctor not found with ID: "
                                        + doctorId
                        )
                );
    }

    private List<DoctorAvailability> findAvailableSchedules(
            UUID doctorId
    ) {

        List<DoctorAvailability> schedules =
                doctorAvailabilityRepository
                        .findAllByDoctorIdAndStatusAndDoctorStatusAndDoctorDepartmentStatusAndProviderOrganizationStatusOrderByDayOfWeekAscStartTimeAsc(
                                doctorId,
                                AvailabilityStatus.ACTIVE,
                                DoctorStatus.ACTIVE,
                                DepartmentStatus.ACTIVE,
                                ProviderStatus.ACTIVE
                        );

        if (schedules.isEmpty()) {
            throw new InvalidDoctorAvailabilityException(
                    "The Doctor does not have any active "
                            + "availability schedules"
            );
        }

        return schedules;
    }

    private DoctorAvailability findMatchingSchedule(
            List<DoctorAvailability> schedules,
            LocalDate appointmentDate,
            LocalTime requestedStartTime
    ) {

        return schedules
                .stream()
                .filter(schedule ->
                        schedule.getDayOfWeek()
                                == appointmentDate.getDayOfWeek()
                )
                .filter(schedule ->
                        isValidSlotStart(
                                schedule,
                                requestedStartTime
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new InvalidDoctorAvailabilityException(
                                "The requested appointment time "
                                        + "does not match an active "
                                        + "Doctor availability slot"
                        )
                );
    }

    private boolean isValidSlotStart(
            DoctorAvailability schedule,
            LocalTime requestedStartTime
    ) {

        LocalTime scheduleStartTime =
                schedule.getStartTime();

        LocalTime scheduleEndTime =
                schedule.getEndTime();

        Integer slotDurationMinutes =
                schedule.getSlotDurationMinutes();

        if (scheduleStartTime == null
                || scheduleEndTime == null
                || slotDurationMinutes == null
                || slotDurationMinutes <= 0) {

            return false;
        }

        if (requestedStartTime.isBefore(
                scheduleStartTime
        )) {
            return false;
        }

        LocalTime requestedEndTime =
                requestedStartTime.plusMinutes(
                        slotDurationMinutes
                );

        if (requestedEndTime.isAfter(
                scheduleEndTime
        )) {
            return false;
        }

        long minutesFromScheduleStart =
                Duration.between(
                        scheduleStartTime,
                        requestedStartTime
                ).toMinutes();

        return minutesFromScheduleStart
                % slotDurationMinutes == 0;
    }

    private void validateRequest(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        if (doctorId == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Doctor ID is required"
            );
        }

        if (appointmentDate == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Appointment date is required"
            );
        }

        if (appointmentDate.isBefore(
                LocalDate.now()
        )) {
            throw new InvalidDoctorAvailabilityException(
                    "Appointment date cannot be in the past"
            );
        }

        if (startTime == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Appointment start time is required"
            );
        }
    }

    private ProviderBookingVerificationResponse mapToResponse(
            Doctor doctor,
            DoctorAvailability schedule,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime
    ) {

        Department department =
                doctor.getDepartment();

        ProviderOrganization providerOrganization =
                doctor.getProviderOrganization();

        return ProviderBookingVerificationResponse.builder()
                .providerId(
                        providerOrganization.getId()
                )
                .providerOrganizationName(
                        providerOrganization.getOrganizationName()
                )
                .departmentId(
                        department.getId()
                )
                .departmentName(
                        department.getDepartmentName()
                )
                .doctorId(
                        doctor.getId()
                )
                .doctorName(
                        createDoctorName(doctor)
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .consultationFee(
                        doctor.getConsultationFee()
                )
                .appointmentDate(
                        appointmentDate
                )
                .startTime(
                        startTime
                )
                .endTime(
                        endTime
                )
                .slotDurationMinutes(
                        schedule.getSlotDurationMinutes()
                )
                .available(true)
                .build();
    }

    private String createDoctorName(
            Doctor doctor
    ) {

        return "Dr. "
                + doctor.getFirstName().trim()
                + " "
                + doctor.getLastName().trim();
    }

    @Override
    @Transactional(readOnly = true)
    public InternalDoctorScheduleResponse getDoctorScheduleForDate(
            UUID doctorId,
            LocalDate appointmentDate
    ) {

        validateScheduleRequest(
                doctorId,
                appointmentDate
        );

        Doctor doctor =
                findAvailableDoctor(doctorId);

        List<DoctorAvailability> availabilityWindows =
                doctorAvailabilityRepository
                        .findAllByDoctorIdAndStatusAndDoctorStatusAndDoctorDepartmentStatusAndProviderOrganizationStatusOrderByDayOfWeekAscStartTimeAsc(
                                doctorId,
                                AvailabilityStatus.ACTIVE,
                                DoctorStatus.ACTIVE,
                                DepartmentStatus.ACTIVE,
                                ProviderStatus.ACTIVE
                        )
                        .stream()
                        .filter(availability ->
                                availability.getDayOfWeek()
                                        == appointmentDate.getDayOfWeek()
                        )
                        .toList();

        Department department =
                doctor.getDepartment();

        ProviderOrganization providerOrganization =
                doctor.getProviderOrganization();

        List<InternalAvailabilityWindowResponse> windows =
                availabilityWindows
                        .stream()
                        .map(availability ->
                                InternalAvailabilityWindowResponse
                                        .builder()
                                        .startTime(
                                                availability.getStartTime()
                                        )
                                        .endTime(
                                                availability.getEndTime()
                                        )
                                        .slotDurationMinutes(
                                                availability
                                                        .getSlotDurationMinutes()
                                        )
                                        .build()
                        )
                        .toList();

        return InternalDoctorScheduleResponse.builder()
                .providerId(
                        providerOrganization.getId()
                )
                .providerOrganizationName(
                        providerOrganization.getOrganizationName()
                )
                .departmentId(
                        department.getId()
                )
                .departmentName(
                        department.getDepartmentName()
                )
                .doctorId(
                        doctor.getId()
                )
                .doctorName(
                        createDoctorName(doctor)
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .appointmentDate(
                        appointmentDate
                )
                .dayOfWeek(
                        appointmentDate.getDayOfWeek()
                )
                .availabilityWindows(
                        windows
                )
                .build();
    }

    private void validateScheduleRequest(
            UUID doctorId,
            LocalDate appointmentDate
    ) {

        if (doctorId == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Doctor ID is required"
            );
        }

        if (appointmentDate == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Appointment date is required"
            );
        }

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new InvalidDoctorAvailabilityException(
                    "Appointment date cannot be in the past"
            );
        }
    }
}