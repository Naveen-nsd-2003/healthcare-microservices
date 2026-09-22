package com.naveen.healthcare.authservice.serviceimpl;

import com.naveen.healthcare.authservice.entity.Otp;
import com.naveen.healthcare.authservice.entity.User;
import com.naveen.healthcare.authservice.enums.UserStatus;
import com.naveen.healthcare.authservice.exception.InvalidOtpException;
import com.naveen.healthcare.authservice.exception.OtpExpiredException;
import com.naveen.healthcare.authservice.exception.UserNotFoundException;
import com.naveen.healthcare.authservice.repository.OtpRepository;
import com.naveen.healthcare.authservice.repository.UserRepository;
import com.naveen.healthcare.authservice.service.EmailService;
import com.naveen.healthcare.authservice.service.OtpService;
import com.naveen.healthcare.authservice.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // =========================================================
    // GENERATE AND SEND OTP
    // =========================================================

    @Override
    @Transactional
    public void generateAndSendOtp(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // Remove old OTP
        otpRepository.deleteByEmail(email);

        // Generate new OTP
        String otp = OtpGenerator.generateOtp();

        Otp otpEntity = Otp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(15))
                .verified(false)
                .build();

        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(
                user.getEmail(),
                user.getFirstName(),
                otp
        );
    }

    // =========================================================
    // VERIFY OTP + ACTIVATE ACCOUNT
    // =========================================================

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otp) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );

        Otp otpEntity = otpRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() ->
                        new InvalidOtpException("OTP not found")
                );

        // Already verified
        if (Boolean.TRUE.equals(otpEntity.getVerified())) {
            throw new InvalidOtpException("OTP already verified");
        }

        // Expired
        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("OTP expired");
        }

        // Invalid OTP
        if (!otpEntity.getOtp().equals(otp)) {
            throw new InvalidOtpException("Invalid OTP");
        }

        // Mark OTP verified
        otpEntity.setVerified(true);
        otpRepository.save(otpEntity);

        // Activate user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        return true;
    }
}