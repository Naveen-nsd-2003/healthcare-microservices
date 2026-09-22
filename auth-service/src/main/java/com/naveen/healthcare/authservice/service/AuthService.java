package com.naveen.healthcare.authservice.service;

import com.naveen.healthcare.authservice.dto.request.*;
import com.naveen.healthcare.authservice.dto.response.LoginResponse;

public interface AuthService {

    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void changePassword(
            String email,
            ChangePasswordRequest request
    );


    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void createPayer(CreateStaffRequest request);

    void createProvider(CreateStaffRequest request);
}