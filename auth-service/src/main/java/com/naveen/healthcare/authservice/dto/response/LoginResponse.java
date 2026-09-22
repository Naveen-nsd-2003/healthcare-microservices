package com.naveen.healthcare.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private String email;

    private String role;

    private boolean firstLogin;
}