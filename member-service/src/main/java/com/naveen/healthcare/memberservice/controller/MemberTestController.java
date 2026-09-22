package com.naveen.healthcare.memberservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberTestController {

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/member-test")
    public Map<String, Object> memberTest(
            Authentication authentication
    ) {

        Map<String, Object> response =
                new LinkedHashMap<>();

        response.put(
                "message",
                "Member Service MEMBER endpoint is working"
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
                "Member Service ADMIN endpoint is working"
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