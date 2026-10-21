import {
    AdminAccountSummary
  } from './admin-account-summary';
  
  export interface AdminDashboard {
  
    totalUsers: number;
  
    totalAdmins: number;
  
    totalMembers: number;
  
    totalProviders: number;
  
    totalPayers: number;
  
    activeUsers: number;
  
    inactiveUsers: number;
  
    blockedUsers: number;
  
    suspendedUsers: number;
  
    lockedAccounts: number;
  
    firstLoginPendingUsers: number;
  
    generatedAt: string;
  
    recentlyCreatedAccounts:
      AdminAccountSummary[];
  }