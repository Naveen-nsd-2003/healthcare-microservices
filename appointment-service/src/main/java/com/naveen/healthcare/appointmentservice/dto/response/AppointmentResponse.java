package com.naveen.healthcare.appointmentservice.dto.response;

import com.naveen.healthcare.appointmentservice.enums.AppointmentStatus;
import com.naveen.healthcare.appointmentservice.enums.CancelledBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AppointmentResponse {

    private UUID id;

    private UUID memberId;

    private String memberEmail;

    private UUID providerId;

    private String providerOrganizationName;

    private UUID departmentId;

    private String departmentName;

    private UUID doctorId;

    private String doctorName;

    private String doctorSpecialization;

    private BigDecimal consultationFee;

    private LocalDate appointmentDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDurationMinutes;

    private String reasonForVisit;

    private AppointmentStatus status;

    private String providerNotes;

    private LocalDateTime confirmedAt;

    private CancelledBy cancelledBy;

    private String cancellationReason;

    private LocalDateTime cancelledAt;

    private LocalDateTime completedAt;

    private LocalDateTime noShowAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}