/*
package com.naveen.healthcare.claimservice.service;

import com.naveen.healthcare.claimservice.dto.request.ClaimVerificationTestRequest;
import com.naveen.healthcare.claimservice.dto.response.CombinedClaimVerificationResponse;

public interface ClaimVerificationService {

    CombinedClaimVerificationResponse verifyClaimRequirements(
            ClaimVerificationTestRequest request
    );
}*/


package com.naveen.healthcare.claimservice.service;

import com.naveen.healthcare.claimservice.dto.request.ClaimVerificationTestRequest;
import com.naveen.healthcare.claimservice.dto.response.ClaimSubmissionContextResponse;
import com.naveen.healthcare.claimservice.dto.response.CombinedClaimVerificationResponse;

import java.util.UUID;

public interface ClaimVerificationService {

    CombinedClaimVerificationResponse verifyClaimRequirements(
            ClaimVerificationTestRequest request
    );

    ClaimSubmissionContextResponse getClaimSubmissionContext(
            UUID appointmentId,
            UUID medicalRecordId
    );
}