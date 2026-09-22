package com.naveen.healthcare.providerservice.entity;

import com.naveen.healthcare.providerservice.enums.MedicalRecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "medical_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_medical_record_appointment",
                        columnNames = "appointment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_medical_record_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_medical_record_member_email",
                        columnList = "member_email"
                ),
                @Index(
                        name = "idx_medical_record_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_medical_record_department_id",
                        columnList = "department_id"
                ),
                @Index(
                        name = "idx_medical_record_doctor_id",
                        columnList = "doctor_id"
                ),
                @Index(
                        name = "idx_medical_record_appointment_date",
                        columnList = "appointment_date"
                ),
                @Index(
                        name = "idx_medical_record_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_medical_record_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "appointment_id",
            nullable = false,
            updatable = false
    )
    private UUID appointmentId;

    @Column(
            name = "member_id",
            nullable = false,
            updatable = false
    )
    private UUID memberId;

    @Column(
            name = "member_email",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String memberEmail;

    @Column(
            name = "provider_id",
            nullable = false,
            updatable = false
    )
    private UUID providerId;

    @Column(
            name = "provider_organization_name",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String providerOrganizationName;

    @Column(
            name = "department_id",
            nullable = false,
            updatable = false
    )
    private UUID departmentId;

    @Column(
            name = "department_name",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String departmentName;

    @Column(
            name = "doctor_id",
            nullable = false,
            updatable = false
    )
    private UUID doctorId;

    @Column(
            name = "doctor_name",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String doctorName;

    @Column(
            name = "doctor_specialization",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String doctorSpecialization;

    @Column(
            name = "appointment_date",
            nullable = false,
            updatable = false
    )
    private LocalDate appointmentDate;

    @Column(
            name = "appointment_start_time",
            nullable = false,
            updatable = false
    )
    private LocalTime appointmentStartTime;

    @Column(
            name = "appointment_end_time",
            nullable = false,
            updatable = false
    )
    private LocalTime appointmentEndTime;

    @Column(
            name = "chief_complaint",
            nullable = false,
            length = 1000
    )
    private String chiefComplaint;

    @Column(
            name = "diagnosis",
            nullable = false,
            length = 2000
    )
    private String diagnosis;

    @Column(
            name = "clinical_observations",
            length = 3000
    )
    private String clinicalObservations;

    @Column(
            name = "treatment_plan",
            nullable = false,
            length = 3000
    )
    private String treatmentPlan;

    @Column(
            name = "prescription_notes",
            length = 3000
    )
    private String prescriptionNotes;

    @Column(
            name = "follow_up_date"
    )
    private LocalDate followUpDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private MedicalRecordStatus status =
            MedicalRecordStatus.ACTIVE;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}