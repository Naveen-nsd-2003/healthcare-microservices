package com.naveen.healthcare.providerservice.controller;

import com.naveen.healthcare.providerservice.dto.request.CreateDepartmentRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDepartmentRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDepartmentStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.DepartmentResponse;
import com.naveen.healthcare.providerservice.service.DepartmentService;
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
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PreAuthorize("hasRole('PROVIDER')")
    @PostMapping
    public ResponseEntity<DepartmentResponse> createDepartment(
            Authentication authentication,
            @Valid @RequestBody CreateDepartmentRequest request
    ) {

        DepartmentResponse response =
                departmentService.createDepartment(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<DepartmentResponse>>
    getMyDepartments(
            Authentication authentication
    ) {

        List<DepartmentResponse> response =
                departmentService.getMyDepartments(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{departmentId}")
    public ResponseEntity<DepartmentResponse>
    getMyDepartmentById(
            Authentication authentication,
            @PathVariable UUID departmentId
    ) {

        DepartmentResponse response =
                departmentService.getMyDepartmentById(
                        authentication.getName(),
                        departmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PutMapping("/provider/{departmentId}")
    public ResponseEntity<DepartmentResponse>
    updateMyDepartment(
            Authentication authentication,
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {

        DepartmentResponse response =
                departmentService.updateMyDepartment(
                        authentication.getName(),
                        departmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{departmentId}/status")
    public ResponseEntity<DepartmentResponse>
    updateMyDepartmentStatus(
            Authentication authentication,
            @PathVariable UUID departmentId,
            @Valid
            @RequestBody
            UpdateDepartmentStatusRequest request
    ) {

        DepartmentResponse response =
                departmentService.updateMyDepartmentStatus(
                        authentication.getName(),
                        departmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/provider/{providerId}")
    public ResponseEntity<List<DepartmentResponse>>
    getAvailableDepartments(
            @PathVariable UUID providerId
    ) {

        List<DepartmentResponse> response =
                departmentService.getAvailableDepartments(
                        providerId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/{departmentId}")
    public ResponseEntity<DepartmentResponse>
    getAvailableDepartmentById(
            @PathVariable UUID departmentId
    ) {

        DepartmentResponse response =
                departmentService.getAvailableDepartmentById(
                        departmentId
                );

        return ResponseEntity.ok(response);
    }
}