package com.naveen.healthcare.authservice.controller;

import com.naveen.healthcare.authservice.dto.response.AdminDashboardResponse;
import com.naveen.healthcare.authservice.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminDashboardService
            adminDashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse>
    getDashboard() {

        AdminDashboardResponse response =
                adminDashboardService
                        .getDashboard();

        return ResponseEntity.ok(response);
    }
}