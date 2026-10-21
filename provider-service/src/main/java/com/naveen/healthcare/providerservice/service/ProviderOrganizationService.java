package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.request.ApproveProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.CreateMyProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.CreateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.RejectProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderOrganizationRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateProviderStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.ProviderIdentityResponse;
import com.naveen.healthcare.providerservice.dto.response.ProviderOrganizationResponse;

import java.util.List;
import java.util.UUID;

public interface ProviderOrganizationService {

    ProviderOrganizationResponse createProviderOrganization(
            CreateProviderOrganizationRequest request
    );

    ProviderOrganizationResponse createMyProviderOrganization(
            String authenticatedProviderEmail,
            CreateMyProviderOrganizationRequest request
    );

    List<ProviderOrganizationResponse>
    getAllProviderOrganizations();

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

    ProviderOrganizationResponse approveProviderOrganization(
            UUID providerId,
            String authenticatedAdminEmail,
            ApproveProviderOrganizationRequest request
    );

    ProviderOrganizationResponse rejectProviderOrganization(
            UUID providerId,
            String authenticatedAdminEmail,
            RejectProviderOrganizationRequest request
    );

    ProviderOrganizationResponse updateProviderStatus(
            UUID providerId,
            UpdateProviderStatusRequest request
    );

    List<ProviderOrganizationResponse>
    getAvailableProviders();

    ProviderOrganizationResponse getAvailableProviderById(
            UUID providerId
    );

    ProviderIdentityResponse getInternalProviderIdentity(
            String authenticatedProviderEmail
    );
}