package com.naveen.healthcare.memberservice.controller;

import com.naveen.healthcare.memberservice.dto.response.InternalMemberVerificationResponse;
import com.naveen.healthcare.memberservice.service.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/members")
@RequiredArgsConstructor
public class InternalMemberController {

    private final MemberProfileService memberProfileService;

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/verification")
    public ResponseEntity<InternalMemberVerificationResponse>
    getAuthenticatedMemberVerification(
            Authentication authentication
    ) {

        InternalMemberVerificationResponse response =
                memberProfileService.getInternalVerification(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }
}