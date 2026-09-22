package com.naveen.healthcare.memberservice.repository;

import com.naveen.healthcare.memberservice.entity.MemberProfile;
import com.naveen.healthcare.memberservice.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberProfileRepository
        extends JpaRepository<MemberProfile, UUID> {

    Optional<MemberProfile> findByEmailIgnoreCase(
            String email
    );

    boolean existsByEmailIgnoreCase(
            String email
    );

    List<MemberProfile> findAllByStatusOrderByCreatedAtDesc(
            MemberStatus status
    );

    List<MemberProfile> findAllByOrderByCreatedAtDesc();
}