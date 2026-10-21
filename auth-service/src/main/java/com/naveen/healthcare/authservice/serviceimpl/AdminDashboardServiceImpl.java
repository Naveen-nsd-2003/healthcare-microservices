package com.naveen.healthcare.authservice.serviceimpl;

import com.naveen.healthcare.authservice.dto.response.AdminAccountSummaryResponse;
import com.naveen.healthcare.authservice.dto.response.AdminDashboardResponse;
import com.naveen.healthcare.authservice.entity.Role;
import com.naveen.healthcare.authservice.entity.User;
import com.naveen.healthcare.authservice.enums.RoleType;
import com.naveen.healthcare.authservice.enums.UserStatus;
import com.naveen.healthcare.authservice.repository.UserRepository;
import com.naveen.healthcare.authservice.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl
        implements AdminDashboardService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        long totalUsers =
                userRepository.count();

        long totalAdmins =
                userRepository
                        .countByRoleRoleName(
                                RoleType.ADMIN
                        );

        long totalMembers =
                userRepository
                        .countByRoleRoleName(
                                RoleType.MEMBER
                        );

        long totalProviders =
                userRepository
                        .countByRoleRoleName(
                                RoleType.PROVIDER
                        );

        long totalPayers =
                userRepository
                        .countByRoleRoleName(
                                RoleType.PAYER
                        );

        long activeUsers =
                userRepository
                        .countByStatus(
                                UserStatus.ACTIVE
                        );

        long inactiveUsers =
                userRepository
                        .countByStatus(
                                UserStatus.INACTIVE
                        );

        long blockedUsers =
                userRepository
                        .countByStatus(
                                UserStatus.BLOCKED
                        );

        long suspendedUsers =
                userRepository
                        .countByStatus(
                                UserStatus.SUSPENDED
                        );

        long lockedAccounts =
                userRepository
                        .countByAccountLockedTrue();

        long firstLoginPendingUsers =
                userRepository
                        .countByFirstLoginTrue();

        List<AdminAccountSummaryResponse>
                recentlyCreatedAccounts =
                userRepository
                        .findTop10ByOrderByCreatedAtDesc()
                        .stream()
                        .map(this::mapToAccountSummary)
                        .toList();

        return AdminDashboardResponse
                .builder()
                .totalUsers(totalUsers)
                .totalAdmins(totalAdmins)
                .totalMembers(totalMembers)
                .totalProviders(totalProviders)
                .totalPayers(totalPayers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .blockedUsers(blockedUsers)
                .suspendedUsers(suspendedUsers)
                .lockedAccounts(lockedAccounts)
                .firstLoginPendingUsers(
                        firstLoginPendingUsers
                )
                .generatedAt(
                        LocalDateTime.now()
                )
                .recentlyCreatedAccounts(
                        recentlyCreatedAccounts
                )
                .build();
    }

    private AdminAccountSummaryResponse
    mapToAccountSummary(
            User user
    ) {

        Role role =
                user.getRole();

        RoleType roleType =
                role == null
                        ? null
                        : role.getRoleName();

        return AdminAccountSummaryResponse
                .builder()
                .id(
                        user.getId()
                )
                .username(
                        user.getUsername()
                )
                .firstName(
                        user.getFirstName()
                )
                .lastName(
                        user.getLastName()
                )
                .fullName(
                        buildFullName(user)
                )
                .email(
                        user.getEmail()
                )
                .phoneNumber(
                        user.getPhoneNumber()
                )
                .role(
                        roleType
                )
                .status(
                        user.getStatus()
                )
                .firstLogin(
                        Boolean.TRUE.equals(
                                user.getFirstLogin()
                        )
                )
                .accountLocked(
                        Boolean.TRUE.equals(
                                user.getAccountLocked()
                        )
                )
                .failedAttempts(
                        user.getFailedAttempts()
                )
                .createdAt(
                        user.getCreatedAt()
                )
                .updatedAt(
                        user.getUpdatedAt()
                )
                .build();
    }

    private String buildFullName(
            User user
    ) {

        String firstName =
                normalizeText(
                        user.getFirstName()
                );

        String lastName =
                normalizeText(
                        user.getLastName()
                );

        return (
                firstName
                        + " "
                        + lastName
        ).trim();
    }

    private String normalizeText(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value.trim();
    }
}