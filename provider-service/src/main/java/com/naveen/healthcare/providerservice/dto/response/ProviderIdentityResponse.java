package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ProviderIdentityResponse {

    private UUID providerId;

    private String adminEmail;

    private String providerOrganizationName;

    private ProviderStatus status;
}