package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.request.CreateDoctorAvailabilityRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateAvailabilityStatusRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorAvailabilityRequest;
import com.naveen.healthcare.providerservice.dto.response.DoctorAvailabilityResponse;
import com.naveen.healthcare.providerservice.entity.Department;
import com.naveen.healthcare.providerservice.entity.Doctor;
import com.naveen.healthcare.providerservice.entity.DoctorAvailability;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.AvailabilityStatus;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.DoctorAvailabilityNotFoundException;
import com.naveen.healthcare.providerservice.exception.DoctorNotFoundException;
import com.naveen.healthcare.providerservice.exception.DuplicateDoctorAvailabilityException;
import com.naveen.healthcare.providerservice.exception.InvalidDoctorAvailabilityException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.DoctorAvailabilityRepository;
import com.naveen.healthcare.providerservice.repository.DoctorRepository;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.DoctorAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityServiceImpl
        implements DoctorAvailabilityService {

    private final DoctorAvailabilityRepository
            doctorAvailabilityRepository;

    private final DoctorRepository doctorRepository;

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    @Override
    @Transactional
    public DoctorAvailabilityResponse createAvailability(
            String authenticatedProviderEmail,
            CreateDoctorAvailabilityRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        Doctor doctor =
                findActiveDoctorOwnedByProvider(
                        providerOrganization,
                        request.getDoctorId()
                );

        validateScheduleDetails(
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getSlotDurationMinutes()
        );

        validateNoOverlapForCreation(
                doctor.getId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime()
        );

        DoctorAvailability availability =
                DoctorAvailability.builder()
                        .doctor(doctor)
                        .providerOrganization(
                                providerOrganization
                        )
                        .dayOfWeek(
                                request.getDayOfWeek()
                        )
                        .startTime(
                                request.getStartTime()
                        )
                        .endTime(
                                request.getEndTime()
                        )
                        .slotDurationMinutes(
                                request.getSlotDurationMinutes()
                        )
                        .status(
                                AvailabilityStatus.ACTIVE
                        )
                        .build();

        DoctorAvailability savedAvailability =
                doctorAvailabilityRepository.save(
                        availability
                );

        return mapToResponse(savedAvailability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse> getMyAvailabilities(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        return doctorAvailabilityRepository
                .findAllByProviderOrganizationAdminEmailIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(
                        providerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse>
    getMyDoctorAvailabilities(
            String authenticatedProviderEmail,
            UUID doctorId
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        findDoctorOwnedByProvider(
                providerOrganization,
                doctorId
        );

        return doctorAvailabilityRepository
                .findAllByDoctorIdAndProviderOrganizationAdminEmailIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(
                        doctorId,
                        providerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorAvailabilityResponse getMyAvailabilityById(
            String authenticatedProviderEmail,
            UUID availabilityId
    ) {

        DoctorAvailability availability =
                findAvailabilityOwnedByProvider(
                        authenticatedProviderEmail,
                        availabilityId
                );

        return mapToResponse(availability);
    }

    @Override
    @Transactional
    public DoctorAvailabilityResponse updateMyAvailability(
            String authenticatedProviderEmail,
            UUID availabilityId,
            UpdateDoctorAvailabilityRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        DoctorAvailability availability =
                findAvailabilityOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        availabilityId
                );

        validateAvailabilityIsActiveForUpdate(
                availability
        );

        Doctor doctor = availability.getDoctor();

        validateDoctorAndDepartmentAreActive(doctor);

        validateScheduleDetails(
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getSlotDurationMinutes()
        );

        validateNoOverlapForUpdate(
                doctor.getId(),
                availabilityId,
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime()
        );

        availability.setDayOfWeek(
                request.getDayOfWeek()
        );

        availability.setStartTime(
                request.getStartTime()
        );

        availability.setEndTime(
                request.getEndTime()
        );

        availability.setSlotDurationMinutes(
                request.getSlotDurationMinutes()
        );

        DoctorAvailability updatedAvailability =
                doctorAvailabilityRepository.save(
                        availability
                );

        return mapToResponse(updatedAvailability);
    }

    @Override
    @Transactional
    public DoctorAvailabilityResponse updateMyAvailabilityStatus(
            String authenticatedProviderEmail,
            UUID availabilityId,
            UpdateAvailabilityStatusRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        DoctorAvailability availability =
                findAvailabilityOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        availabilityId
                );

        AvailabilityStatus requestedStatus =
                request.getStatus();

        if (requestedStatus == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Availability status is required"
            );
        }

        if (availability.getStatus() == requestedStatus) {
            throw new InvalidDoctorAvailabilityException(
                    "Doctor availability is already in "
                            + requestedStatus
                            + " status"
            );
        }

        if (requestedStatus == AvailabilityStatus.ACTIVE) {

            Doctor doctor = availability.getDoctor();

            validateDoctorAndDepartmentAreActive(doctor);

            validateNoOverlapForUpdate(
                    doctor.getId(),
                    availability.getId(),
                    availability.getDayOfWeek(),
                    availability.getStartTime(),
                    availability.getEndTime()
            );
        }

        availability.setStatus(requestedStatus);

        DoctorAvailability updatedAvailability =
                doctorAvailabilityRepository.save(
                        availability
                );

        return mapToResponse(updatedAvailability);
    }

    @Override
    @Transactional
    public void deleteMyAvailability(
            String authenticatedProviderEmail,
            UUID availabilityId
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        DoctorAvailability availability =
                findAvailabilityOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        availabilityId
                );

        doctorAvailabilityRepository.delete(availability);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorAvailabilityResponse>
    getAvailableDoctorSchedules(
            UUID doctorId
    ) {

        validateDoctorId(doctorId);

        Doctor doctor =
                doctorRepository
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

        return doctorAvailabilityRepository
                .findAllByDoctorIdAndStatusAndDoctorStatusAndDoctorDepartmentStatusAndProviderOrganizationStatusOrderByDayOfWeekAscStartTimeAsc(
                        doctor.getId(),
                        AvailabilityStatus.ACTIVE,
                        DoctorStatus.ACTIVE,
                        DepartmentStatus.ACTIVE,
                        ProviderStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ProviderOrganization findProviderByEmail(
            String authenticatedProviderEmail
    ) {

        String normalizedProviderEmail =
                normalizeEmail(
                        authenticatedProviderEmail
                );

        return providerOrganizationRepository
                .findByAdminEmailIgnoreCase(
                        normalizedProviderEmail
                )
                .orElseThrow(() ->
                        new ProviderNotFoundException(
                                "Provider organization not found for "
                                        + "authenticated email: "
                                        + normalizedProviderEmail
                        )
                );
    }

    private ProviderOrganization findActiveProviderByEmail(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new InvalidDoctorAvailabilityException(
                    "Provider organization must be ACTIVE "
                            + "to manage Doctor availability"
            );
        }

        return providerOrganization;
    }

    private Doctor findDoctorOwnedByProvider(
            ProviderOrganization providerOrganization,
            UUID doctorId
    ) {

        validateDoctorId(doctorId);

        return doctorRepository
                .findByIdAndProviderOrganizationAdminEmailIgnoreCase(
                        doctorId,
                        providerOrganization.getAdminEmail()
                )
                .orElseThrow(() ->
                        new DoctorNotFoundException(
                                "Doctor not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private Doctor findActiveDoctorOwnedByProvider(
            ProviderOrganization providerOrganization,
            UUID doctorId
    ) {

        Doctor doctor =
                findDoctorOwnedByProvider(
                        providerOrganization,
                        doctorId
                );

        validateDoctorAndDepartmentAreActive(doctor);

        return doctor;
    }

    private void validateDoctorAndDepartmentAreActive(
            Doctor doctor
    ) {

        if (doctor.getStatus() != DoctorStatus.ACTIVE) {
            throw new InvalidDoctorAvailabilityException(
                    "Doctor must be ACTIVE "
                            + "to manage availability"
            );
        }

        Department department = doctor.getDepartment();

        if (department == null
                || department.getStatus()
                != DepartmentStatus.ACTIVE) {

            throw new InvalidDoctorAvailabilityException(
                    "Doctor department must be ACTIVE "
                            + "to manage availability"
            );
        }

        ProviderOrganization providerOrganization =
                doctor.getProviderOrganization();

        if (providerOrganization == null
                || providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new InvalidDoctorAvailabilityException(
                    "Provider organization must be ACTIVE "
                            + "to manage Doctor availability"
            );
        }
    }

    private DoctorAvailability findAvailabilityOwnedByProvider(
            String authenticatedProviderEmail,
            UUID availabilityId
    ) {

        validateAvailabilityId(availabilityId);

        String normalizedProviderEmail =
                normalizeEmail(
                        authenticatedProviderEmail
                );

        return doctorAvailabilityRepository
                .findByIdAndProviderOrganizationAdminEmailIgnoreCase(
                        availabilityId,
                        normalizedProviderEmail
                )
                .orElseThrow(() ->
                        new DoctorAvailabilityNotFoundException(
                                "Doctor availability not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private void validateScheduleDetails(
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime,
            Integer slotDurationMinutes
    ) {

        if (dayOfWeek == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Day of week is required"
            );
        }

        if (startTime == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Start time is required"
            );
        }

        if (endTime == null) {
            throw new InvalidDoctorAvailabilityException(
                    "End time is required"
            );
        }

        if (!startTime.isBefore(endTime)) {
            throw new InvalidDoctorAvailabilityException(
                    "Start time must be before end time"
            );
        }

        if (slotDurationMinutes == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Slot duration is required"
            );
        }

        if (slotDurationMinutes < 10
                || slotDurationMinutes > 120) {

            throw new InvalidDoctorAvailabilityException(
                    "Slot duration must be between "
                            + "10 and 120 minutes"
            );
        }

        long totalScheduleMinutes =
                Duration.between(
                        startTime,
                        endTime
                ).toMinutes();

        if (slotDurationMinutes > totalScheduleMinutes) {
            throw new InvalidDoctorAvailabilityException(
                    "Slot duration cannot exceed "
                            + "the availability time range"
            );
        }

        if (totalScheduleMinutes
                % slotDurationMinutes != 0) {

            throw new InvalidDoctorAvailabilityException(
                    "Availability time range must be exactly "
                            + "divisible by the slot duration"
            );
        }
    }

    private void validateNoOverlapForCreation(
            UUID doctorId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {

        boolean overlapExists =
                doctorAvailabilityRepository
                        .existsOverlappingAvailability(
                                doctorId,
                                dayOfWeek,
                                startTime,
                                endTime
                        );

        if (overlapExists) {
            throw new DuplicateDoctorAvailabilityException(
                    "Doctor already has an overlapping "
                            + "availability schedule on "
                            + dayOfWeek
            );
        }
    }

    private void validateNoOverlapForUpdate(
            UUID doctorId,
            UUID availabilityId,
            DayOfWeek dayOfWeek,
            LocalTime startTime,
            LocalTime endTime
    ) {

        boolean overlapExists =
                doctorAvailabilityRepository
                        .existsOverlappingAvailabilityExcludingId(
                                doctorId,
                                dayOfWeek,
                                startTime,
                                endTime,
                                availabilityId
                        );

        if (overlapExists) {
            throw new DuplicateDoctorAvailabilityException(
                    "Doctor already has an overlapping "
                            + "availability schedule on "
                            + dayOfWeek
            );
        }
    }

    private void validateAvailabilityIsActiveForUpdate(
            DoctorAvailability availability
    ) {

        if (availability.getStatus()
                != AvailabilityStatus.ACTIVE) {

            throw new InvalidDoctorAvailabilityException(
                    "An INACTIVE Doctor availability "
                            + "cannot be updated"
            );
        }
    }

    private void validateDoctorId(
            UUID doctorId
    ) {

        if (doctorId == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Doctor ID is required"
            );
        }
    }

    private void validateAvailabilityId(
            UUID availabilityId
    ) {

        if (availabilityId == null) {
            throw new InvalidDoctorAvailabilityException(
                    "Availability ID is required"
            );
        }
    }

    private DoctorAvailabilityResponse mapToResponse(
            DoctorAvailability availability
    ) {

        Doctor doctor = availability.getDoctor();

        Department department = doctor.getDepartment();

        ProviderOrganization providerOrganization =
                availability.getProviderOrganization();

        return DoctorAvailabilityResponse.builder()
                .id(
                        availability.getId()
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
                .departmentId(
                        department.getId()
                )
                .departmentName(
                        department.getDepartmentName()
                )
                .providerId(
                        providerOrganization.getId()
                )
                .providerOrganizationName(
                        providerOrganization.getOrganizationName()
                )
                .dayOfWeek(
                        availability.getDayOfWeek()
                )
                .startTime(
                        availability.getStartTime()
                )
                .endTime(
                        availability.getEndTime()
                )
                .slotDurationMinutes(
                        availability.getSlotDurationMinutes()
                )
                .status(
                        availability.getStatus()
                )
                .createdAt(
                        availability.getCreatedAt()
                )
                .updatedAt(
                        availability.getUpdatedAt()
                )
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

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidDoctorAvailabilityException(
                    "Authenticated Provider email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}