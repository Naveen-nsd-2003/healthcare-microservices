package com.naveen.healthcare.providerservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/providers")
public class ProviderTestController {

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider-test")
    public Map<String, Object> providerTest(
            Authentication authentication
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Provider Service PROVIDER endpoint is working"
        );

        response.put(
                "email",
                authentication.getName()
        );

        response.put(
                "authorities",
                authentication.getAuthorities()
        );

        return response;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-test")
    public Map<String, Object> adminTest(
            Authentication authentication
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Provider Service ADMIN endpoint is working"
        );

        response.put(
                "email",
                authentication.getName()
        );

        response.put(
                "authorities",
                authentication.getAuthorities()
        );

        return response;
    }
}