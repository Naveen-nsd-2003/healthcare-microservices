package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.request.CreateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.ProviderOrganizationResponse;
import com.naveen.healthcare.providerservice.service.ProviderOrganizationService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderOrganizationController {

    private final ProviderOrganizationService
            providerOrganizationService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProviderOrganizationResponse>
    createProviderOrganization(
            @Valid
            @RequestBody
            CreateProviderOrganizationRequest request
    ) {

        ProviderOrganizationResponse response =
                providerOrganizationService
                        .createProviderOrganization(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ProviderOrganizationResponse>>
    getAllProviderOrganizations() {

        List<ProviderOrganizationResponse> response =
                providerOrganizationService
                        .getAllProviderOrganizations();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/me")
    public ResponseEntity<ProviderOrganizationResponse>
    getMyProviderOrganization(
            Authentication authentication
    ) {

        ProviderOrganizationResponse response =
                providerOrganizationService
                        .getMyProviderOrganization(
                                authentication.getName()
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/me")
    public ResponseEntity<ProviderOrganizationResponse>
    updateMyProviderOrganization(
            Authentication authentication,
            @Valid
            @RequestBody
            UpdateProviderOrganizationRequest request
    ) {

        ProviderOrganizationResponse response =
                providerOrganizationService
                        .updateMyProviderOrganization(
                                authentication.getName(),
                                request
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available")
    public ResponseEntity<List<ProviderOrganizationResponse>>
    getAvailableProviders() {

        List<ProviderOrganizationResponse> response =
                providerOrganizationService
                        .getAvailableProviders();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/{providerId}")
    public ResponseEntity<ProviderOrganizationResponse>
    getAvailableProviderById(
            @PathVariable UUID providerId
    ) {

        ProviderOrganizationResponse response =
                providerOrganizationService
                        .getAvailableProviderById(providerId);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{providerId}/status")
    public ResponseEntity<ProviderOrganizationResponse>
    updateProviderStatus(
            @PathVariable UUID providerId,
            @Valid
            @RequestBody
            UpdateProviderStatusRequest request
    ) {

        ProviderOrganizationResponse response =
                providerOrganizationService
                        .updateProviderStatus(
                                providerId,
                                request
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{providerId}")
    public ResponseEntity<ProviderOrganizationResponse>
    getProviderOrganizationById(
            @PathVariable UUID providerId
    ) {

        ProviderOrganizationResponse response =
                providerOrganizationService
                        .getProviderOrganizationById(providerId);

        return ResponseEntity.ok(response);
    }
}