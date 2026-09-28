package com.naveen.healthcare.claimservice.service;

import com.naveen.healthcare.claimservice.entity.Claim;

public interface ClaimEventPublisher {

    void publishClaimApproved(
            Claim claim
    );

    void publishClaimSubmitted(Claim claim);

    void publishClaimUnderReview(Claim claim);

    void publishClaimRejected(Claim claim);

    void publishClaimCancelled(Claim claim);
}