package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.payersservice.service.PolicyClaimVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/internal/policies")
@RequiredArgsConstructor
public class InternalPolicyController {

    private final PolicyClaimVerificationService
            policyClaimVerificationService;

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/{policyId}/claim-verification")
    public ResponseEntity<ClaimPolicyVerificationResponse>
    verifyPolicyForClaim(
            @PathVariable UUID policyId,

            @RequestParam UUID memberId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate serviceDate
    ) {

        ClaimPolicyVerificationResponse response =
                policyClaimVerificationService
                        .verifyPolicyForClaim(
                                policyId,
                                memberId,
                                serviceDate
                        );

        return ResponseEntity.ok(response);
    }
}