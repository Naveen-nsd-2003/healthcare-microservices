package com.naveen.healthcare.claimservice.service;

import com.naveen.healthcare.claimservice.entity.Claim;

public interface ClaimEventPublisher {

    void publishClaimApproved(
            Claim claim
    );
}