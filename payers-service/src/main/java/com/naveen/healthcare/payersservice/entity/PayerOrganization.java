package com.naveen.healthcare.payersservice.entity;

import com.naveen.healthcare.payersservice.enums.PayerStatus;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payer_organizations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_payer_registration_number",
                        columnNames = "registration_number"
                ),
                @UniqueConstraint(
                        name = "uk_payer_admin_email",
                        columnNames = "admin_email"
                )
        },
        indexes = {
                @Index(
                        name = "idx_payer_organization_name",
                        columnList = "organization_name"
                ),
                @Index(
                        name = "idx_payer_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_payer_submitted_at",
                        columnList = "submitted_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayerOrganization {

    @Id
    @GeneratedValue(
            strategy = GenerationType.UUID
    )
    private UUID id;

    @Column(
            name = "organization_name",
            nullable = false,
            length = 150
    )
    private String organizationName;

    @Column(
            name = "registration_number",
            nullable = false,
            length = 100
    )
    private String registrationNumber;

    @Column(
            name = "admin_email",
            nullable = false,
            length = 150
    )
    private String adminEmail;

    @Column(
            name = "contact_number",
            nullable = false,
            length = 20
    )
    private String contactNumber;

    @Column(
            name = "website",
            length = 255
    )
    private String website;

    @Column(
            name = "address_line",
            nullable = false,
            length = 255
    )
    private String addressLine;

    @Column(
            name = "city",
            nullable = false,
            length = 100
    )
    private String city;

    @Column(
            name = "state",
            nullable = false,
            length = 100
    )
    private String state;

    @Column(
            name = "postal_code",
            nullable = false,
            length = 20
    )
    private String postalCode;

    @Column(
            name = "country",
            nullable = false,
            length = 100
    )
    private String country;

    @Enumerated(
            EnumType.STRING
    )
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private PayerStatus status =
            PayerStatus.ACTIVE;

    @Column(
            name = "submitted_at"
    )
    private LocalDateTime submittedAt;

    @Column(
            name = "reviewed_at"
    )
    private LocalDateTime reviewedAt;

    @Column(
            name = "reviewed_by_email",
            length = 150
    )
    private String reviewedByEmail;

    @Column(
            name = "review_remarks",
            length = 1000
    )
    private String reviewRemarks;

    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;

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

        LocalDateTime now =
                LocalDateTime.now();

        if (status == null) {
            status = PayerStatus.ACTIVE;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt =
                LocalDateTime.now();
    }
}