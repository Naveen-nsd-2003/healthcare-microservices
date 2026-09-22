package com.naveen.healthcare.memberservice.controller;

import com.naveen.healthcare.memberservice.dto.request.CreateMemberProfileRequest;
import com.naveen.healthcare.memberservice.dto.request.UpdateMemberProfileRequest;
import com.naveen.healthcare.memberservice.dto.request.UpdateMemberStatusRequest;
import com.naveen.healthcare.memberservice.dto.response.MemberProfileResponse;
import com.naveen.healthcare.memberservice.service.MemberProfileService;
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
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberProfileController {

    private final MemberProfileService memberProfileService;

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping("/me")
    public ResponseEntity<MemberProfileResponse> createMyProfile(
            Authentication authentication,
            @Valid @RequestBody CreateMemberProfileRequest request
    ) {

        MemberProfileResponse response =
                memberProfileService.createMyProfile(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> getMyProfile(
            Authentication authentication
    ) {

        MemberProfileResponse response =
                memberProfileService.getMyProfile(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PutMapping("/me")
    public ResponseEntity<MemberProfileResponse> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateMemberProfileRequest request
    ) {

        MemberProfileResponse response =
                memberProfileService.updateMyProfile(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<MemberProfileResponse>>
    getAllMemberProfiles() {

        List<MemberProfileResponse> response =
                memberProfileService.getAllMemberProfiles();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberProfileResponse>
    getMemberProfileById(
            @PathVariable UUID memberId
    ) {

        MemberProfileResponse response =
                memberProfileService.getMemberProfileById(
                        memberId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<MemberProfileResponse>
    updateMemberStatus(
            @PathVariable UUID memberId,
            @Valid @RequestBody UpdateMemberStatusRequest request
    ) {

        MemberProfileResponse response =
                memberProfileService.updateMemberStatus(
                        memberId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}