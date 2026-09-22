package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.dto.response.ProviderBookingVerificationResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface ProviderVerificationService {

    ProviderBookingVerificationResponse verifyBookingSlot(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    );
}