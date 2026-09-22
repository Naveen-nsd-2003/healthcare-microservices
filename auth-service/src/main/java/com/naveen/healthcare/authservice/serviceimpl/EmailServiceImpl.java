package com.naveen.healthcare.authservice.serviceimpl;

import com.naveen.healthcare.authservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@Profile("smtp")
@RequiredArgsConstructor
public class EmailServiceImpl
        implements EmailService {


    private final JavaMailSender mailSender;


    // =====================================================
    // SEND OTP EMAIL
    // =====================================================

    @Override
    public void sendOtpEmail(
            String to,
            String firstName,
            String otp
    ) {


        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setTo(to);


        message.setSubject(
                "Healthcare System - OTP Verification"
        );


        message.setText(
                "Hello " + firstName + ",\n\n"
                        + "Your OTP is: " + otp + "\n\n"
                        + "This OTP is valid for 15 minutes.\n\n"
                        + "If you did not request this OTP, "
                        + "please ignore this message.\n\n"
                        + "Regards,\n"
                        + "Healthcare Management System"
        );


        mailSender.send(message);
    }


    // =====================================================
    // SEND TEMPORARY PASSWORD EMAIL
    // =====================================================

    @Override
    public void sendTemporaryPasswordEmail(
            String to,
            String firstName,
            String temporaryPassword
    ) {


        SimpleMailMessage message =
                new SimpleMailMessage();


        message.setTo(to);


        message.setSubject(
                "Healthcare System - Account Created"
        );


        message.setText(
                "Hello " + firstName + ",\n\n"
                        + "Your healthcare account has been created "
                        + "successfully.\n\n"
                        + "Temporary Password: "
                        + temporaryPassword + "\n\n"
                        + "Please use this temporary password for "
                        + "your first login and change it immediately "
                        + "after logging in.\n\n"
                        + "Regards,\n"
                        + "Healthcare Management System"
        );


        mailSender.send(message);
    }
}