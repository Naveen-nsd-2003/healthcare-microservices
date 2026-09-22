package com.naveen.healthcare.authservice.repository;

import com.naveen.healthcare.authservice.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {

    Optional<Otp> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByEmail(String email);
}