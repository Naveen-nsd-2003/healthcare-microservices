package com.naveen.healthcare.authservice.controller;

import com.naveen.healthcare.authservice.dto.request.RegisterRequest;
import com.naveen.healthcare.authservice.dto.request.VerifyOtpRequest;
import com.naveen.healthcare.authservice.service.AuthService;
import com.naveen.healthcare.authservice.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.naveen.healthcare.authservice.dto.request.ChangePasswordRequest;
import com.naveen.healthcare.authservice.security.CustomUserDetails;
import com.naveen.healthcare.authservice.dto.request.LoginRequest;
import com.naveen.healthcare.authservice.dto.response.LoginResponse;
import com.naveen.healthcare.authservice.dto.request.ForgotPasswordRequest;
import com.naveen.healthcare.authservice.dto.request.ResetPasswordRequest;
import com.naveen.healthcare.authservice.dto.request.CreateStaffRequest;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final OtpService otpService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        "User registered successfully. " +
                                "Please check your email for the " +
                                "temporary password and OTP."
                );
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response =
                authService.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {

        otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        return ResponseEntity.ok(
                "OTP verified successfully. " +
                        "Your account is now active."
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        authService.changePassword(
                userDetails.getUser().getEmail(),
                request
        );

        return ResponseEntity.ok("Password changed successfully");
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                "OTP sent successfully to your registered email."
        );
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                "Password reset successfully."
        );
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/payers")
    public ResponseEntity<String> createPayer(
            @Valid @RequestBody CreateStaffRequest request) {

        authService.createPayer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Payer account created successfully.");
    }



    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/providers")
    public ResponseEntity<String> createProvider(
            @Valid @RequestBody CreateStaffRequest request) {

        authService.createProvider(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Provider account created successfully.");
    }
}