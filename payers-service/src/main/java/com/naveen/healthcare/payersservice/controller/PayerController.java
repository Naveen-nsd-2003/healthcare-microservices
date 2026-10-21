package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.request.ApprovePayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.CreateMyPayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.CreatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.RejectPayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PayerResponse;
import com.naveen.healthcare.payersservice.service.PayerService;
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
@RequestMapping("/api/payers")
@RequiredArgsConstructor
public class PayerController {

    private final PayerService payerService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PayerResponse> createPayer(
            @Valid
            @RequestBody
            CreatePayerRequest request
    ) {

        PayerResponse response =
                payerService.createPayer(
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PostMapping("/me")
    public ResponseEntity<PayerResponse> createMyOrganization(
            Authentication authentication,
            @Valid
            @RequestBody
            CreateMyPayerOrganizationRequest request
    ) {

        PayerResponse response =
                payerService.createMyOrganization(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PayerResponse>> getAllPayers() {

        List<PayerResponse> response =
                payerService.getAllPayers();

        return ResponseEntity.ok(
                response
        );
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/me")
    public ResponseEntity<PayerResponse> getMyOrganization(
            Authentication authentication
    ) {

        PayerResponse response =
                payerService.getMyOrganization(
                        authentication.getName()
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PreAuthorize("hasRole('PAYER')")
    @PutMapping("/me")
    public ResponseEntity<PayerResponse> updateMyOrganization(
            Authentication authentication,
            @Valid
            @RequestBody
            UpdatePayerRequest request
    ) {

        PayerResponse response =
                payerService.updateMyOrganization(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{payerId}")
    public ResponseEntity<PayerResponse> getPayerById(
            @PathVariable
            UUID payerId
    ) {

        PayerResponse response =
                payerService.getPayerById(
                        payerId
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{payerId}/approve")
    public ResponseEntity<PayerResponse> approvePayerOrganization(
            @PathVariable
            UUID payerId,
            Authentication authentication,
            @Valid
            @RequestBody
            ApprovePayerOrganizationRequest request
    ) {

        PayerResponse response =
                payerService.approvePayerOrganization(
                        payerId,
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{payerId}/reject")
    public ResponseEntity<PayerResponse> rejectPayerOrganization(
            @PathVariable
            UUID payerId,
            Authentication authentication,
            @Valid
            @RequestBody
            RejectPayerOrganizationRequest request
    ) {

        PayerResponse response =
                payerService.rejectPayerOrganization(
                        payerId,
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{payerId}/status")
    public ResponseEntity<PayerResponse> updatePayerStatus(
            @PathVariable
            UUID payerId,
            @Valid
            @RequestBody
            UpdatePayerStatusRequest request
    ) {

        PayerResponse response =
                payerService.updatePayerStatus(
                        payerId,
                        request
                );

        return ResponseEntity.ok(
                response
        );
    }
}