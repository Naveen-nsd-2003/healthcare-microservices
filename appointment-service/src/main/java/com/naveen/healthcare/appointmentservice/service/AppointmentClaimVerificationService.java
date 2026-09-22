package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.dto.response.ClaimAppointmentVerificationResponse;

import java.util.UUID;

public interface AppointmentClaimVerificationService {

    ClaimAppointmentVerificationResponse verifyAppointmentForClaim(
            String authenticatedProviderEmail,
            UUID appointmentId
    );
}