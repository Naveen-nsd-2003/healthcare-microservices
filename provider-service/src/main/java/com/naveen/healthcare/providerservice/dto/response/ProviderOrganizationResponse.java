package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import com.naveen.healthcare.providerservice.enums.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ProviderOrganizationResponse {

    private UUID id;

    private String organizationName;

    private ProviderType providerType;

    private String registrationNumber;

    private String licenseNumber;

    private String adminEmail;

    private String contactNumber;

    private String website;

    private String addressLine;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    private Boolean emergencyServicesAvailable;

    private ProviderStatus status;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private String reviewedByEmail;

    private String reviewRemarks;

    private String rejectionReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}