package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.request.UpdatePolicyStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PolicyResponse;
import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;

import java.util.List;
import java.util.UUID;

public interface PolicyService {

    PolicyResponse issuePolicyForApprovedEnrollment(
            InsuranceEnrollment enrollment
    );

    List<PolicyResponse> getMyPolicies(
            String authenticatedMemberEmail
    );

    PolicyResponse getMyPolicyById(
            String authenticatedMemberEmail,
            UUID policyId
    );

    List<PolicyResponse> getPayerPolicies(
            String authenticatedPayerEmail
    );

    PolicyResponse getPayerPolicyById(
            String authenticatedPayerEmail,
            UUID policyId
    );

    PolicyResponse updatePolicyStatus(
            String authenticatedPayerEmail,
            UUID policyId,
            UpdatePolicyStatusRequest request
    );
}