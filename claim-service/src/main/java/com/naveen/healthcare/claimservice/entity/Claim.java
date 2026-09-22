package com.naveen.healthcare.claimservice.entity;

import com.naveen.healthcare.claimservice.enums.ClaimStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "claims",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_claim_number",
                        columnNames = "claim_number"
                ),
                @UniqueConstraint(
                        name = "uk_claim_appointment",
                        columnNames = "appointment_id"
                ),
                @UniqueConstraint(
                        name = "uk_claim_medical_record",
                        columnNames = "medical_record_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_claim_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_claim_member_email",
                        columnList = "member_email"
                ),
                @Index(
                        name = "idx_claim_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_claim_payer_id",
                        columnList = "payer_id"
                ),
                @Index(
                        name = "idx_claim_policy_id",
                        columnList = "policy_id"
                ),
                @Index(
                        name = "idx_claim_doctor_id",
                        columnList = "doctor_id"
                ),
                @Index(
                        name = "idx_claim_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_claim_service_date",
                        columnList = "service_date"
                ),
                @Index(
                        name = "idx_claim_submitted_at",
                        columnList = "submitted_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "claim_number",
            nullable = false,
            updatable = false,
            length = 40
    )
    private String claimNumber;

    @Column(
            name = "appointment_id",
            nullable = false,
            updatable = false
    )
    private UUID appointmentId;

    @Column(
            name = "medical_record_id",
            nullable = false,
            updatable = false
    )
    private UUID medicalRecordId;

    @Column(
            name = "policy_id",
            nullable = false,
            updatable = false
    )
    private UUID policyId;

    @Column(
            name = "policy_number",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String policyNumber;

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
            name = "payer_id",
            nullable = false,
            updatable = false
    )
    private UUID payerId;

    @Column(
            name = "payer_organization_name",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String payerOrganizationName;

    @Column(
            name = "plan_id",
            nullable = false,
            updatable = false
    )
    private UUID planId;

    @Column(
            name = "plan_code",
            nullable = false,
            updatable = false,
            length = 100
    )
    private String planCode;

    @Column(
            name = "plan_name",
            nullable = false,
            updatable = false,
            length = 200
    )
    private String planName;

    @Column(
            name = "service_date",
            nullable = false,
            updatable = false
    )
    private LocalDate serviceDate;

    @Column(
            name = "consultation_fee",
            nullable = false,
            updatable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal consultationFee;

    @Column(
            name = "claimed_amount",
            nullable = false,
            updatable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal claimedAmount;

    @Column(
            name = "approved_amount",
            precision = 12,
            scale = 2
    )
    private BigDecimal approvedAmount;

    @Column(
            name = "coverage_amount",
            nullable = false,
            updatable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal coverageAmount;

    @Column(
            name = "annual_deductible",
            nullable = false,
            updatable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal annualDeductible;

    @Column(
            name = "copay_percentage",
            nullable = false,
            updatable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal copayPercentage;

    @Column(
            name = "coinsurance_percentage",
            nullable = false,
            updatable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal coinsurancePercentage;

    @Column(
            name = "policy_coverage_start_date",
            nullable = false,
            updatable = false
    )
    private LocalDate policyCoverageStartDate;

    @Column(
            name = "policy_coverage_end_date",
            nullable = false,
            updatable = false
    )
    private LocalDate policyCoverageEndDate;

    @Column(
            name = "diagnosis_snapshot",
            nullable = false,
            updatable = false,
            length = 2000
    )
    private String diagnosisSnapshot;

    @Column(
            name = "treatment_plan_snapshot",
            nullable = false,
            updatable = false,
            length = 3000
    )
    private String treatmentPlanSnapshot;

    @Column(
            name = "claim_description",
            nullable = false,
            updatable = false,
            length = 1500
    )
    private String claimDescription;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private ClaimStatus status = ClaimStatus.SUBMITTED;

    @Column(
            name = "review_remarks",
            length = 1500
    )
    private String reviewRemarks;

    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;

    @Column(
            name = "cancellation_reason",
            length = 1000
    )
    private String cancellationReason;

    @Column(
            name = "submitted_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime submittedAt;

    @Column(
            name = "reviewed_at"
    )
    private LocalDateTime reviewedAt;

    @Column(
            name = "resolved_at"
    )
    private LocalDateTime resolvedAt;

    @Column(
            name = "cancelled_at"
    )
    private LocalDateTime cancelledAt;

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

        LocalDateTime currentTimestamp =
                LocalDateTime.now();

        if (status == null) {
            status = ClaimStatus.SUBMITTED;
        }

        if (submittedAt == null) {
            submittedAt = currentTimestamp;
        }

        if (createdAt == null) {
            createdAt = currentTimestamp;
        }

        updatedAt = currentTimestamp;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}