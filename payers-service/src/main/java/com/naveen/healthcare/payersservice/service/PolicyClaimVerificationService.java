package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.response.ClaimPolicyVerificationResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface PolicyClaimVerificationService {

    ClaimPolicyVerificationResponse verifyPolicyForClaim(
            UUID policyId,
            UUID memberId,
            LocalDate serviceDate
    );
}