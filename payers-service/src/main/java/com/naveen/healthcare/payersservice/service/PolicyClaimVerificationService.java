/*
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
}*/


package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.payersservice.dto.response.EligibleClaimPolicyResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PolicyClaimVerificationService {

    ClaimPolicyVerificationResponse verifyPolicyForClaim(
            UUID policyId,
            UUID memberId,
            LocalDate serviceDate
    );

    List<EligibleClaimPolicyResponse>
    getEligiblePoliciesForClaim(
            UUID memberId,
            LocalDate serviceDate
    );
}
