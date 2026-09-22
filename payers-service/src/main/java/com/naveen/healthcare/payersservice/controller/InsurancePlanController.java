package com.naveen.healthcare.payersservice.controller;

import com.naveen.healthcare.payersservice.dto.request.CreateInsurancePlanRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdateInsurancePlanRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdateInsurancePlanStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.InsurancePlanResponse;
import com.naveen.healthcare.payersservice.service.InsurancePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class InsurancePlanController {

    private final InsurancePlanService insurancePlanService;

    @PreAuthorize("hasRole('PAYER')")
    @PostMapping
    public ResponseEntity<InsurancePlanResponse> createPlan(
            Authentication authentication,
            @Valid @RequestBody CreateInsurancePlanRequest request
    ) {

        InsurancePlanResponse response =
                insurancePlanService.createPlan(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/me")
    public ResponseEntity<List<InsurancePlanResponse>> getMyPlans(
            Authentication authentication
    ) {

        List<InsurancePlanResponse> response =
                insurancePlanService.getMyPlans(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/me/{planId}")
    public ResponseEntity<InsurancePlanResponse> getMyPlanById(
            Authentication authentication,
            @PathVariable UUID planId
    ) {

        InsurancePlanResponse response =
                insurancePlanService.getMyPlanById(
                        authentication.getName(),
                        planId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PutMapping("/me/{planId}")
    public ResponseEntity<InsurancePlanResponse> updateMyPlan(
            Authentication authentication,
            @PathVariable UUID planId,
            @Valid @RequestBody UpdateInsurancePlanRequest request
    ) {

        InsurancePlanResponse response =
                insurancePlanService.updateMyPlan(
                        authentication.getName(),
                        planId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PAYER')")
    @PatchMapping("/me/{planId}/status")
    public ResponseEntity<InsurancePlanResponse> updateMyPlanStatus(
            Authentication authentication,
            @PathVariable UUID planId,
            @Valid
            @RequestBody
            UpdateInsurancePlanStatusRequest request
    ) {

        InsurancePlanResponse response =
                insurancePlanService.updateMyPlanStatus(
                        authentication.getName(),
                        planId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available")
    public ResponseEntity<List<InsurancePlanResponse>>
    getAvailablePlans() {

        List<InsurancePlanResponse> response =
                insurancePlanService.getAvailablePlans();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/available/{planId}")
    public ResponseEntity<InsurancePlanResponse>
    getAvailablePlanById(
            @PathVariable UUID planId
    ) {

        InsurancePlanResponse response =
                insurancePlanService
                        .getAvailablePlanById(planId);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<List<InsurancePlanResponse>>
    getAllPlansForAdmin() {

        List<InsurancePlanResponse> response =
                insurancePlanService.getAllPlansForAdmin();

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{planId}")
    public ResponseEntity<InsurancePlanResponse>
    getAnyPlanByIdForAdmin(
            @PathVariable UUID planId
    ) {

        InsurancePlanResponse response =
                insurancePlanService
                        .getAnyPlanByIdForAdmin(planId);

        return ResponseEntity.ok(response);
    }
}