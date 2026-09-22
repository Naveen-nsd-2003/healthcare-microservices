package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.response.ProviderBookingVerificationResponse;
import com.naveen.healthcare.providerservice.dto.response.ProviderIdentityResponse;
import com.naveen.healthcare.providerservice.service.ProviderBookingVerificationService;
import com.naveen.healthcare.providerservice.service.ProviderOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.naveen.healthcare.providerservice.dto.response.InternalDoctorScheduleResponse;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/internal/providers")
@RequiredArgsConstructor
public class InternalProviderController {

    private final ProviderBookingVerificationService
            providerBookingVerificationService;

    private final ProviderOrganizationService
            providerOrganizationService;

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/doctors/{doctorId}/booking-verification")
    public ResponseEntity<ProviderBookingVerificationResponse>
    verifyBookingSlot(
            @PathVariable UUID doctorId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate appointmentDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
            LocalTime startTime
    ) {

        ProviderBookingVerificationResponse response =
                providerBookingVerificationService.verifyBookingSlot(
                        doctorId,
                        appointmentDate,
                        startTime
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/me/identity")
    public ResponseEntity<ProviderIdentityResponse>
    getAuthenticatedProviderIdentity(
            Authentication authentication
    ) {

        ProviderIdentityResponse response =
                providerOrganizationService
                        .getInternalProviderIdentity(
                                authentication.getName()
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/doctors/{doctorId}/schedule")
    public ResponseEntity<InternalDoctorScheduleResponse>
    getDoctorScheduleForDate(
            @PathVariable UUID doctorId,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate appointmentDate
    ) {

        InternalDoctorScheduleResponse response =
                providerBookingVerificationService
                        .getDoctorScheduleForDate(
                                doctorId,
                                appointmentDate
                        );

        return ResponseEntity.ok(response);
    }
}