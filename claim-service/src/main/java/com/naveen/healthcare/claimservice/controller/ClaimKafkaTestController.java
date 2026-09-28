/*
package com.naveen.healthcare.claimservice.controller;

import com.naveen.healthcare.claimservice.entity.Claim;
import com.naveen.healthcare.claimservice.repository.ClaimRepository;
import com.naveen.healthcare.claimservice.service.ClaimEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/claims/test")
@RequiredArgsConstructor
public class ClaimKafkaTestController {

    private final ClaimRepository claimRepository;

    private final ClaimEventPublisher claimEventPublisher;

    @PostMapping("/submitted/{claimId}")
    public ResponseEntity<String> publishSubmitted(
            @PathVariable UUID claimId
    ) {

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        claimEventPublisher.publishClaimSubmitted(
                claim
        );

        return ResponseEntity.ok(
                "CLAIM_SUBMITTED event published"
        );
    }

    @PostMapping("/under-review/{claimId}")
    public ResponseEntity<String> publishUnderReview(
            @PathVariable UUID claimId
    ) {

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        claimEventPublisher.publishClaimUnderReview(
                claim
        );

        return ResponseEntity.ok(
                "CLAIM_UNDER_REVIEW event published"
        );
    }

    @PostMapping("/rejected/{claimId}")
    public ResponseEntity<String> publishRejected(
            @PathVariable UUID claimId
    ) {

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        claimEventPublisher.publishClaimRejected(
                claim
        );

        return ResponseEntity.ok(
                "CLAIM_REJECTED event published"
        );
    }

    @PostMapping("/cancelled/{claimId}")
    public ResponseEntity<String> publishCancelled(
            @PathVariable UUID claimId
    ) {

        Claim claim = claimRepository
                .findById(claimId)
                .orElseThrow();

        claimEventPublisher.publishClaimCancelled(
                claim
        );

        return ResponseEntity.ok(
                "CLAIM_CANCELLED event published"
        );
    }
}*/
