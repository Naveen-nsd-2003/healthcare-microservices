package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.request.CreateDoctorAvailabilityRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateAvailabilityStatusRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorAvailabilityRequest;
import com.naveen.healthcare.providerservice.dto.response.DoctorAvailabilityResponse;
import com.naveen.healthcare.providerservice.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class DoctorAvailabilityController {

    private final DoctorAvailabilityService
            doctorAvailabilityService;

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ResponseEntity<DoctorAvailabilityResponse>
    createAvailability(
            Authentication authentication,
            @Valid
            @RequestBody
            CreateDoctorAvailabilityRequest request
    ) {

        DoctorAvailabilityResponse response =
                doctorAvailabilityService.createAvailability(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<DoctorAvailabilityResponse>>
    getMyAvailabilities(
            Authentication authentication
    ) {

        List<DoctorAvailabilityResponse> response =
                doctorAvailabilityService.getMyAvailabilities(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/doctor/{doctorId}")
    public ResponseEntity<List<DoctorAvailabilityResponse>>
    getMyDoctorAvailabilities(
            Authentication authentication,
            @PathVariable UUID doctorId
    ) {

        List<DoctorAvailabilityResponse> response =
                doctorAvailabilityService
                        .getMyDoctorAvailabilities(
                                authentication.getName(),
                                doctorId
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{availabilityId}")
    public ResponseEntity<DoctorAvailabilityResponse>
    getMyAvailabilityById(
            Authentication authentication,
            @PathVariable UUID availabilityId
    ) {

        DoctorAvailabilityResponse response =
                doctorAvailabilityService
                        .getMyAvailabilityById(
                                authentication.getName(),
                                availabilityId
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/provider/{availabilityId}")
    public ResponseEntity<DoctorAvailabilityResponse>
    updateMyAvailability(
            Authentication authentication,
            @PathVariable UUID availabilityId,
            @Valid
            @RequestBody
            UpdateDoctorAvailabilityRequest request
    ) {

        DoctorAvailabilityResponse response =
                doctorAvailabilityService
                        .updateMyAvailability(
                                authentication.getName(),
                                availabilityId,
                                request
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{availabilityId}/status")
    public ResponseEntity<DoctorAvailabilityResponse>
    updateMyAvailabilityStatus(
            Authentication authentication,
            @PathVariable UUID availabilityId,
            @Valid
            @RequestBody
            UpdateAvailabilityStatusRequest request
    ) {

        DoctorAvailabilityResponse response =
                doctorAvailabilityService
                        .updateMyAvailabilityStatus(
                                authentication.getName(),
                                availabilityId,
                                request
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @DeleteMapping("/provider/{availabilityId}")
    public ResponseEntity<Void> deleteMyAvailability(
            Authentication authentication,
            @PathVariable UUID availabilityId
    ) {

        doctorAvailabilityService.deleteMyAvailability(
                authentication.getName(),
                availabilityId
        );

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/doctor/{doctorId}")
    public ResponseEntity<List<DoctorAvailabilityResponse>>
    getAvailableDoctorSchedules(
            @PathVariable UUID doctorId
    ) {

        List<DoctorAvailabilityResponse> response =
                doctorAvailabilityService
                        .getAvailableDoctorSchedules(
                                doctorId
                        );

        return ResponseEntity.ok(response);
    }
}