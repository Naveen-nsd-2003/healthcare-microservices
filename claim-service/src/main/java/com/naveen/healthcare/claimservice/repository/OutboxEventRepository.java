package com.naveen.healthcare.claimservice.repository;

import com.naveen.healthcare.claimservice.entity.OutboxEvent;
import com.naveen.healthcare.claimservice.enums.OutboxEventStatus;
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
            select event
            from OutboxEvent event
            where event.status = :status
              and event.nextAttemptAt <= :currentTime
            order by event.createdAt asc
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
            select event
            from OutboxEvent event
            where event.status = :status
              and event.processingStartedAt is not null
              and event.processingStartedAt <= :staleBefore
            order by event.processingStartedAt asc
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
