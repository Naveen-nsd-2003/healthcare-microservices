/*
package com.naveen.healthcare.claimservice.controller;

import com.naveen.healthcare.claimservice.dto.request.ApproveClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.CancelClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.RejectClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.SubmitClaimRequest;
import com.naveen.healthcare.claimservice.dto.response.ClaimResponse;
import com.naveen.healthcare.claimservice.service.ClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody SubmitClaimRequest request
    ) {

        ClaimResponse response =
                claimService.submitClaim(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<ClaimResponse>>
    getProviderClaims(
            Authentication authentication
    ) {

        List<ClaimResponse> response =
                claimService.getProviderClaims(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{claimId}")
    public ResponseEntity<ClaimResponse>
    getProviderClaimById(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getProviderClaimById(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{claimId}/cancel")
    public ResponseEntity<ClaimResponse>
    cancelProviderClaim(
            Authentication authentication,
            @PathVariable UUID claimId,
            @Valid @RequestBody CancelClaimRequest request
    ) {

        ClaimResponse response =
                claimService.cancelProviderClaim(
                        authentication.getName(),
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer")
    public ResponseEntity<List<ClaimResponse>>
    getPayerClaims(
            Authentication authentication
    ) {

        List<ClaimResponse> response =
                claimService.getPayerClaims(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer/{claimId}")
    public ResponseEntity<ClaimResponse>
    getPayerClaimById(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getPayerClaimById(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{claimId}/review")
    public ResponseEntity<ClaimResponse>
    startClaimReview(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.startClaimReview(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{claimId}/approve")
    public ResponseEntity<ClaimResponse>
    approveClaim(
            Authentication authentication,
            @PathVariable UUID claimId,
            @Valid @RequestBody ApproveClaimRequest request
    ) {

        ClaimResponse response =
                claimService.approveClaim(
                        authentication.getName(),
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{claimId}/reject")
    public ResponseEntity<ClaimResponse>
    rejectClaim(
            Authentication authentication,
            @PathVariable UUID claimId,
            @Valid @RequestBody RejectClaimRequest request
    ) {

        ClaimResponse response =
                claimService.rejectClaim(
                        authentication.getName(),
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<List<ClaimResponse>>
    getMyClaims(
            Authentication authentication
    ) {

        List<ClaimResponse> response =
                claimService.getMyClaims(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/{claimId}")
    public ResponseEntity<ClaimResponse>
    getMyClaimById(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getMyClaimById(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }
}*/

package com.naveen.healthcare.claimservice.controller;

import com.naveen.healthcare.claimservice.dto.request.ApproveClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.CancelClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.RejectClaimRequest;
import com.naveen.healthcare.claimservice.dto.request.SubmitClaimRequest;
import com.naveen.healthcare.claimservice.dto.response.ClaimResponse;
import com.naveen.healthcare.claimservice.dto.response.ClaimSubmissionContextResponse;
import com.naveen.healthcare.claimservice.service.ClaimService;
import com.naveen.healthcare.claimservice.service.ClaimVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    private final ClaimVerificationService
            claimVerificationService;

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/submission-context")
    public ResponseEntity<ClaimSubmissionContextResponse>
    getClaimSubmissionContext(
            @RequestParam UUID appointmentId,
            @RequestParam UUID medicalRecordId
    ) {

        ClaimSubmissionContextResponse response =
                claimVerificationService
                        .getClaimSubmissionContext(
                                appointmentId,
                                medicalRecordId
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody SubmitClaimRequest request
    ) {

        ClaimResponse response =
                claimService.submitClaim(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<ClaimResponse>>
    getProviderClaims(
            Authentication authentication
    ) {

        List<ClaimResponse> response =
                claimService.getProviderClaims(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{claimId}")
    public ResponseEntity<ClaimResponse>
    getProviderClaimById(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getProviderClaimById(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{claimId}/cancel")
    public ResponseEntity<ClaimResponse>
    cancelProviderClaim(
            Authentication authentication,
            @PathVariable UUID claimId,
            @Valid @RequestBody CancelClaimRequest request
    ) {

        ClaimResponse response =
                claimService.cancelProviderClaim(
                        authentication.getName(),
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer")
    public ResponseEntity<List<ClaimResponse>>
    getPayerClaims(
            Authentication authentication
    ) {

        List<ClaimResponse> response =
                claimService.getPayerClaims(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer/{claimId}")
    public ResponseEntity<ClaimResponse>
    getPayerClaimById(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getPayerClaimById(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{claimId}/review")
    public ResponseEntity<ClaimResponse>
    startClaimReview(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.startClaimReview(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{claimId}/approve")
    public ResponseEntity<ClaimResponse>
    approveClaim(
            Authentication authentication,
            @PathVariable UUID claimId,
            @Valid @RequestBody ApproveClaimRequest request
    ) {

        ClaimResponse response =
                claimService.approveClaim(
                        authentication.getName(),
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{claimId}/reject")
    public ResponseEntity<ClaimResponse>
    rejectClaim(
            Authentication authentication,
            @PathVariable UUID claimId,
            @Valid @RequestBody RejectClaimRequest request
    ) {

        ClaimResponse response =
                claimService.rejectClaim(
                        authentication.getName(),
                        claimId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<List<ClaimResponse>>
    getMyClaims(
            Authentication authentication
    ) {

        List<ClaimResponse> response =
                claimService.getMyClaims(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/{claimId}")
    public ResponseEntity<ClaimResponse>
    getMyClaimById(
            Authentication authentication,
            @PathVariable UUID claimId
    ) {

        ClaimResponse response =
                claimService.getMyClaimById(
                        authentication.getName(),
                        claimId
                );

        return ResponseEntity.ok(response);
    }
}
