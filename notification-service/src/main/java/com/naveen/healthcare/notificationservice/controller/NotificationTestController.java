package com.naveen.healthcare.notificationservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationTestController {

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member-test")
    public Map<String, Object> memberTest(
            Authentication authentication
    ) {

        return createResponse(
                "Notification Service MEMBER endpoint is working",
                authentication
        );
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider-test")
    public Map<String, Object> providerTest(
            Authentication authentication
    ) {

        return createResponse(
                "Notification Service PROVIDER endpoint is working",
                authentication
        );
    }

    @PreAuthorize("hasRole('PAYER')")
    @GetMapping("/payer-test")
    public Map<String, Object> payerTest(
            Authentication authentication
    ) {

        return createResponse(
                "Notification Service PAYER endpoint is working",
                authentication
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-test")
    public Map<String, Object> adminTest(
            Authentication authentication
    ) {

        return createResponse(
                "Notification Service ADMIN endpoint is working",
                authentication
        );
    }

    private Map<String, Object> createResponse(
            String message,
            Authentication authentication
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                message
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