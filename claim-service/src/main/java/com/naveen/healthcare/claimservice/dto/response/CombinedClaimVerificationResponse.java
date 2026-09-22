package com.naveen.healthcare.claimservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CombinedClaimVerificationResponse {

    private String message;

    private ClaimAppointmentVerificationResponse appointment;

    private ClaimMedicalRecordVerificationResponse medicalRecord;

    private ClaimPolicyVerificationResponse policy;
}