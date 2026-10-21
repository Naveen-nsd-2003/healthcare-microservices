/*
package com.naveen.healthcare.appointmentservice.repository;

import com.naveen.healthcare.appointmentservice.entity.OutboxEvent;
import com.naveen.healthcare.appointmentservice.enums.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    boolean existsByEventId(
            UUID eventId
    );

    Optional<OutboxEvent> findByEventId(
            UUID eventId
    );

    List<OutboxEvent>
    findTop50ByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            OutboxEventStatus status,
            LocalDateTime nextAttemptAt
    );

    List<OutboxEvent>
    findTop50ByStatusAndProcessingStartedAtLessThanEqualOrderByProcessingStartedAtAsc(
            OutboxEventStatus status,
            LocalDateTime processingStartedAt
    );

    long countByStatus(
            OutboxEventStatus status
    );
}*/


package com.naveen.healthcare.appointmentservice.repository;

import com.naveen.healthcare.appointmentservice.entity.OutboxEvent;
import com.naveen.healthcare.appointmentservice.enums.OutboxEventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OutboxEventRepository
        extends JpaRepository<OutboxEvent, UUID> {

    boolean existsByEventId(
            UUID eventId
    );

    Optional<OutboxEvent> findByEventId(
            UUID eventId
    );

    long countByStatus(
            OutboxEventStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select outboxEvent
            from OutboxEvent outboxEvent
            where outboxEvent.status = :status
              and outboxEvent.nextAttemptAt <= :currentTime
            order by outboxEvent.createdAt asc
            """
    )
    List<OutboxEvent> findEligibleEventsForProcessing(
            @Param("status")
            OutboxEventStatus status,

            @Param("currentTime")
            LocalDateTime currentTime,

            Pageable pageable
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
            """
            select outboxEvent
            from OutboxEvent outboxEvent
            where outboxEvent.status = :status
              and outboxEvent.processingStartedAt is not null
              and outboxEvent.processingStartedAt <= :staleBefore
            order by outboxEvent.processingStartedAt asc
            """
    )
    List<OutboxEvent> findStaleProcessingEvents(
            @Param("status")
            OutboxEventStatus status,

            @Param("staleBefore")
            LocalDateTime staleBefore,

            Pageable pageable
    );
}
