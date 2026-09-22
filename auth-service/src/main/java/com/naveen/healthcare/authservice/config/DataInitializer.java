package com.naveen.healthcare.authservice.config;

import com.naveen.healthcare.authservice.entity.Role;
import com.naveen.healthcare.authservice.enums.RoleType;
import com.naveen.healthcare.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        createRoleIfNotExists(
                RoleType.ADMIN,
                "System administrator"
        );

        createRoleIfNotExists(
                RoleType.PAYER,
                "Healthcare payer user"
        );

        createRoleIfNotExists(
                RoleType.PROVIDER,
                "Healthcare provider user"
        );

        createRoleIfNotExists(
                RoleType.MEMBER,
                "Healthcare member user"
        );
    }

    private void createRoleIfNotExists(
            RoleType roleType,
            String description
    ) {

        if (!roleRepository.existsByRoleName(roleType)) {

            Role role = Role.builder()
                    .roleName(roleType)
                    .description(description)
                    .build();

            roleRepository.save(role);
        }
    }
}