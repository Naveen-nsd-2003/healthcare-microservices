package com.naveen.healthcare.memberservice.entity;

import com.naveen.healthcare.memberservice.enums.MemberStatus;
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
import java.util.UUID;

@Entity
@Table(
        name = "member_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_profile_email",
                        columnNames = "email"
                )
        },
        indexes = {
                @Index(
                        name = "idx_member_profile_email",
                        columnList = "email"
                ),
                @Index(
                        name = "idx_member_profile_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_member_profile_name",
                        columnList = "first_name,last_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "email",
            nullable = false,
            length = 150,
            updatable = false
    )
    private String email;

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
            name = "date_of_birth",
            nullable = false
    )
    private LocalDate dateOfBirth;

    @Column(
            name = "phone_number",
            nullable = false,
            length = 20
    )
    private String phoneNumber;

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

    @Column(
            name = "emergency_contact_name",
            nullable = false,
            length = 150
    )
    private String emergencyContactName;

    @Column(
            name = "emergency_contact_number",
            nullable = false,
            length = 20
    )
    private String emergencyContactNumber;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

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

        LocalDateTime currentTime = LocalDateTime.now();

        createdAt = currentTime;
        updatedAt = currentTime;

        if (status == null) {
            status = MemberStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}