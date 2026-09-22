package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.request.CreatePayerRequest;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/payers")
@RequiredArgsConstructor
public class PayerController {


    private final PayerService payerService;


    // =====================================================
    // ADMIN: CREATE PAYER ORGANIZATION
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PayerResponse> createPayer(
            @Valid
            @RequestBody
            CreatePayerRequest request
    ) {


        PayerResponse response =
                payerService.createPayer(request);


        return ResponseEntity

                .status(HttpStatus.CREATED)

                .body(response);
    }


    // =====================================================
    // ADMIN: GET ALL PAYER ORGANIZATIONS
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PayerResponse>> getAllPayers() {


        List<PayerResponse> response =
                payerService.getAllPayers();


        return ResponseEntity.ok(response);
    }


    // =====================================================
    // PAYER: GET OWN ORGANIZATION
    // =====================================================

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/me")
    public ResponseEntity<PayerResponse> getMyOrganization(
            Authentication authentication
    ) {


        /*
         * authentication.getName() contains the JWT subject.
         *
         * In this project, the JWT subject is the user email.
         */
        String authenticatedEmail =
                authentication.getName();


        PayerResponse response =
                payerService.getMyOrganization(
                        authenticatedEmail
                );


        return ResponseEntity.ok(response);
    }


    // =====================================================
    // PAYER: UPDATE OWN ORGANIZATION
    // =====================================================

    @PreAuthorize("hasRole('PAYER')")
    @PutMapping("/me")
    public ResponseEntity<PayerResponse> updateMyOrganization(
            Authentication authentication,

            @Valid
            @RequestBody
            UpdatePayerRequest request
    ) {


        String authenticatedEmail =
                authentication.getName();


        PayerResponse response =
                payerService.updateMyOrganization(

                        authenticatedEmail,

                        request
                );


        return ResponseEntity.ok(response);
    }


    // =====================================================
    // ADMIN: GET PAYER BY ID
    // =====================================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{payerId}")
    public ResponseEntity<PayerResponse> getPayerById(
            @PathVariable
            UUID payerId
    ) {


        PayerResponse response =
                payerService.getPayerById(payerId);


        return ResponseEntity.ok(response);
    }


    // =====================================================
    // ADMIN: UPDATE PAYER STATUS
    // =====================================================

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


        return ResponseEntity.ok(response);
    }
}