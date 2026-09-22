package com.naveen.healthcare.authservice.controller;

import com.naveen.healthcare.authservice.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ProfileController {

    @GetMapping("/profile")
    public Map<String, Object> profile(
            @AuthenticationPrincipal
            CustomUserDetails userDetails) {

        return Map.of(
                "email", userDetails.getUser().getEmail(),
                "role", userDetails.getUser().getRole().getRoleName(),
                "status", userDetails.getUser().getStatus()
        );
    }
}