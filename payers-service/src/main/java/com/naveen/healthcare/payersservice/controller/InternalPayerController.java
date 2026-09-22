package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.response.PayerIdentityResponse;
import com.naveen.healthcare.payersservice.service.PayerIdentityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/payers")
@RequiredArgsConstructor
public class InternalPayerController {

    private final PayerIdentityService payerIdentityService;

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/me/identity")
    public ResponseEntity<PayerIdentityResponse>
    getAuthenticatedPayerIdentity(
            Authentication authentication
    ) {

        PayerIdentityResponse response =
                payerIdentityService
                        .getAuthenticatedPayerIdentity(
                                authentication.getName()
                        );

        return ResponseEntity.ok(response);
    }
}