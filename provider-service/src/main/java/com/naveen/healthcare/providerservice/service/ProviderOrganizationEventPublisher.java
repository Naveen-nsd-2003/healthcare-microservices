package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.entity.ProviderOrganization;

public interface ProviderOrganizationEventPublisher {

    void publishOrganizationSubmitted(
            ProviderOrganization providerOrganization
    );

    void publishOrganizationApproved(
            ProviderOrganization providerOrganization
    );

    void publishOrganizationRejected(
            ProviderOrganization providerOrganization
    );

    void publishOrganizationSuspended(
            ProviderOrganization providerOrganization
    );

    void publishOrganizationReactivated(
            ProviderOrganization providerOrganization
    );

    void publishOrganizationDeactivated(
            ProviderOrganization providerOrganization
    );
}