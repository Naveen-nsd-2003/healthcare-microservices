package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.dto.response.ClaimMedicalRecordVerificationResponse;
import com.naveen.healthcare.providerservice.entity.MedicalRecord;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.MedicalRecordStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.InvalidMedicalRecordOperationException;
import com.naveen.healthcare.providerservice.exception.MedicalRecordNotFoundException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.MedicalRecordRepository;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.MedicalRecordClaimVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordClaimVerificationServiceImpl
        implements MedicalRecordClaimVerificationService {

    private final MedicalRecordRepository medicalRecordRepository;

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    @Override
    @Transactional(readOnly = true)
    public ClaimMedicalRecordVerificationResponse
    verifyMedicalRecordForClaim(
            String authenticatedProviderEmail,
            UUID medicalRecordId,
            UUID appointmentId
    ) {

        validateRequest(
                medicalRecordId,
                appointmentId
        );

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        MedicalRecord medicalRecord =
                medicalRecordRepository
                        .findByIdAndProviderId(
                                medicalRecordId,
                                providerOrganization.getId()
                        )
                        .orElseThrow(() ->
                                new MedicalRecordNotFoundException(
                                        "Medical Record not found for "
                                                + "the authenticated Provider"
                                )
                        );

        validateMedicalRecordForClaim(
                medicalRecord,
                appointmentId,
                providerOrganization
        );

        return mapToResponse(medicalRecord);
    }

    private ProviderOrganization findActiveProviderByEmail(
            String authenticatedProviderEmail
    ) {

        String normalizedProviderEmail =
                normalizeEmail(authenticatedProviderEmail);

        ProviderOrganization providerOrganization =
                providerOrganizationRepository
                        .findByAdminEmailIgnoreCase(
                                normalizedProviderEmail
                        )
                        .orElseThrow(() ->
                                new ProviderNotFoundException(
                                        "Provider organization not found "
                                                + "for authenticated email: "
                                                + normalizedProviderEmail
                                )
                        );

        if (providerOrganization.getStatus()
                != ProviderStatus.ACTIVE) {

            throw new InvalidMedicalRecordOperationException(
                    "Provider organization must be ACTIVE "
                            + "to submit a claim"
            );
        }

        return providerOrganization;
    }

    private void validateMedicalRecordForClaim(
            MedicalRecord medicalRecord,
            UUID requestedAppointmentId,
            ProviderOrganization providerOrganization
    ) {

        if (medicalRecord.getAppointmentId() == null
                || !requestedAppointmentId.equals(
                medicalRecord.getAppointmentId()
        )) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not belong "
                            + "to the supplied appointment"
            );
        }

        if (medicalRecord.getProviderId() == null
                || !providerOrganization.getId().equals(
                medicalRecord.getProviderId()
        )) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not belong "
                            + "to the authenticated Provider"
            );
        }

        MedicalRecordStatus status =
                medicalRecord.getStatus();

        if (status == null) {
            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a valid status"
            );
        }

        if (status == MedicalRecordStatus.VOIDED) {
            throw new InvalidMedicalRecordOperationException(
                    "A VOIDED Medical Record cannot be used "
                            + "for claim submission"
            );
        }

        if (status != MedicalRecordStatus.ACTIVE
                && status != MedicalRecordStatus.AMENDED) {

            throw new InvalidMedicalRecordOperationException(
                    "Only an ACTIVE or AMENDED Medical Record "
                            + "can be used for claim submission"
            );
        }

        validateMemberInformation(medicalRecord);
        validateProviderInformation(medicalRecord);
        validateClinicalInformation(medicalRecord);
    }

    private void validateMemberInformation(
            MedicalRecord medicalRecord
    ) {

        if (medicalRecord.getMemberId() == null) {
            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a Member ID"
            );
        }

        if (medicalRecord.getMemberEmail() == null
                || medicalRecord.getMemberEmail().isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a Member email"
            );
        }
    }

    private void validateProviderInformation(
            MedicalRecord medicalRecord
    ) {

        if (medicalRecord.getProviderId() == null) {
            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a Provider ID"
            );
        }

        if (medicalRecord.getProviderOrganizationName() == null
                || medicalRecord
                .getProviderOrganizationName()
                .isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a Provider organization name"
            );
        }

        if (medicalRecord.getDepartmentId() == null
                || medicalRecord.getDepartmentName() == null
                || medicalRecord.getDepartmentName().isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "complete Department information"
            );
        }

        if (medicalRecord.getDoctorId() == null
                || medicalRecord.getDoctorName() == null
                || medicalRecord.getDoctorName().isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "complete Doctor information"
            );
        }

        if (medicalRecord.getDoctorSpecialization() == null
                || medicalRecord
                .getDoctorSpecialization()
                .isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "the Doctor specialization"
            );
        }

        if (medicalRecord.getAppointmentDate() == null) {
            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "an appointment date"
            );
        }
    }

    private void validateClinicalInformation(
            MedicalRecord medicalRecord
    ) {

        if (medicalRecord.getDiagnosis() == null
                || medicalRecord.getDiagnosis().isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a diagnosis"
            );
        }

        if (medicalRecord.getTreatmentPlan() == null
                || medicalRecord.getTreatmentPlan().isBlank()) {

            throw new InvalidMedicalRecordOperationException(
                    "The Medical Record does not contain "
                            + "a treatment plan"
            );
        }
    }

    private ClaimMedicalRecordVerificationResponse mapToResponse(
            MedicalRecord medicalRecord
    ) {

        return ClaimMedicalRecordVerificationResponse.builder()
                .medicalRecordId(
                        medicalRecord.getId()
                )
                .appointmentId(
                        medicalRecord.getAppointmentId()
                )
                .memberId(
                        medicalRecord.getMemberId()
                )
                .memberEmail(
                        medicalRecord.getMemberEmail()
                )
                .providerId(
                        medicalRecord.getProviderId()
                )
                .providerOrganizationName(
                        medicalRecord
                                .getProviderOrganizationName()
                )
                .departmentId(
                        medicalRecord.getDepartmentId()
                )
                .departmentName(
                        medicalRecord.getDepartmentName()
                )
                .doctorId(
                        medicalRecord.getDoctorId()
                )
                .doctorName(
                        medicalRecord.getDoctorName()
                )
                .doctorSpecialization(
                        medicalRecord.getDoctorSpecialization()
                )
                .appointmentDate(
                        medicalRecord.getAppointmentDate()
                )
                .diagnosis(
                        medicalRecord.getDiagnosis()
                )
                .treatmentPlan(
                        medicalRecord.getTreatmentPlan()
                )
                .status(
                        medicalRecord.getStatus()
                )
                .build();
    }

    private void validateRequest(
            UUID medicalRecordId,
            UUID appointmentId
    ) {

        if (medicalRecordId == null) {
            throw new InvalidMedicalRecordOperationException(
                    "Medical Record ID is required"
            );
        }

        if (appointmentId == null) {
            throw new InvalidMedicalRecordOperationException(
                    "Appointment ID is required"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidMedicalRecordOperationException(
                    "Authenticated Provider email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}