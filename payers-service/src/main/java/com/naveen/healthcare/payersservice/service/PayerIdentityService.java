package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.response.PayerIdentityResponse;

public interface PayerIdentityService {

    PayerIdentityResponse getAuthenticatedPayerIdentity(
            String authenticatedPayerEmail
    );
}