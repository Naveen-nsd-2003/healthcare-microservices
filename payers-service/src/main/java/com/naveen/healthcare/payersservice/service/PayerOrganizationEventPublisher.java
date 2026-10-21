package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.entity.PayerOrganization;

public interface PayerOrganizationEventPublisher {

    void publishOrganizationSubmitted(
            PayerOrganization payerOrganization
    );

    void publishOrganizationApproved(
            PayerOrganization payerOrganization
    );

    void publishOrganizationRejected(
            PayerOrganization payerOrganization
    );

    void publishOrganizationSuspended(
            PayerOrganization payerOrganization
    );

    void publishOrganizationReactivated(
            PayerOrganization payerOrganization
    );

    void publishOrganizationDeactivated(
            PayerOrganization payerOrganization
    );
}