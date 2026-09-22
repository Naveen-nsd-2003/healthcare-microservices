package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.request.CreateDoctorRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.DoctorResponse;
import com.naveen.healthcare.providerservice.entity.Department;
import com.naveen.healthcare.providerservice.entity.Doctor;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.DepartmentNotFoundException;
import com.naveen.healthcare.providerservice.exception.DoctorNotFoundException;
import com.naveen.healthcare.providerservice.exception.DuplicateDoctorException;
import com.naveen.healthcare.providerservice.exception.InvalidDoctorOperationException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.DepartmentRepository;
import com.naveen.healthcare.providerservice.repository.DoctorRepository;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    private final DepartmentRepository departmentRepository;

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    @Override
    @Transactional
    public DoctorResponse createDoctor(
            String authenticatedProviderEmail,
            CreateDoctorRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        Department department =
                findActiveDepartmentOwnedByProvider(
                        providerOrganization,
                        request.getDepartmentId()
                );

        String medicalRegistrationNumber =
                normalizeBusinessIdentifier(
                        request.getMedicalRegistrationNumber()
                );

        if (doctorRepository
                .existsByMedicalRegistrationNumberIgnoreCase(
                        medicalRegistrationNumber
                )) {

            throw new DuplicateDoctorException(
                    "A Doctor already exists with medical "
                            + "registration number: "
                            + medicalRegistrationNumber
            );
        }

        Doctor doctor =
                Doctor.builder()
                        .firstName(
                                normalizeRequiredText(
                                        request.getFirstName()
                                )
                        )
                        .lastName(
                                normalizeRequiredText(
                                        request.getLastName()
                                )
                        )
                        .medicalRegistrationNumber(
                                medicalRegistrationNumber
                        )
                        .qualification(
                                normalizeRequiredText(
                                        request.getQualification()
                                )
                        )
                        .specialization(
                                normalizeRequiredText(
                                        request.getSpecialization()
                                )
                        )
                        .yearsOfExperience(
                                request.getYearsOfExperience()
                        )
                        .consultationFee(
                                request.getConsultationFee()
                        )
                        .biography(
                                normalizeOptionalText(
                                        request.getBiography()
                                )
                        )
                        .providerOrganization(
                                providerOrganization
                        )
                        .department(
                                department
                        )
                        .status(
                                DoctorStatus.ACTIVE
                        )
                        .build();

        Doctor savedDoctor =
                doctorRepository.save(doctor);

        return mapToResponse(savedDoctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getMyDoctors(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        return doctorRepository
                .findAllByProviderOrganizationAdminEmailIgnoreCaseOrderByFirstNameAscLastNameAsc(
                        providerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getMyDoctorById(
            String authenticatedProviderEmail,
            UUID doctorId
    ) {

        Doctor doctor =
                findDoctorOwnedByProvider(
                        authenticatedProviderEmail,
                        doctorId
                );

        return mapToResponse(doctor);
    }

    @Override
    @Transactional
    public DoctorResponse updateMyDoctor(
            String authenticatedProviderEmail,
            UUID doctorId,
            UpdateDoctorRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        Doctor doctor =
                findDoctorOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        doctorId
                );

        validateDoctorIsActiveForUpdate(doctor);

        Department department =
                findActiveDepartmentOwnedByProvider(
                        providerOrganization,
                        request.getDepartmentId()
                );

        doctor.setFirstName(
                normalizeRequiredText(
                        request.getFirstName()
                )
        );

        doctor.setLastName(
                normalizeRequiredText(
                        request.getLastName()
                )
        );

        doctor.setQualification(
                normalizeRequiredText(
                        request.getQualification()
                )
        );

        doctor.setSpecialization(
                normalizeRequiredText(
                        request.getSpecialization()
                )
        );

        doctor.setYearsOfExperience(
                request.getYearsOfExperience()
        );

        doctor.setConsultationFee(
                request.getConsultationFee()
        );

        doctor.setBiography(
                normalizeOptionalText(
                        request.getBiography()
                )
        );

        doctor.setDepartment(department);

        Doctor updatedDoctor =
                doctorRepository.save(doctor);

        return mapToResponse(updatedDoctor);
    }

    @Override
    @Transactional
    public DoctorResponse updateMyDoctorStatus(
            String authenticatedProviderEmail,
            UUID doctorId,
            UpdateDoctorStatusRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        Doctor doctor =
                findDoctorOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        doctorId
                );

        DoctorStatus requestedStatus =
                request.getStatus();

        if (requestedStatus == null) {
            throw new InvalidDoctorOperationException(
                    "Doctor status is required"
            );
        }

        if (doctor.getStatus() == requestedStatus) {
            throw new InvalidDoctorOperationException(
                    "Doctor is already in "
                            + requestedStatus
                            + " status"
            );
        }

        if (requestedStatus == DoctorStatus.ACTIVE
                && doctor.getDepartment().getStatus()
                != DepartmentStatus.ACTIVE) {

            throw new InvalidDoctorOperationException(
                    "Doctor cannot be activated because "
                            + "the assigned department is not ACTIVE"
            );
        }

        doctor.setStatus(requestedStatus);

        Doctor updatedDoctor =
                doctorRepository.save(doctor);

        return mapToResponse(updatedDoctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAvailableDoctors() {

        return doctorRepository
                .findAllByStatusAndDepartmentStatusAndProviderOrganizationStatusOrderByFirstNameAscLastNameAsc(
                        DoctorStatus.ACTIVE,
                        DepartmentStatus.ACTIVE,
                        ProviderStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getAvailableDoctorById(
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

        return mapToResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAvailableDoctorsByProvider(
            UUID providerId
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderById(providerId);

        return doctorRepository
                .findAllByProviderOrganizationIdAndStatusAndDepartmentStatusAndProviderOrganizationStatusOrderByFirstNameAscLastNameAsc(
                        providerOrganization.getId(),
                        DoctorStatus.ACTIVE,
                        DepartmentStatus.ACTIVE,
                        ProviderStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAvailableDoctorsByDepartment(
            UUID departmentId
    ) {

        Department department =
                findAvailableDepartmentById(departmentId);

        return doctorRepository
                .findAllByDepartmentIdAndStatusAndDepartmentStatusAndProviderOrganizationStatusOrderByFirstNameAscLastNameAsc(
                        department.getId(),
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

        validateProviderIsActive(providerOrganization);

        return providerOrganization;
    }

    private ProviderOrganization findActiveProviderById(
            UUID providerId
    ) {

        if (providerId == null) {
            throw new InvalidDoctorOperationException(
                    "Provider ID is required"
            );
        }

        ProviderOrganization providerOrganization =
                providerOrganizationRepository
                        .findById(providerId)
                        .orElseThrow(() ->
                                new ProviderNotFoundException(
                                        "Provider organization not found with ID: "
                                                + providerId
                                )
                        );

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new ProviderNotFoundException(
                    "Active Provider organization not found with ID: "
                            + providerId
            );
        }

        return providerOrganization;
    }

    private Department findActiveDepartmentOwnedByProvider(
            ProviderOrganization providerOrganization,
            UUID departmentId
    ) {

        if (departmentId == null) {
            throw new InvalidDoctorOperationException(
                    "Department ID is required"
            );
        }

        Department department =
                departmentRepository
                        .findByIdAndProviderOrganizationAdminEmailIgnoreCase(
                                departmentId,
                                providerOrganization.getAdminEmail()
                        )
                        .orElseThrow(() ->
                                new DepartmentNotFoundException(
                                        "Department not found for "
                                                + "the authenticated Provider"
                                )
                        );

        if (department.getStatus()
                != DepartmentStatus.ACTIVE) {

            throw new InvalidDoctorOperationException(
                    "Department must be ACTIVE "
                            + "to assign a Doctor"
            );
        }

        return department;
    }

    private Department findAvailableDepartmentById(
            UUID departmentId
    ) {

        if (departmentId == null) {
            throw new InvalidDoctorOperationException(
                    "Department ID is required"
            );
        }

        return departmentRepository
                .findByIdAndStatusAndProviderOrganizationStatus(
                        departmentId,
                        DepartmentStatus.ACTIVE,
                        ProviderStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new DepartmentNotFoundException(
                                "Active department not found with ID: "
                                        + departmentId
                        )
                );
    }

    private Doctor findDoctorOwnedByProvider(
            String authenticatedProviderEmail,
            UUID doctorId
    ) {

        validateDoctorId(doctorId);

        String normalizedProviderEmail =
                normalizeEmail(
                        authenticatedProviderEmail
                );

        return doctorRepository
                .findByIdAndProviderOrganizationAdminEmailIgnoreCase(
                        doctorId,
                        normalizedProviderEmail
                )
                .orElseThrow(() ->
                        new DoctorNotFoundException(
                                "Doctor not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private void validateProviderIsActive(
            ProviderOrganization providerOrganization
    ) {

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new InvalidDoctorOperationException(
                    "Provider organization must be ACTIVE "
                            + "to manage Doctors"
            );
        }
    }

    private void validateDoctorIsActiveForUpdate(
            Doctor doctor
    ) {

        if (doctor.getStatus()
                != DoctorStatus.ACTIVE) {

            throw new InvalidDoctorOperationException(
                    "An INACTIVE Doctor cannot be updated"
            );
        }
    }

    private void validateDoctorId(
            UUID doctorId
    ) {

        if (doctorId == null) {
            throw new InvalidDoctorOperationException(
                    "Doctor ID is required"
            );
        }
    }

    private DoctorResponse mapToResponse(
            Doctor doctor
    ) {

        Department department =
                doctor.getDepartment();

        ProviderOrganization providerOrganization =
                doctor.getProviderOrganization();

        return DoctorResponse.builder()
                .id(doctor.getId())
                .firstName(doctor.getFirstName())
                .lastName(doctor.getLastName())
                .fullName(
                        createFullName(
                                doctor.getFirstName(),
                                doctor.getLastName()
                        )
                )
                .medicalRegistrationNumber(
                        doctor.getMedicalRegistrationNumber()
                )
                .qualification(
                        doctor.getQualification()
                )
                .specialization(
                        doctor.getSpecialization()
                )
                .yearsOfExperience(
                        doctor.getYearsOfExperience()
                )
                .consultationFee(
                        doctor.getConsultationFee()
                )
                .biography(
                        doctor.getBiography()
                )
                .status(
                        doctor.getStatus()
                )
                .departmentId(
                        department.getId()
                )
                .departmentName(
                        department.getDepartmentName()
                )
                .departmentCode(
                        department.getDepartmentCode()
                )
                .providerId(
                        providerOrganization.getId()
                )
                .providerOrganizationName(
                        providerOrganization.getOrganizationName()
                )
                .createdAt(
                        doctor.getCreatedAt()
                )
                .updatedAt(
                        doctor.getUpdatedAt()
                )
                .build();
    }

    private String createFullName(
            String firstName,
            String lastName
    ) {

        return "Dr. "
                + firstName.trim()
                + " "
                + lastName.trim();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidDoctorOperationException(
                    "Authenticated Provider email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeBusinessIdentifier(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidDoctorOperationException(
                    "Medical registration number is required"
            );
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidDoctorOperationException(
                    "Required Doctor information cannot be empty"
            );
        }

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