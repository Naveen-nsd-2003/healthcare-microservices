package com.naveen.healthcare.authservice.serviceimpl;

import com.naveen.healthcare.authservice.dto.request.RegisterRequest;
import com.naveen.healthcare.authservice.entity.Role;
import com.naveen.healthcare.authservice.entity.User;
import com.naveen.healthcare.authservice.enums.UserStatus;
import com.naveen.healthcare.authservice.exception.InvalidOtpException;
import com.naveen.healthcare.authservice.exception.RoleNotFoundException;
import com.naveen.healthcare.authservice.exception.UserAlreadyExistsException;
import com.naveen.healthcare.authservice.repository.RoleRepository;
import com.naveen.healthcare.authservice.repository.UserRepository;
import com.naveen.healthcare.authservice.service.AuthService;
import com.naveen.healthcare.authservice.service.EmailService;
import com.naveen.healthcare.authservice.service.OtpService;
import com.naveen.healthcare.authservice.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naveen.healthcare.authservice.dto.request.LoginRequest;
import com.naveen.healthcare.authservice.dto.response.LoginResponse;
import com.naveen.healthcare.authservice.security.CustomUserDetails;
import com.naveen.healthcare.authservice.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import com.naveen.healthcare.authservice.dto.request.ChangePasswordRequest;
import com.naveen.healthcare.authservice.exception.UserNotFoundException;

import com.naveen.healthcare.authservice.dto.request.ForgotPasswordRequest;
import com.naveen.healthcare.authservice.dto.request.ResetPasswordRequest;

import com.naveen.healthcare.authservice.dto.request.CreateStaffRequest;
import com.naveen.healthcare.authservice.enums.RoleType;
import com.naveen.healthcare.authservice.exception.InvalidPasswordException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;


    @Override
    @Transactional
    public void register(RegisterRequest request) {

        // =====================================================
        // 1. CHECK USERNAME
        // =====================================================

        if (userRepository.existsByUsername(
                request.getUsername())) {

            throw new UserAlreadyExistsException(
                    "Username already exists"
            );
        }


        // =====================================================
        // 2. CHECK EMAIL
        // =====================================================

        if (userRepository.existsByEmail(
                request.getEmail())) {

            throw new UserAlreadyExistsException(
                    "Email already exists"
            );
        }


        // =====================================================
        // 3. FIND MEMBER ROLE
        // =====================================================

        Role role = roleRepository
                .findByRoleName(
                        com.naveen.healthcare.authservice.enums.RoleType.MEMBER
                )
                .orElseThrow(() ->
                        new RoleNotFoundException(
                                "MEMBER role not found"
                        )
                );


        // =====================================================
        // 4. GENERATE TEMPORARY PASSWORD
        // =====================================================

        String temporaryPassword =
                PasswordGenerator.generatePassword();


        // =====================================================
        // 5. CREATE USER
        // =====================================================

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(
                        passwordEncoder.encode(
                                temporaryPassword
                        )
                )
                .role(role)
                .status(UserStatus.INACTIVE)
                .firstLogin(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build();


        // =====================================================
        // 6. SAVE USER
        // =====================================================

        userRepository.save(user);


        // =====================================================
        // 7. SEND TEMPORARY PASSWORD
        // =====================================================

        emailService.sendTemporaryPasswordEmail(
                user.getEmail(),
                user.getFirstName(),
                temporaryPassword
        );


        // =====================================================
        // 8. GENERATE AND SEND OTP
        // =====================================================

        otpService.generateAndSendOtp(
                user.getEmail()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String token =
                jwtService.generateToken(userDetails);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .email(userDetails.getUser().getEmail())
                .role(
                        userDetails.getUser()
                                .getRole()
                                .getRoleName()
                                .name()
                )
                .firstLogin(
                        userDetails.getUser().getFirstLogin()
                )
                .build();
    }

    @Override
    @Transactional
    public void changePassword(
            String email,
            ChangePasswordRequest request
    ) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );

        // Verify temporary/current password
        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword()
        )) {

            throw new InvalidPasswordException(
                    "Current password is incorrect"
            );
        }

        System.out.println("Email = " + email);
        System.out.println(
                "Password Match = " +
                        passwordEncoder.matches(
                                request.getCurrentPassword(),
                                user.getPassword()
                        )
        );
        // Prevent same password
        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword()
        )) {

            throw new InvalidPasswordException(
                    "New password must be different from current password"
            );
        }

        // Encode new password
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        // First login completed
        user.setFirstLogin(false);

        userRepository.save(user);
    }

    // =====================================================
// FORGOT PASSWORD
// =====================================================

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + request.getEmail()
                        )
                );

        otpService.generateAndSendOtp(user.getEmail());
    }


// =====================================================
// RESET PASSWORD
// =====================================================

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {

        // 1. Verify OTP
        otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        // 2. Find User
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + request.getEmail()
                        )
                );

        // 3. Encode New Password
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        // 4. Reset first login flag
        user.setFirstLogin(false);

        userRepository.save(user);
    }

    // =====================================================
// CREATE PAYER
// =====================================================

    @Override
    @Transactional
    public void createPayer(CreateStaffRequest request) {

        createStaffUser(request, RoleType.PAYER);
    }


// =====================================================
// CREATE PROVIDER
// =====================================================

    @Override
    @Transactional
    public void createProvider(CreateStaffRequest request) {

        createStaffUser(request, RoleType.PROVIDER);
    }


// =====================================================
// COMMON STAFF CREATION
// =====================================================

    private void createStaffUser(
            CreateStaffRequest request,
            RoleType roleType
    ) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        Role role = roleRepository.findByRoleName(roleType)
                .orElseThrow(() ->
                        new RoleNotFoundException(roleType + " role not found")
                );

        String tempPassword = PasswordGenerator.generatePassword();

        User user = User.builder()
                .username(request.getUsername())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(tempPassword))
                .role(role)
                .status(UserStatus.ACTIVE)
                .firstLogin(true)
                .accountLocked(false)
                .failedAttempts(0)
                .build();

        userRepository.save(user);

        emailService.sendTemporaryPasswordEmail(
                user.getEmail(),
                user.getFirstName(),
                tempPassword
        );
    }
}