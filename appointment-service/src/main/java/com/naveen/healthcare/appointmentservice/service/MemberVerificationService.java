package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.dto.response.MemberVerificationResponse;

public interface MemberVerificationService {

    MemberVerificationResponse verifyActiveMember(
            String authenticatedMemberEmail
    );
}
