package com.naveen.healthcare.authservice.config;

import com.naveen.healthcare.authservice.entity.Role;
import com.naveen.healthcare.authservice.entity.User;
import com.naveen.healthcare.authservice.enums.RoleType;
import com.naveen.healthcare.authservice.enums.UserStatus;
import com.naveen.healthcare.authservice.repository.RoleRepository;
import com.naveen.healthcare.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;


    @Override
    public void run(String... args) {

        // =====================================================
        // 1. MAKE SURE ADMIN ROLE EXISTS
        // =====================================================

        Role adminRole = roleRepository
                .findByRoleName(RoleType.ADMIN)
                .orElseGet(() ->
                        roleRepository.save(
                                Role.builder()
                                        .roleName(RoleType.ADMIN)
                                        .description(
                                                "System Administrator"
                                        )
                                        .build()
                        )
                );


        // =====================================================
        // 2. CHECK WHETHER ADMIN USER ALREADY EXISTS
        // =====================================================

        if (userRepository.existsByUsername(adminUsername)) {

            System.out.println(
                    "ADMIN user already exists."
            );

            return;
        }


        if (userRepository.existsByEmail(adminEmail)) {

            System.out.println(
                    "ADMIN email already exists."
            );

            return;
        }


        // =====================================================
        // 3. CREATE ADMIN
        // =====================================================

        User admin = User.builder()
                .username(adminUsername)
                .firstName("System")
                .lastName("Administrator")
                .email(adminEmail)
                .password(
                        passwordEncoder.encode(
                                adminPassword
                        )
                )
                .role(adminRole)
                .status(UserStatus.ACTIVE)
                .firstLogin(false)
                .accountLocked(false)
                .failedAttempts(0)
                .build();


        // =====================================================
        // 4. SAVE ADMIN
        // =====================================================

        userRepository.save(admin);

        System.out.println(
                "================================================="
        );

        System.out.println(
                "Default ADMIN user created successfully."
        );

        System.out.println(
                "Username : " + adminUsername
        );

        System.out.println(
                "Email    : " + adminEmail
        );

        System.out.println(
                "================================================="
        );
    }
}