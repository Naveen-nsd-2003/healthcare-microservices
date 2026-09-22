package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.response.MemberVerificationResponse;

public interface MemberVerificationService {

    MemberVerificationResponse verifyActiveMember(
            String authenticatedEmail
    );
}