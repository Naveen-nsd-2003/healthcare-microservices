package com.naveen.healthcare.authservice.dto.response;

import com.naveen.healthcare.authservice.enums.RoleType;
import com.naveen.healthcare.authservice.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class AdminAccountSummaryResponse {

    private UUID id;

    private String username;

    private String firstName;

    private String lastName;

    private String fullName;

    private String email;

    private String phoneNumber;

    private RoleType role;

    private UserStatus status;

    private Boolean firstLogin;

    private Boolean accountLocked;

    private Integer failedAttempts;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}