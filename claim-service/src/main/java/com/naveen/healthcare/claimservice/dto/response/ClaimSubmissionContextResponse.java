package com.naveen.healthcare.claimservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ClaimSubmissionContextResponse {

    private String message;

    private ClaimAppointmentVerificationResponse appointment;

    private ClaimMedicalRecordVerificationResponse medicalRecord;

    private List<EligibleClaimPolicyResponse> eligiblePolicies;
}