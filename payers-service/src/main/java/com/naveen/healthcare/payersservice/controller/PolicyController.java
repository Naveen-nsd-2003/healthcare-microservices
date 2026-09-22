package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.request.UpdatePolicyStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PolicyResponse;
import com.naveen.healthcare.payersservice.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<List<PolicyResponse>> getMyPolicies(
            Authentication authentication
    ) {

        List<PolicyResponse> response =
                policyService.getMyPolicies(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/{policyId}")
    public ResponseEntity<PolicyResponse> getMyPolicyById(
            Authentication authentication,
            @PathVariable UUID policyId
    ) {

        PolicyResponse response =
                policyService.getMyPolicyById(
                        authentication.getName(),
                        policyId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer")
    public ResponseEntity<List<PolicyResponse>> getPayerPolicies(
            Authentication authentication
    ) {

        List<PolicyResponse> response =
                policyService.getPayerPolicies(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer/{policyId}")
    public ResponseEntity<PolicyResponse> getPayerPolicyById(
            Authentication authentication,
            @PathVariable UUID policyId
    ) {

        PolicyResponse response =
                policyService.getPayerPolicyById(
                        authentication.getName(),
                        policyId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{policyId}/status")
    public ResponseEntity<PolicyResponse> updatePolicyStatus(
            Authentication authentication,
            @PathVariable UUID policyId,
            @Valid @RequestBody UpdatePolicyStatusRequest request
    ) {

        PolicyResponse response =
                policyService.updatePolicyStatus(
                        authentication.getName(),
                        policyId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}