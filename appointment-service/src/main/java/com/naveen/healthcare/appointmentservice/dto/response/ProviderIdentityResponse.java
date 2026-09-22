package com.naveen.healthcare.appointmentservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProviderIdentityResponse {

    private UUID providerId;

    private String adminEmail;

    private String providerOrganizationName;

    private String status;
}