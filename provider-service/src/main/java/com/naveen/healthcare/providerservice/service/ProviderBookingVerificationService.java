package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.response.InternalDoctorScheduleResponse;
import com.naveen.healthcare.providerservice.dto.response.ProviderBookingVerificationResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface ProviderBookingVerificationService {

    ProviderBookingVerificationResponse verifyBookingSlot(
            UUID doctorId,
            LocalDate appointmentDate,
            LocalTime startTime
    );

    InternalDoctorScheduleResponse getDoctorScheduleForDate(
            UUID doctorId,
            LocalDate appointmentDate
    );
}