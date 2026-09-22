package com.naveen.healthcare.providerservice.serviceimpl;

import com.naveen.healthcare.providerservice.client.AppointmentServiceClient;
import com.naveen.healthcare.providerservice.dto.request.CreateMedicalRecordRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateMedicalRecordRequest;
import com.naveen.healthcare.providerservice.dto.response.MedicalRecordAppointmentVerificationResponse;
import com.naveen.healthcare.providerservice.dto.response.MedicalRecordResponse;
import com.naveen.healthcare.providerservice.entity.MedicalRecord;
import com.naveen.healthcare.providerservice.entity.ProviderOrganization;
import com.naveen.healthcare.providerservice.enums.MedicalRecordStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.exception.AppointmentServiceCommunicationException;
import com.naveen.healthcare.providerservice.exception.DuplicateMedicalRecordException;
import com.naveen.healthcare.providerservice.exception.InvalidMedicalRecordOperationException;
import com.naveen.healthcare.providerservice.exception.MedicalRecordNotFoundException;
import com.naveen.healthcare.providerservice.exception.ProviderNotFoundException;
import com.naveen.healthcare.providerservice.repository.MedicalRecordRepository;
import com.naveen.healthcare.providerservice.repository.ProviderOrganizationRepository;
import com.naveen.healthcare.providerservice.service.MedicalRecordService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl
        implements MedicalRecordService {

    private static final String COMPLETED_STATUS = "COMPLETED";

    private final MedicalRecordRepository medicalRecordRepository;

    private final ProviderOrganizationRepository
            providerOrganizationRepository;

    private final AppointmentServiceClient appointmentServiceClient;

    @Override
    @Transactional
    public MedicalRecordResponse createMedicalRecord(
            String authenticatedProviderEmail,
            CreateMedicalRecordRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        UUID appointmentId =
                request.getAppointmentId();

        validateAppointmentId(appointmentId);

        if (medicalRecordRepository
                .existsByAppointmentId(appointmentId)) {

            throw new DuplicateMedicalRecordException(
                    "A Medical Record already exists for "
                            + "the selected appointment"
            );
        }

        MedicalRecordAppointmentVerificationResponse
                appointmentVerification =
                retrieveCompletedAppointmentVerification(
                        appointmentId
                );

        validateAppointmentVerification(
                appointmentVerification,
                providerOrganization,
                appointmentId
        );

        validateFollowUpDate(
                request.getFollowUpDate(),
                appointmentVerification.getAppointmentDate()
        );

        MedicalRecord medicalRecord =
                MedicalRecord.builder()
                        .appointmentId(
                                appointmentVerification
                                        .getAppointmentId()
                        )
                        .memberId(
                                appointmentVerification.getMemberId()
                        )
                        .memberEmail(
                                normalizeEmail(
                                        appointmentVerification
                                                .getMemberEmail()
                                )
                        )
                        .providerId(
                                appointmentVerification.getProviderId()
                        )
                        .providerOrganizationName(
                                normalizeRequiredText(
                                        appointmentVerification
                                                .getProviderOrganizationName()
                                )
                        )
                        .departmentId(
                                appointmentVerification
                                        .getDepartmentId()
                        )
                        .departmentName(
                                normalizeRequiredText(
                                        appointmentVerification
                                                .getDepartmentName()
                                )
                        )
                        .doctorId(
                                appointmentVerification.getDoctorId()
                        )
                        .doctorName(
                                normalizeRequiredText(
                                        appointmentVerification
                                                .getDoctorName()
                                )
                        )
                        .doctorSpecialization(
                                normalizeRequiredText(
                                        appointmentVerification
                                                .getDoctorSpecialization()
                                )
                        )
                        .appointmentDate(
                                appointmentVerification
                                        .getAppointmentDate()
                        )
                        .appointmentStartTime(
                                appointmentVerification.getStartTime()
                        )
                        .appointmentEndTime(
                                appointmentVerification.getEndTime()
                        )
                        .chiefComplaint(
                                normalizeRequiredText(
                                        request.getChiefComplaint()
                                )
                        )
                        .diagnosis(
                                normalizeRequiredText(
                                        request.getDiagnosis()
                                )
                        )
                        .clinicalObservations(
                                normalizeOptionalText(
                                        request.getClinicalObservations()
                                )
                        )
                        .treatmentPlan(
                                normalizeRequiredText(
                                        request.getTreatmentPlan()
                                )
                        )
                        .prescriptionNotes(
                                normalizeOptionalText(
                                        request.getPrescriptionNotes()
                                )
                        )
                        .followUpDate(
                                request.getFollowUpDate()
                        )
                        .status(
                                MedicalRecordStatus.ACTIVE
                        )
                        .build();

        MedicalRecord savedMedicalRecord =
                medicalRecordRepository.save(medicalRecord);

        return mapToResponse(savedMedicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getProviderMedicalRecords(
            String authenticatedProviderEmail
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        return medicalRecordRepository
                .findAllByProviderIdOrderByAppointmentDateDescCreatedAtDesc(
                        providerOrganization.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse>
    getProviderMedicalRecordsByDoctor(
            String authenticatedProviderEmail,
            UUID doctorId
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        if (doctorId == null) {
            throw new InvalidMedicalRecordOperationException(
                    "Doctor ID is required"
            );
        }

        return medicalRecordRepository
                .findAllByDoctorIdAndProviderIdOrderByAppointmentDateDescCreatedAtDesc(
                        doctorId,
                        providerOrganization.getId()
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getProviderMedicalRecordById(
            String authenticatedProviderEmail,
            UUID medicalRecordId
    ) {

        ProviderOrganization providerOrganization =
                findProviderByEmail(
                        authenticatedProviderEmail
                );

        MedicalRecord medicalRecord =
                findMedicalRecordOwnedByProvider(
                        providerOrganization.getId(),
                        medicalRecordId
                );

        return mapToResponse(medicalRecord);
    }

    @Override
    @Transactional
    public MedicalRecordResponse updateProviderMedicalRecord(
            String authenticatedProviderEmail,
            UUID medicalRecordId,
            UpdateMedicalRecordRequest request
    ) {

        ProviderOrganization providerOrganization =
                findActiveProviderByEmail(
                        authenticatedProviderEmail
                );

        MedicalRecord medicalRecord =
                findMedicalRecordOwnedByProvider(
                        providerOrganization.getId(),
                        medicalRecordId
                );

        validateMedicalRecordCanBeAmended(medicalRecord);

        validateFollowUpDate(
                request.getFollowUpDate(),
                medicalRecord.getAppointmentDate()
        );

        medicalRecord.setChiefComplaint(
                normalizeRequiredText(
                        request.getChiefComplaint()
                )
        );

        medicalRecord.setDiagnosis(
                normalizeRequiredText(
                        request.getDiagnosis()
                )
        );

        medicalRecord.setClinicalObservations(
                normalizeOptionalText(
                        request.getClinicalObservations()
                )
        );

        medicalRecord.setTreatmentPlan(
                normalizeRequiredText(
                        request.getTreatmentPlan()
                )
        );

        medicalRecord.setPrescriptionNotes(
                normalizeOptionalText(
                        request.getPrescriptionNotes()
                )
        );

        medicalRecord.setFollowUpDate(
                request.getFollowUpDate()
        );

        medicalRecord.setStatus(
                MedicalRecordStatus.AMENDED
        );

        MedicalRecord updatedMedicalRecord =
                medicalRecordRepository.save(medicalRecord);

        return mapToResponse(updatedMedicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponse> getMyMedicalRecords(
            String authenticatedMemberEmail
    ) {

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        return medicalRecordRepository
                .findAllByMemberEmailIgnoreCaseAndStatusNotOrderByAppointmentDateDescCreatedAtDesc(
                        normalizedMemberEmail,
                        MedicalRecordStatus.VOIDED
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponse getMyMedicalRecordById(
            String authenticatedMemberEmail,
            UUID medicalRecordId
    ) {

        validateMedicalRecordId(medicalRecordId);

        String normalizedMemberEmail =
                normalizeEmail(authenticatedMemberEmail);

        MedicalRecord medicalRecord =
                medicalRecordRepository
                        .findByIdAndMemberEmailIgnoreCaseAndStatusNot(
                                medicalRecordId,
                                normalizedMemberEmail,
                                MedicalRecordStatus.VOIDED
                        )
                        .orElseThrow(() ->
                                new MedicalRecordNotFoundException(
                                        "Medical Record not found for "
                                                + "the authenticated Member"
                                )
                        );

        return mapToResponse(medicalRecord);
    }

    private MedicalRecordAppointmentVerificationResponse
    retrieveCompletedAppointmentVerification(
            UUID appointmentId
    ) {

        try {

            return appointmentServiceClient
                    .verifyCompletedAppointmentForMedicalRecord(
                            appointmentId
                    );

        } catch (RetryableException exception) {

            throw new AppointmentServiceCommunicationException(
                    "Appointment verification is "
                            + "temporarily unavailable",
                    exception
            );

        } catch (FeignException exception) {

            handleAppointmentServiceFailure(exception);

            throw new AppointmentServiceCommunicationException(
                    "Appointment Service returned "
                            + "an unexpected response",
                    exception
            );
        }
    }

    private void handleAppointmentServiceFailure(
            FeignException exception
    ) {

        int status = exception.status();

        if (status == 400) {
            throw new InvalidMedicalRecordOperationException(
                    "A Medical Record can be created only for "
                            + "a COMPLETED appointment"
            );
        }

        if (status == 404) {
            throw new MedicalRecordNotFoundException(
                    "Completed appointment not found for "
                            + "the authenticated Provider"
            );
        }

        if (status == 401) {
            throw new AppointmentServiceCommunicationException(
                    "Appointment Service rejected the forwarded "
                            + "authentication token",
                    exception
            );
        }

        if (status == 403) {
            throw new AppointmentServiceCommunicationException(
                    "Appointment Service rejected the forwarded "
                            + "Provider authorization",
                    exception
            );
        }

        if (status == 503) {
            throw new AppointmentServiceCommunicationException(
                    "Appointment verification is "
                            + "temporarily unavailable",
                    exception
            );
        }

        throw new AppointmentServiceCommunicationException(
                "Appointment Service returned "
                        + "an unexpected response",
                exception
        );
    }

    private void validateAppointmentVerification(
            MedicalRecordAppointmentVerificationResponse response,
            ProviderOrganization providerOrganization,
            UUID requestedAppointmentId
    ) {

        if (response == null) {
            throw new AppointmentServiceCommunicationException(
                    "Appointment Service returned an empty response"
            );
        }

        if (response.getAppointmentId() == null
                || !requestedAppointmentId.equals(
                response.getAppointmentId()
        )) {

            throw new AppointmentServiceCommunicationException(
                    "Appointment Service returned details for "
                            + "an unexpected appointment"
            );
        }

        if (response.getProviderId() == null
                || !providerOrganization.getId().equals(
                response.getProviderId()
        )) {

            throw new InvalidMedicalRecordOperationException(
                    "The completed appointment does not belong "
                            + "to the authenticated Provider"
            );
        }

        if (!COMPLETED_STATUS.equalsIgnoreCase(
                response.getStatus()
        )) {

            throw new InvalidMedicalRecordOperationException(
                    "A Medical Record can be created only for "
                            + "a COMPLETED appointment"
            );
        }

        if (response.getMemberId() == null
                || response.getMemberEmail() == null
                || response.getMemberEmail().isBlank()) {

            throw new AppointmentServiceCommunicationException(
                    "Appointment Service returned incomplete "
                            + "Member information"
            );
        }

        if (response.getDepartmentId() == null
                || response.getDoctorId() == null) {

            throw new AppointmentServiceCommunicationException(
                    "Appointment Service returned incomplete "
                            + "Provider information"
            );
        }

        if (response.getAppointmentDate() == null
                || response.getStartTime() == null
                || response.getEndTime() == null
                || !response.getStartTime().isBefore(
                response.getEndTime()
        )) {

            throw new AppointmentServiceCommunicationException(
                    "Appointment Service returned invalid "
                            + "appointment timing information"
            );
        }
    }

    private ProviderOrganization findProviderByEmail(
            String authenticatedProviderEmail
    ) {

        String normalizedProviderEmail =
                normalizeEmail(authenticatedProviderEmail);

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

            throw new InvalidMedicalRecordOperationException(
                    "Provider organization must be ACTIVE "
                            + "to manage Medical Records"
            );
        }

        return providerOrganization;
    }

    private MedicalRecord findMedicalRecordOwnedByProvider(
            UUID providerId,
            UUID medicalRecordId
    ) {

        validateMedicalRecordId(medicalRecordId);

        return medicalRecordRepository
                .findByIdAndProviderId(
                        medicalRecordId,
                        providerId
                )
                .orElseThrow(() ->
                        new MedicalRecordNotFoundException(
                                "Medical Record not found for "
                                        + "the authenticated Provider"
                        )
                );
    }

    private void validateMedicalRecordCanBeAmended(
            MedicalRecord medicalRecord
    ) {

        if (medicalRecord.getStatus()
                == MedicalRecordStatus.VOIDED) {

            throw new InvalidMedicalRecordOperationException(
                    "A VOIDED Medical Record cannot be amended"
            );
        }
    }

    private void validateFollowUpDate(
            LocalDate followUpDate,
            LocalDate appointmentDate
    ) {

        if (followUpDate == null) {
            return;
        }

        if (appointmentDate == null) {
            throw new InvalidMedicalRecordOperationException(
                    "Appointment date is required "
                            + "to validate the follow-up date"
            );
        }

        if (followUpDate.isBefore(appointmentDate)) {
            throw new InvalidMedicalRecordOperationException(
                    "Follow-up date cannot be before "
                            + "the appointment date"
            );
        }

        if (followUpDate.isBefore(LocalDate.now())) {
            throw new InvalidMedicalRecordOperationException(
                    "Follow-up date cannot be in the past"
            );
        }
    }

    private MedicalRecordResponse mapToResponse(
            MedicalRecord medicalRecord
    ) {

        return MedicalRecordResponse.builder()
                .id(
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
                .appointmentStartTime(
                        medicalRecord.getAppointmentStartTime()
                )
                .appointmentEndTime(
                        medicalRecord.getAppointmentEndTime()
                )
                .chiefComplaint(
                        medicalRecord.getChiefComplaint()
                )
                .diagnosis(
                        medicalRecord.getDiagnosis()
                )
                .clinicalObservations(
                        medicalRecord.getClinicalObservations()
                )
                .treatmentPlan(
                        medicalRecord.getTreatmentPlan()
                )
                .prescriptionNotes(
                        medicalRecord.getPrescriptionNotes()
                )
                .followUpDate(
                        medicalRecord.getFollowUpDate()
                )
                .status(
                        medicalRecord.getStatus()
                )
                .createdAt(
                        medicalRecord.getCreatedAt()
                )
                .updatedAt(
                        medicalRecord.getUpdatedAt()
                )
                .build();
    }

    private void validateAppointmentId(
            UUID appointmentId
    ) {

        if (appointmentId == null) {
            throw new InvalidMedicalRecordOperationException(
                    "Appointment ID is required"
            );
        }
    }

    private void validateMedicalRecordId(
            UUID medicalRecordId
    ) {

        if (medicalRecordId == null) {
            throw new InvalidMedicalRecordOperationException(
                    "Medical Record ID is required"
            );
        }
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new InvalidMedicalRecordOperationException(
                    "Authenticated email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {
            throw new InvalidMedicalRecordOperationException(
                    "Required Medical Record information "
                            + "cannot be empty"
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