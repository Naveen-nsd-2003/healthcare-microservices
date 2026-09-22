package com.naveen.healthcare.claimservice.client;

import com.naveen.healthcare.claimservice.config.FeignClientConfig;
import com.naveen.healthcare.claimservice.dto.response.ClaimMedicalRecordVerificationResponse;
import com.naveen.healthcare.claimservice.dto.response.ProviderIdentityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(
        name = "provider-service",
        configuration = FeignClientConfig.class
)
public interface ProviderServiceClient {

    @GetMapping(
            "/internal/medical-records/"
                    + "{medicalRecordId}/claim-verification"
    )
    ClaimMedicalRecordVerificationResponse verifyMedicalRecordForClaim(
            @PathVariable("medicalRecordId")
            UUID medicalRecordId,

            @RequestParam("appointmentId")
            UUID appointmentId
    );

    @GetMapping("/internal/providers/me/identity")
    ProviderIdentityResponse getAuthenticatedProviderIdentity();
}