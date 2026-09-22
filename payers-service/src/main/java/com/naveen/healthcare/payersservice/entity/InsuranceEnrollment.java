package com.naveen.healthcare.payersservice.entity;

import com.naveen.healthcare.payersservice.enums.EnrollmentStatus;
import com.naveen.healthcare.payersservice.enums.NomineeRelationship;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "insurance_enrollments",
        indexes = {
                @Index(
                        name = "idx_enrollment_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_enrollment_member_email",
                        columnList = "member_email"
                ),
                @Index(
                        name = "idx_enrollment_plan_id",
                        columnList = "plan_id"
                ),
                @Index(
                        name = "idx_enrollment_payer_id",
                        columnList = "payer_id"
                ),
                @Index(
                        name = "idx_enrollment_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_enrollment_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

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

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "plan_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_enrollment_insurance_plan"
            )
    )
    private InsurancePlan insurancePlan;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "payer_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_enrollment_payer_organization"
            )
    )
    private PayerOrganization payerOrganization;

    @Column(
            name = "requested_coverage_start_date",
            nullable = false,
            updatable = false
    )
    private LocalDate requestedCoverageStartDate;

    @Column(
            name = "nominee_name",
            nullable = false,
            length = 150
    )
    private String nomineeName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "nominee_relationship",
            nullable = false,
            length = 30
    )
    private NomineeRelationship nomineeRelationship;

    @Column(
            name = "nominee_contact_number",
            nullable = false,
            length = 20
    )
    private String nomineeContactNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(
            name = "review_remarks",
            length = 500
    )
    private String reviewRemarks;

    @Column(
            name = "reviewed_at"
    )
    private LocalDateTime reviewedAt;

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