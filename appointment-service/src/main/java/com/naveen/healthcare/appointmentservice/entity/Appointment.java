package com.naveen.healthcare.appointmentservice.entity;

import com.naveen.healthcare.appointmentservice.enums.AppointmentStatus;
import com.naveen.healthcare.appointmentservice.enums.CancelledBy;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(
                        name = "idx_appointment_member_id",
                        columnList = "member_id"
                ),
                @Index(
                        name = "idx_appointment_member_email",
                        columnList = "member_email"
                ),
                @Index(
                        name = "idx_appointment_provider_id",
                        columnList = "provider_id"
                ),
                @Index(
                        name = "idx_appointment_doctor_id",
                        columnList = "doctor_id"
                ),
                @Index(
                        name = "idx_appointment_department_id",
                        columnList = "department_id"
                ),
                @Index(
                        name = "idx_appointment_doctor_date_time",
                        columnList = "doctor_id,appointment_date,start_time"
                ),
                @Index(
                        name = "idx_appointment_member_date_time",
                        columnList = "member_id,appointment_date,start_time"
                ),
                @Index(
                        name = "idx_appointment_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_appointment_created_at",
                        columnList = "created_at"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

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
            name = "consultation_fee",
            nullable = false,
            updatable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal consultationFee;

    @Column(
            name = "appointment_date",
            nullable = false,
            updatable = false
    )
    private LocalDate appointmentDate;

    @Column(
            name = "start_time",
            nullable = false,
            updatable = false
    )
    private LocalTime startTime;

    @Column(
            name = "end_time",
            nullable = false,
            updatable = false
    )
    private LocalTime endTime;

    @Column(
            name = "slot_duration_minutes",
            nullable = false,
            updatable = false
    )
    private Integer slotDurationMinutes;

    @Column(
            name = "reason_for_visit",
            nullable = false,
            updatable = false,
            length = 1000
    )
    private String reasonForVisit;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    @Builder.Default
    private AppointmentStatus status =
            AppointmentStatus.BOOKED;

    @Column(
            name = "provider_notes",
            length = 1000
    )
    private String providerNotes;

    @Column(
            name = "confirmed_at"
    )
    private LocalDateTime confirmedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "cancelled_by",
            length = 20
    )
    private CancelledBy cancelledBy;

    @Column(
            name = "cancellation_reason",
            length = 500
    )
    private String cancellationReason;

    @Column(
            name = "cancelled_at"
    )
    private LocalDateTime cancelledAt;

    @Column(
            name = "completed_at"
    )
    private LocalDateTime completedAt;

    @Column(
            name = "no_show_at"
    )
    private LocalDateTime noShowAt;

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