package com.naveen.healthcare.payersservice.entity;

import com.naveen.healthcare.payersservice.enums.PayerStatus;
import jakarta.persistence.*;
import lombok.*;

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
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayerOrganization {


    // =====================================================
    // PRIMARY KEY
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // =====================================================
    // ORGANIZATION INFORMATION
    // =====================================================

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


    // =====================================================
    // AUTH SERVICE BUSINESS IDENTIFIER
    // =====================================================

    /*
     * This email belongs to the PAYER authentication
     * account created in Auth Service.
     *
     * It is not a database foreign key.
     */
    @Column(
            name = "admin_email",
            nullable = false,
            length = 150
    )
    private String adminEmail;


    // =====================================================
    // CONTACT INFORMATION
    // =====================================================

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


    // =====================================================
    // ADDRESS
    // =====================================================

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


    // =====================================================
    // STATUS
    // =====================================================

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private PayerStatus status = PayerStatus.ACTIVE;


    // =====================================================
    // AUDIT FIELDS
    // =====================================================

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


    // =====================================================
    // ENTITY LIFECYCLE
    // =====================================================

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