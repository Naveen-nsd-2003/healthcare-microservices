/*
package com.naveen.healthcare.claimservice.client;

import com.naveen.healthcare.claimservice.config.FeignClientConfig;
import com.naveen.healthcare.claimservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.PayerIdentityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.UUID;

@FeignClient(
        name = "payers-service",
        configuration = FeignClientConfig.class
)
public interface PayersServiceClient {

    @GetMapping(
            "/internal/policies/{policyId}/claim-verification"
    )
    ClaimPolicyVerificationResponse verifyPolicyForClaim(
            @PathVariable("policyId")
            UUID policyId,

            @RequestParam("memberId")
            UUID memberId,

            @RequestParam("serviceDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate serviceDate
    );

    @GetMapping("/internal/payers/me/identity")
    PayerIdentityResponse getAuthenticatedPayerIdentity();
}
*/


package com.naveen.healthcare.claimservice.client;

import com.naveen.healthcare.claimservice.config.FeignClientConfig;
import com.naveen.healthcare.claimservice.dto.response.ClaimPolicyVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.EligibleClaimPolicyResponse;
import com.naveen.healthcare.claimservice.dto.response.PayerIdentityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@FeignClient(
        name = "payers-service",
        configuration = FeignClientConfig.class
)
public interface PayersServiceClient {

    @GetMapping(
            "/internal/policies/claim-eligible"
    )
    List<EligibleClaimPolicyResponse>
    getEligiblePoliciesForClaim(
            @RequestParam("memberId")
            UUID memberId,

            @RequestParam("serviceDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate serviceDate
    );

    @GetMapping(
            "/internal/policies/{policyId}/claim-verification"
    )
    ClaimPolicyVerificationResponse verifyPolicyForClaim(
            @PathVariable("policyId")
            UUID policyId,

            @RequestParam("memberId")
            UUID memberId,

            @RequestParam("serviceDate")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate serviceDate
    );

    @GetMapping("/internal/payers/me/identity")
    PayerIdentityResponse getAuthenticatedPayerIdentity();
}
