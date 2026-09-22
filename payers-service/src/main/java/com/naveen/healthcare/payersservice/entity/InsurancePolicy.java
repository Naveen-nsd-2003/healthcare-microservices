package com.naveen.healthcare.payersservice.entity;

import com.naveen.healthcare.payersservice.enums.PolicyStatus;
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
import jakarta.persistence.OneToOne;
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
        name = "insurance_policies",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_insurance_policy_number",
                        columnNames = "policy_number"
                ),
                @UniqueConstraint(
                        name = "uk_insurance_policy_enrollment",
                        columnNames = "enrollment_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_policy_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_policy_member_email",
                        columnList = "member_email"
                ),
                @Index(
                        name = "idx_policy_plan_id",
                        columnList = "plan_id"
                ),
                @Index(
                        name = "idx_policy_payer_id",
                        columnList = "payer_id"
                ),
                @Index(
                        name = "idx_policy_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_policy_coverage_dates",
                        columnList = "coverage_start_date,coverage_end_date"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "policy_number",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String policyNumber;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "enrollment_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_policy_enrollment"
            )
    )
    private InsuranceEnrollment enrollment;

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
                    name = "fk_policy_insurance_plan"
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
                    name = "fk_policy_payer_organization"
            )
    )
    private PayerOrganization payerOrganization;

    @Column(
            name = "plan_name",
            nullable = false,
            updatable = false,
            length = 150
    )
    private String planName;

    @Column(
            name = "plan_code",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String planCode;

    @Column(
            name = "coverage_amount",
            nullable = false,
            updatable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal coverageAmount;

    @Column(
            name = "monthly_premium",
            nullable = false,
            updatable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal monthlyPremium;

    @Column(
            name = "annual_deductible",
            nullable = false,
            updatable = false,
            precision = 15,
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
            name = "policy_duration_months",
            nullable = false,
            updatable = false
    )
    private Integer policyDurationMonths;

    @Column(
            name = "waiting_period_days",
            nullable = false,
            updatable = false
    )
    private Integer waitingPeriodDays;

    @Column(
            name = "hospitalization_coverage",
            nullable = false,
            updatable = false
    )
    private Boolean hospitalizationCoverage;

    @Column(
            name = "outpatient_coverage",
            nullable = false,
            updatable = false
    )
    private Boolean outpatientCoverage;

    @Column(
            name = "prescription_coverage",
            nullable = false,
            updatable = false
    )
    private Boolean prescriptionCoverage;

    @Column(
            name = "maternity_coverage",
            nullable = false,
            updatable = false
    )
    private Boolean maternityCoverage;

    @Column(
            name = "coverage_start_date",
            nullable = false,
            updatable = false
    )
    private LocalDate coverageStartDate;

    @Column(
            name = "coverage_end_date",
            nullable = false,
            updatable = false
    )
    private LocalDate coverageEndDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private PolicyStatus status = PolicyStatus.ACTIVE;

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