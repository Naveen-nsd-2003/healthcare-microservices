package com.naveen.healthcare.providerservice.repository;

import com.naveen.healthcare.providerservice.entity.DoctorAvailability;
import com.naveen.healthcare.providerservice.enums.AvailabilityStatus;
import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, UUID> {

    List<DoctorAvailability>
    findAllByDoctorIdAndProviderOrganizationAdminEmailIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(
            UUID doctorId,
            String providerAdminEmail
    );

    List<DoctorAvailability>
    findAllByProviderOrganizationAdminEmailIgnoreCaseOrderByDayOfWeekAscStartTimeAsc(
            String providerAdminEmail
    );

    Optional<DoctorAvailability>
    findByIdAndProviderOrganizationAdminEmailIgnoreCase(
            UUID availabilityId,
            String providerAdminEmail
    );

    List<DoctorAvailability>
    findAllByDoctorIdAndStatusAndDoctorStatusAndDoctorDepartmentStatusAndProviderOrganizationStatusOrderByDayOfWeekAscStartTimeAsc(
            UUID doctorId,
            AvailabilityStatus availabilityStatus,
            DoctorStatus doctorStatus,
            DepartmentStatus departmentStatus,
            ProviderStatus providerStatus
    );

    @Query("""
            SELECT COUNT(availability) > 0
            FROM DoctorAvailability availability
            WHERE availability.doctor.id = :doctorId
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.startTime < :newEndTime
              AND availability.endTime > :newStartTime
            """)
    boolean existsOverlappingAvailability(
            @Param("doctorId")
            UUID doctorId,

            @Param("dayOfWeek")
            DayOfWeek dayOfWeek,

            @Param("newStartTime")
            LocalTime newStartTime,

            @Param("newEndTime")
            LocalTime newEndTime
    );

    @Query("""
            SELECT COUNT(availability) > 0
            FROM DoctorAvailability availability
            WHERE availability.doctor.id = :doctorId
              AND availability.dayOfWeek = :dayOfWeek
              AND availability.id <> :availabilityId
              AND availability.startTime < :newEndTime
              AND availability.endTime > :newStartTime
            """)
    boolean existsOverlappingAvailabilityExcludingId(
            @Param("doctorId")
            UUID doctorId,

            @Param("dayOfWeek")
            DayOfWeek dayOfWeek,

            @Param("newStartTime")
            LocalTime newStartTime,

            @Param("newEndTime")
            LocalTime newEndTime,

            @Param("availabilityId")
            UUID availabilityId
    );
}