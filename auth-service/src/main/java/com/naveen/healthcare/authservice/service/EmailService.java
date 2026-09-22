package com.naveen.healthcare.authservice.service;

public interface EmailService {

    void sendOtpEmail(
            String to,
            String firstName,
            String otp
    );

    void sendTemporaryPasswordEmail(
            String to,
            String firstName,
            String temporaryPassword
    );
}