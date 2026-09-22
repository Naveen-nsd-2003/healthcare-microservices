package com.naveen.healthcare.providerservice.entity;

import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "doctors",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_doctor_medical_registration_number",
                        columnNames = "medical_registration_number"
                )
        },
        indexes = {
                @Index(
                        name = "idx_doctor_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_doctor_department_id",
                        columnList = "department_id"
                ),
                @Index(
                        name = "idx_doctor_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_doctor_specialization",
                        columnList = "specialization"
                ),
                @Index(
                        name = "idx_doctor_name",
                        columnList = "first_name,last_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "first_name",
            nullable = false,
            length = 100
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 100
    )
    private String lastName;

    @Column(
            name = "medical_registration_number",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String medicalRegistrationNumber;

    @Column(
            name = "qualification",
            nullable = false,
            length = 200
    )
    private String qualification;

    @Column(
            name = "specialization",
            nullable = false,
            length = 150
    )
    private String specialization;

    @Column(
            name = "years_of_experience",
            nullable = false
    )
    private Integer yearsOfExperience;

    @Column(
            name = "consultation_fee",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal consultationFee;

    @Column(
            name = "biography",
            length = 1500
    )
    private String biography;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "provider_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_doctor_provider"
            )
    )
    private ProviderOrganization providerOrganization;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "department_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_doctor_department"
            )
    )
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private DoctorStatus status = DoctorStatus.ACTIVE;

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