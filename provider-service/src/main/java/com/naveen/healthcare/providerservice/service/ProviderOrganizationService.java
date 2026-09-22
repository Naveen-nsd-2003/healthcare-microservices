package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.request.CreateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.ProviderOrganizationResponse;
import com.naveen.healthcare.providerservice.dto.response.ProviderIdentityResponse;
import java.util.List;
import java.util.UUID;

public interface ProviderOrganizationService {

    ProviderOrganizationResponse createProviderOrganization(
            CreateProviderOrganizationRequest request
    );

    List<ProviderOrganizationResponse> getAllProviderOrganizations();

    ProviderOrganizationResponse getProviderOrganizationById(
            UUID providerId
    );

    ProviderOrganizationResponse getMyProviderOrganization(
            String authenticatedProviderEmail
    );

    ProviderOrganizationResponse updateMyProviderOrganization(
            String authenticatedProviderEmail,
            UpdateProviderOrganizationRequest request
    );

    ProviderOrganizationResponse updateProviderStatus(
            UUID providerId,
            UpdateProviderStatusRequest request
    );

    List<ProviderOrganizationResponse> getAvailableProviders();

    ProviderOrganizationResponse getAvailableProviderById(
            UUID providerId
    );

    ProviderIdentityResponse getInternalProviderIdentity(
            String authenticatedProviderEmail
    );
}