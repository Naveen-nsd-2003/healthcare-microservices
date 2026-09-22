package com.naveen.healthcare.claimservice.service;

import com.naveen.healthcare.claimservice.dto.request.ApproveClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.CancelClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.RejectClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.SubmitClaimRequest;
import com.naveen.healthcare.claimservice.dto.response.ClaimResponse;

import java.util.List;
import java.util.UUID;

public interface ClaimService {

    ClaimResponse submitClaim(
            SubmitClaimRequest request
    );

    List<ClaimResponse> getProviderClaims(
            String authenticatedProviderEmail
    );

    ClaimResponse getProviderClaimById(
            String authenticatedProviderEmail,
            UUID claimId
    );

    ClaimResponse cancelProviderClaim(
            String authenticatedProviderEmail,
            UUID claimId,
            CancelClaimRequest request
    );

    List<ClaimResponse> getPayerClaims(
            String authenticatedPayerEmail
    );

    ClaimResponse getPayerClaimById(
            String authenticatedPayerEmail,
            UUID claimId
    );

    ClaimResponse startClaimReview(
            String authenticatedPayerEmail,
            UUID claimId
    );

    ClaimResponse approveClaim(
            String authenticatedPayerEmail,
            UUID claimId,
            ApproveClaimRequest request
    );

    ClaimResponse rejectClaim(
            String authenticatedPayerEmail,
            UUID claimId,
            RejectClaimRequest request
    );

    List<ClaimResponse> getMyClaims(
            String authenticatedMemberEmail
    );

    ClaimResponse getMyClaimById(
            String authenticatedMemberEmail,
            UUID claimId
    );
}