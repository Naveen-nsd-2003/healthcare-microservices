package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.request.CreateEnrollmentRequest;
import com.naveen.healthcare.payersservice.dto.request.ReviewEnrollmentRequest;
import com.naveen.healthcare.payersservice.dto.response.EnrollmentResponse;
import com.naveen.healthcare.payersservice.service.EnrollmentService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping
    public ResponseEntity<EnrollmentResponse> createEnrollment(
            Authentication authentication,
            @Valid @RequestBody CreateEnrollmentRequest request
    ) {

        EnrollmentResponse response =
                enrollmentService.createEnrollment(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<List<EnrollmentResponse>>
    getMyEnrollments(
            Authentication authentication
    ) {

        List<EnrollmentResponse> response =
                enrollmentService.getMyEnrollments(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/{enrollmentId}")
    public ResponseEntity<EnrollmentResponse>
    getMyEnrollmentById(
            Authentication authentication,
            @PathVariable UUID enrollmentId
    ) {

        EnrollmentResponse response =
                enrollmentService.getMyEnrollmentById(
                        authentication.getName(),
                        enrollmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/me/{enrollmentId}/cancel")
    public ResponseEntity<EnrollmentResponse>
    cancelMyEnrollment(
            Authentication authentication,
            @PathVariable UUID enrollmentId
    ) {

        EnrollmentResponse response =
                enrollmentService.cancelMyEnrollment(
                        authentication.getName(),
                        enrollmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer")
    public ResponseEntity<List<EnrollmentResponse>>
    getPayerEnrollments(
            Authentication authentication
    ) {

        List<EnrollmentResponse> response =
                enrollmentService.getPayerEnrollments(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer/{enrollmentId}")
    public ResponseEntity<EnrollmentResponse>
    getPayerEnrollmentById(
            Authentication authentication,
            @PathVariable UUID enrollmentId
    ) {

        EnrollmentResponse response =
                enrollmentService.getPayerEnrollmentById(
                        authentication.getName(),
                        enrollmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{enrollmentId}/approve")
    public ResponseEntity<EnrollmentResponse>
    approveEnrollment(
            Authentication authentication,
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody ReviewEnrollmentRequest request
    ) {

        EnrollmentResponse response =
                enrollmentService.approveEnrollment(
                        authentication.getName(),
                        enrollmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/payer/{enrollmentId}/reject")
    public ResponseEntity<EnrollmentResponse>
    rejectEnrollment(
            Authentication authentication,
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody ReviewEnrollmentRequest request
    ) {

        EnrollmentResponse response =
                enrollmentService.rejectEnrollment(
                        authentication.getName(),
                        enrollmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }
}