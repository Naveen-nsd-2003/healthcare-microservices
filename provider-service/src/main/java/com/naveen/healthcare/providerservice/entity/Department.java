package com.naveen.healthcare.providerservice.entity;

import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
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

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_department_provider_code",
                        columnNames = {
                                "provider_id",
                                "department_code"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_department_provider_name",
                        columnNames = {
                                "provider_id",
                                "department_name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_department_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_department_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_department_name",
                        columnList = "department_name"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "department_name",
            nullable = false,
            length = 150
    )
    private String departmentName;

    @Column(
            name = "department_code",
            nullable = false,
            updatable = false,
            length = 50
    )
    private String departmentCode;

    @Column(
            name = "description",
            nullable = false,
            length = 1000
    )
    private String description;

    @Column(
            name = "contact_number",
            length = 20
    )
    private String contactNumber;

    @Column(
            name = "location",
            length = 150
    )
    private String location;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "provider_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_department_provider"
            )
    )
    private ProviderOrganization providerOrganization;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private DepartmentStatus status =
            DepartmentStatus.ACTIVE;

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