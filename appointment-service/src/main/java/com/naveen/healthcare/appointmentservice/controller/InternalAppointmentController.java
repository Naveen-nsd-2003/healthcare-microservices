package com.naveen.healthcare.appointmentservice.controller;

import com.naveen.healthcare.appointmentservice.dto.response.ClaimAppointmentVerificationResponse;
import com.naveen.healthcare.appointmentservice.dto.response.MedicalRecordAppointmentVerificationResponse;
import com.naveen.healthcare.appointmentservice.service.AppointmentClaimVerificationService;
import com.naveen.healthcare.appointmentservice.service.AppointmentMedicalRecordVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/appointments")
@RequiredArgsConstructor
public class InternalAppointmentController {

    private final AppointmentMedicalRecordVerificationService
            appointmentMedicalRecordVerificationService;

    private final AppointmentClaimVerificationService
            appointmentClaimVerificationService;

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping(
            "/{appointmentId}/medical-record-verification"
    )
    public ResponseEntity<
            MedicalRecordAppointmentVerificationResponse
            > verifyAppointmentForMedicalRecord(
            Authentication authentication,
            @PathVariable UUID appointmentId
    ) {

        MedicalRecordAppointmentVerificationResponse response =
                appointmentMedicalRecordVerificationService
                        .verifyCompletedAppointmentForMedicalRecord(
                                authentication.getName(),
                                appointmentId
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/{appointmentId}/claim-verification")
    public ResponseEntity<ClaimAppointmentVerificationResponse>
    verifyAppointmentForClaim(
            Authentication authentication,
            @PathVariable UUID appointmentId
    ) {

        ClaimAppointmentVerificationResponse response =
                appointmentClaimVerificationService
                        .verifyAppointmentForClaim(
                                authentication.getName(),
                                appointmentId
                        );

        return ResponseEntity.ok(response);
    }
}