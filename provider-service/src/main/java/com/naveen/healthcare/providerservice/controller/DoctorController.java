package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.request.CreateDoctorRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.DoctorResponse;
import com.naveen.healthcare.providerservice.service.DoctorService;
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
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(
            Authentication authentication,
            @Valid @RequestBody CreateDoctorRequest request
    ) {

        DoctorResponse response =
                doctorService.createDoctor(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<DoctorResponse>> getMyDoctors(
            Authentication authentication
    ) {

        List<DoctorResponse> response =
                doctorService.getMyDoctors(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{doctorId}")
    public ResponseEntity<DoctorResponse> getMyDoctorById(
            Authentication authentication,
            @PathVariable UUID doctorId
    ) {

        DoctorResponse response =
                doctorService.getMyDoctorById(
                        authentication.getName(),
                        doctorId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/provider/{doctorId}")
    public ResponseEntity<DoctorResponse> updateMyDoctor(
            Authentication authentication,
            @PathVariable UUID doctorId,
            @Valid @RequestBody UpdateDoctorRequest request
    ) {

        DoctorResponse response =
                doctorService.updateMyDoctor(
                        authentication.getName(),
                        doctorId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{doctorId}/status")
    public ResponseEntity<DoctorResponse> updateMyDoctorStatus(
            Authentication authentication,
            @PathVariable UUID doctorId,
            @Valid @RequestBody UpdateDoctorStatusRequest request
    ) {

        DoctorResponse response =
                doctorService.updateMyDoctorStatus(
                        authentication.getName(),
                        doctorId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available")
    public ResponseEntity<List<DoctorResponse>> getAvailableDoctors() {

        List<DoctorResponse> response =
                doctorService.getAvailableDoctors();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/provider/{providerId}")
    public ResponseEntity<List<DoctorResponse>>
    getAvailableDoctorsByProvider(
            @PathVariable UUID providerId
    ) {

        List<DoctorResponse> response =
                doctorService.getAvailableDoctorsByProvider(
                        providerId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/department/{departmentId}")
    public ResponseEntity<List<DoctorResponse>>
    getAvailableDoctorsByDepartment(
            @PathVariable UUID departmentId
    ) {

        List<DoctorResponse> response =
                doctorService.getAvailableDoctorsByDepartment(
                        departmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/{doctorId}")
    public ResponseEntity<DoctorResponse> getAvailableDoctorById(
            @PathVariable UUID doctorId
    ) {

        DoctorResponse response =
                doctorService.getAvailableDoctorById(
                        doctorId
                );

        return ResponseEntity.ok(response);
    }
}