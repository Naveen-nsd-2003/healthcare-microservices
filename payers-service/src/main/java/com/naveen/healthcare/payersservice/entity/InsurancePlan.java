package com.naveen.healthcare.payersservice.entity;

import com.naveen.healthcare.payersservice.enums.InsurancePlanStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "insurance_plans",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_insurance_plan_code",
                        columnNames = "plan_code"
                )
        },
        indexes = {
                @Index(
                        name = "idx_insurance_plan_payer_id",
                        columnList = "payer_id"
                ),
                @Index(
                        name = "idx_insurance_plan_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_insurance_plan_name",
                        columnList = "plan_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsurancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "plan_name",
            nullable = false,
            length = 150
    )
    private String planName;

    @Column(
            name = "plan_code",
            nullable = false,
            length = 50
    )
    private String planCode;

    @Column(
            name = "description",
            nullable = false,
            length = 1000
    )
    private String description;

    @Column(
            name = "coverage_amount",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal coverageAmount;

    @Column(
            name = "monthly_premium",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal monthlyPremium;

    @Column(
            name = "annual_deductible",
            nullable = false,
            precision = 15,
            scale = 2
    )
    private BigDecimal annualDeductible;

    @Column(
            name = "copay_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal copayPercentage;

    @Column(
            name = "coinsurance_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal coinsurancePercentage;

    @Column(
            name = "minimum_age",
            nullable = false
    )
    private Integer minimumAge;

    @Column(
            name = "maximum_age",
            nullable = false
    )
    private Integer maximumAge;

    @Column(
            name = "policy_duration_months",
            nullable = false
    )
    private Integer policyDurationMonths;

    @Column(
            name = "hospitalization_coverage",
            nullable = false
    )
    private Boolean hospitalizationCoverage;

    @Column(
            name = "outpatient_coverage",
            nullable = false
    )
    private Boolean outpatientCoverage;

    @Column(
            name = "prescription_coverage",
            nullable = false
    )
    private Boolean prescriptionCoverage;

    @Column(
            name = "maternity_coverage",
            nullable = false
    )
    private Boolean maternityCoverage;

    @Column(
            name = "waiting_period_days",
            nullable = false
    )
    private Integer waitingPeriodDays;

    /*
     * This relationship is allowed because InsurancePlan and
     * PayerOrganization belong to the same Payers Service database.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "payer_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_insurance_plan_payer"
            )
    )
    private PayerOrganization payerOrganization;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private InsurancePlanStatus status =
            InsurancePlanStatus.DRAFT;

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