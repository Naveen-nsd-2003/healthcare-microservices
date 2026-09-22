package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.request.CreateMedicalRecordRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateMedicalRecordRequest;
import com.naveen.healthcare.providerservice.dto.response.MedicalRecordResponse;
import com.naveen.healthcare.providerservice.service.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ResponseEntity<MedicalRecordResponse>
    createMedicalRecord(
            Authentication authentication,
            @Valid @RequestBody CreateMedicalRecordRequest request
    ) {

        MedicalRecordResponse response =
                medicalRecordService.createMedicalRecord(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<MedicalRecordResponse>>
    getProviderMedicalRecords(
            Authentication authentication
    ) {

        List<MedicalRecordResponse> response =
                medicalRecordService.getProviderMedicalRecords(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/doctor/{doctorId}")
    public ResponseEntity<List<MedicalRecordResponse>>
    getProviderMedicalRecordsByDoctor(
            Authentication authentication,
            @PathVariable UUID doctorId
    ) {

        List<MedicalRecordResponse> response =
                medicalRecordService
                        .getProviderMedicalRecordsByDoctor(
                                authentication.getName(),
                                doctorId
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{medicalRecordId}")
    public ResponseEntity<MedicalRecordResponse>
    getProviderMedicalRecordById(
            Authentication authentication,
            @PathVariable UUID medicalRecordId
    ) {

        MedicalRecordResponse response =
                medicalRecordService
                        .getProviderMedicalRecordById(
                                authentication.getName(),
                                medicalRecordId
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/provider/{medicalRecordId}")
    public ResponseEntity<MedicalRecordResponse>
    updateProviderMedicalRecord(
            Authentication authentication,
            @PathVariable UUID medicalRecordId,
            @Valid @RequestBody UpdateMedicalRecordRequest request
    ) {

        MedicalRecordResponse response =
                medicalRecordService
                        .updateProviderMedicalRecord(
                                authentication.getName(),
                                medicalRecordId,
                                request
                        );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<List<MedicalRecordResponse>>
    getMyMedicalRecords(
            Authentication authentication
    ) {

        List<MedicalRecordResponse> response =
                medicalRecordService.getMyMedicalRecords(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/{medicalRecordId}")
    public ResponseEntity<MedicalRecordResponse>
    getMyMedicalRecordById(
            Authentication authentication,
            @PathVariable UUID medicalRecordId
    ) {

        MedicalRecordResponse response =
                medicalRecordService.getMyMedicalRecordById(
                        authentication.getName(),
                        medicalRecordId
                );

        return ResponseEntity.ok(response);
    }
}