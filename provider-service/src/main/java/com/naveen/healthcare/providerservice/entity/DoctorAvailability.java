package com.naveen.healthcare.providerservice.entity;

import com.naveen.healthcare.providerservice.enums.AvailabilityStatus;
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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "doctor_availabilities",
        indexes = {
                @Index(
                        name = "idx_availability_doctor_id",
                        columnList = "doctor_id"
                ),
                @Index(
                        name = "idx_availability_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_availability_day_of_week",
                        columnList = "day_of_week"
                ),
                @Index(
                        name = "idx_availability_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_availability_doctor_day",
                        columnList = "doctor_id,day_of_week"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "doctor_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_availability_doctor"
            )
    )
    private Doctor doctor;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "provider_id",
            nullable = false,
            updatable = false,
            foreignKey = @ForeignKey(
                    name = "fk_availability_provider"
            )
    )
    private ProviderOrganization providerOrganization;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "day_of_week",
            nullable = false,
            length = 20
    )
    private DayOfWeek dayOfWeek;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;

    @Column(
            name = "slot_duration_minutes",
            nullable = false
    )
    private Integer slotDurationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private AvailabilityStatus status =
            AvailabilityStatus.ACTIVE;

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