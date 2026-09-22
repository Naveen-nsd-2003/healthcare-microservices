package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.request.CreateDepartmentRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDepartmentRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDepartmentStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.DepartmentResponse;
import com.naveen.healthcare.providerservice.entity.Department;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.DepartmentNotFoundException;
import com.naveen.healthcare.providerservice.exception.DuplicateDepartmentException;
import com.naveen.healthcare.providerservice.exception.InvalidDepartmentOperationException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.DepartmentRepository;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(
            String authenticatedProviderEmail,
            CreateDepartmentRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        String normalizedDepartmentName =
                normalizeRequiredText(
                        request.getDepartmentName()
                );

        String normalizedDepartmentCode =
                normalizeDepartmentCode(
                        request.getDepartmentCode()
                );

        validateUniqueDepartmentForCreation(
                providerOrganization.getId(),
                normalizedDepartmentName,
                normalizedDepartmentCode
        );

        Department department =
                Department.builder()
                        .departmentName(
                                normalizedDepartmentName
                        )
                        .departmentCode(
                                normalizedDepartmentCode
                        )
                        .description(
                                normalizeRequiredText(
                                        request.getDescription()
                                )
                        )
                        .contactNumber(
                                normalizeOptionalText(
                                        request.getContactNumber()
                                )
                        )
                        .location(
                                normalizeOptionalText(
                                        request.getLocation()
                                )
                        )
                        .providerOrganization(
                                providerOrganization
                        )
                        .status(
                                DepartmentStatus.ACTIVE
                        )
                        .build();

        Department savedDepartment =
                departmentRepository.save(department);

        return mapToResponse(savedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getMyDepartments(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        return departmentRepository
                .findAllByProviderOrganizationAdminEmailIgnoreCaseOrderByDepartmentNameAsc(
                        providerOrganization.getAdminEmail()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getMyDepartmentById(
            String authenticatedProviderEmail,
            UUID departmentId
    ) {

        Department department =
                findDepartmentOwnedByProvider(
                        authenticatedProviderEmail,
                        departmentId
                );

        return mapToResponse(department);
    }

    @Override
    @Transactional
    public DepartmentResponse updateMyDepartment(
            String authenticatedProviderEmail,
            UUID departmentId,
            UpdateDepartmentRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        Department department =
                findDepartmentOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        departmentId
                );

        validateDepartmentIsActiveForUpdate(department);

        String normalizedDepartmentName =
                normalizeRequiredText(
                        request.getDepartmentName()
                );

        validateUniqueDepartmentNameForUpdate(
                providerOrganization.getId(),
                departmentId,
                normalizedDepartmentName
        );

        department.setDepartmentName(
                normalizedDepartmentName
        );

        department.setDescription(
                normalizeRequiredText(
                        request.getDescription()
                )
        );

        department.setContactNumber(
                normalizeOptionalText(
                        request.getContactNumber()
                )
        );

        department.setLocation(
                normalizeOptionalText(
                        request.getLocation()
                )
        );

        Department updatedDepartment =
                departmentRepository.save(department);

        return mapToResponse(updatedDepartment);
    }

    @Override
    @Transactional
    public DepartmentResponse updateMyDepartmentStatus(
            String authenticatedProviderEmail,
            UUID departmentId,
            UpdateDepartmentStatusRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        Department department =
                findDepartmentOwnedByProvider(
                        providerOrganization.getAdminEmail(),
                        departmentId
                );

        DepartmentStatus requestedStatus =
                request.getStatus();

        if (requestedStatus == null) {
            throw new InvalidDepartmentOperationException(
                    "Department status is required"
            );
        }

        if (department.getStatus() == requestedStatus) {
            throw new InvalidDepartmentOperationException(
                    "Department is already in "
                            + requestedStatus
                            + " status"
            );
        }

        department.setStatus(requestedStatus);

        Department updatedDepartment =
                departmentRepository.save(department);

        return mapToResponse(updatedDepartment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAvailableDepartments(
            UUID providerId
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderById(providerId);

        return departmentRepository
                .findAllByProviderOrganizationIdAndStatusOrderByDepartmentNameAsc(
                        providerOrganization.getId(),
                        DepartmentStatus.ACTIVE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getAvailableDepartmentById(
            UUID departmentId
    ) {

        if (departmentId == null) {
            throw new InvalidDepartmentOperationException(
                    "Department ID is required"
            );
        }

        Department department =
                departmentRepository
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

        return mapToResponse(department);
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
            throw new InvalidDepartmentOperationException(
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

    private Department findDepartmentOwnedByProvider(
            String authenticatedProviderEmail,
            UUID departmentId
    ) {

        if (departmentId == null) {
            throw new InvalidDepartmentOperationException(
                    "Department ID is required"
            );
        }

        String normalizedProviderEmail =
                normalizeEmail(
                        authenticatedProviderEmail
                );

        return departmentRepository
                .findByIdAndProviderOrganizationAdminEmailIgnoreCase(
                        departmentId,
                        normalizedProviderEmail
                )
                .orElseThrow(() ->
                        new DepartmentNotFoundException(
                                "Department not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private void validateProviderIsActive(
            ProviderOrganization providerOrganization
    ) {

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new InvalidDepartmentOperationException(
                    "Provider organization must be ACTIVE "
                            + "to manage departments"
            );
        }
    }

    private void validateUniqueDepartmentForCreation(
            UUID providerId,
            String departmentName,
            String departmentCode
    ) {

        if (departmentRepository
                .existsByProviderOrganizationIdAndDepartmentCodeIgnoreCase(
                        providerId,
                        departmentCode
                )) {

            throw new DuplicateDepartmentException(
                    "A department already exists with code: "
                            + departmentCode
            );
        }

        if (departmentRepository
                .existsByProviderOrganizationIdAndDepartmentNameIgnoreCase(
                        providerId,
                        departmentName
                )) {

            throw new DuplicateDepartmentException(
                    "A department already exists with name: "
                            + departmentName
            );
        }
    }

    private void validateUniqueDepartmentNameForUpdate(
            UUID providerId,
            UUID departmentId,
            String departmentName
    ) {

        boolean duplicateNameExists =
                departmentRepository
                        .existsByProviderOrganizationIdAndDepartmentNameIgnoreCaseAndIdNot(
                                providerId,
                                departmentName,
                                departmentId
                        );

        if (duplicateNameExists) {
            throw new DuplicateDepartmentException(
                    "Another department already exists with name: "
                            + departmentName
            );
        }
    }

    private void validateDepartmentIsActiveForUpdate(
            Department department
    ) {

        if (department.getStatus()
                != DepartmentStatus.ACTIVE) {

            throw new InvalidDepartmentOperationException(
                    "An INACTIVE department cannot be updated"
            );
        }
    }

    private DepartmentResponse mapToResponse(
            Department department
    ) {

        ProviderOrganization providerOrganization =
                department.getProviderOrganization();

        return DepartmentResponse.builder()
                .id(
                        department.getId()
                )
                .departmentName(
                        department.getDepartmentName()
                )
                .departmentCode(
                        department.getDepartmentCode()
                )
                .description(
                        department.getDescription()
                )
                .contactNumber(
                        department.getContactNumber()
                )
                .location(
                        department.getLocation()
                )
                .status(
                        department.getStatus()
                )
                .providerId(
                        providerOrganization.getId()
                )
                .providerOrganizationName(
                        providerOrganization.getOrganizationName()
                )
                .createdAt(
                        department.getCreatedAt()
                )
                .updatedAt(
                        department.getUpdatedAt()
                )
                .build();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidDepartmentOperationException(
                    "Authenticated Provider email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeDepartmentCode(
            String departmentCode
    ) {

        if (departmentCode == null
                || departmentCode.isBlank()) {

            throw new InvalidDepartmentOperationException(
                    "Department code is required"
            );
        }

        return departmentCode
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidDepartmentOperationException(
                    "Required department information cannot be empty"
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