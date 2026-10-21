package com.naveen.healthcare.authservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;

    private long totalAdmins;

    private long totalMembers;

    private long totalProviders;

    private long totalPayers;

    private long activeUsers;

    private long inactiveUsers;

    private long blockedUsers;

    private long suspendedUsers;

    private long lockedAccounts;

    private long firstLoginPendingUsers;

    private LocalDateTime generatedAt;

    private List<AdminAccountSummaryResponse>
            recentlyCreatedAccounts;
}