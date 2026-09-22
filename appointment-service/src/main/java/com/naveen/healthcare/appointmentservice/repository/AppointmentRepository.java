package com.naveen.healthcare.appointmentservice.repository;

import com.naveen.healthcare.appointmentservice.entity.Appointment;
import com.naveen.healthcare.appointmentservice.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctorIdAndAppointmentDateAndStartTimeAndStatusIn(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime,
            Collection<AppointmentStatus> statuses
    );

    boolean existsByMemberIdAndAppointmentDateAndStartTimeAndStatusIn(
            UUID memberId,
            LocalDate appointmentDate,
            LocalTime startTime,
            Collection<AppointmentStatus> statuses
    );

    List<Appointment>
    findAllByMemberEmailIgnoreCaseOrderByAppointmentDateDescStartTimeDesc(
            String memberEmail
    );

    Optional<Appointment>
    findByIdAndMemberEmailIgnoreCase(
            UUID appointmentId,
            String memberEmail
    );

    List<Appointment>
    findAllByProviderIdOrderByAppointmentDateDescStartTimeDesc(
            UUID providerId
    );

    Optional<Appointment>
    findByIdAndProviderId(
            UUID appointmentId,
            UUID providerId
    );

    List<Appointment>
    findAllByDoctorIdAndAppointmentDateAndStatusInOrderByStartTimeAsc(
            UUID doctorId,
            LocalDate appointmentDate,
            Collection<AppointmentStatus> statuses
    );

    List<Appointment>
    findAllByProviderIdAndAppointmentDateOrderByStartTimeAsc(
            UUID providerId,
            LocalDate appointmentDate
    );
}