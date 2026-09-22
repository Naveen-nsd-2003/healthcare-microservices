package com.naveen.healthcare.providerservice.repository;

import com.naveen.healthcare.providerservice.entity.MedicalRecord;
import com.naveen.healthcare.providerservice.enums.MedicalRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository
        extends JpaRepository<MedicalRecord, UUID> {

    boolean existsByAppointmentId(
            UUID appointmentId
    );

    Optional<MedicalRecord> findByAppointmentId(
            UUID appointmentId
    );

    List<MedicalRecord>
    findAllByProviderIdOrderByAppointmentDateDescCreatedAtDesc(
            UUID providerId
    );

    Optional<MedicalRecord> findByIdAndProviderId(
            UUID medicalRecordId,
            UUID providerId
    );

    List<MedicalRecord>
    findAllByDoctorIdAndProviderIdOrderByAppointmentDateDescCreatedAtDesc(
            UUID doctorId,
            UUID providerId
    );

    List<MedicalRecord>
    findAllByMemberEmailIgnoreCaseAndStatusNotOrderByAppointmentDateDescCreatedAtDesc(
            String memberEmail,
            MedicalRecordStatus excludedStatus
    );

    Optional<MedicalRecord>
    findByIdAndMemberEmailIgnoreCaseAndStatusNot(
            UUID medicalRecordId,
            String memberEmail,
            MedicalRecordStatus excludedStatus
    );
}