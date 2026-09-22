package com.naveen.healthcare.claimservice.service;

import com.naveen.healthcare.claimservice.dto.request.ClaimVerificationTestRequest;
import com.naveen.healthcare.claimservice.dto.response.CombinedClaimVerificationResponse;

public interface ClaimVerificationService {

    CombinedClaimVerificationResponse verifyClaimRequirements(
            ClaimVerificationTestRequest request
    );
}