package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.response.ClaimMedicalRecordVerificationResponse;
import com.naveen.healthcare.providerservice.service.MedicalRecordClaimVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/medical-records")
@RequiredArgsConstructor
public class InternalMedicalRecordController {

    private final MedicalRecordClaimVerificationService
            medicalRecordClaimVerificationService;

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/{medicalRecordId}/claim-verification")
    public ResponseEntity<ClaimMedicalRecordVerificationResponse>
    verifyMedicalRecordForClaim(
            Authentication authentication,
            @PathVariable UUID medicalRecordId,
            @RequestParam UUID appointmentId
    ) {

        ClaimMedicalRecordVerificationResponse response =
                medicalRecordClaimVerificationService
                        .verifyMedicalRecordForClaim(
                                authentication.getName(),
                                medicalRecordId,
                                appointmentId
                        );

        return ResponseEntity.ok(response);
    }
}