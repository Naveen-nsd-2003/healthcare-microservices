package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.response.ClaimMedicalRecordVerificationResponse;

import java.util.UUID;

public interface MedicalRecordClaimVerificationService {

    ClaimMedicalRecordVerificationResponse verifyMedicalRecordForClaim(
            String authenticatedProviderEmail,
            UUID medicalRecordId,
            UUID appointmentId
    );
}