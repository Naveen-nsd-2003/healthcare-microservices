package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.entity.InsuranceEnrollment;
import com.naveen.healthcare.payersservice.entity.InsurancePolicy;

public interface InsuranceEventPublisher {

    void publishEnrollmentSubmitted(
            InsuranceEnrollment enrollment
    );

    void publishEnrollmentApproved(
            InsuranceEnrollment enrollment
    );

    void publishEnrollmentRejected(
            InsuranceEnrollment enrollment
    );

    void publishEnrollmentCancelled(
            InsuranceEnrollment enrollment
    );

    void publishPolicyActivated(
            InsurancePolicy policy
    );

    void publishPolicySuspended(
            InsurancePolicy policy
    );

    void publishPolicyCancelled(
            InsurancePolicy policy
    );

    void publishPolicyExpired(
            InsurancePolicy policy
    );
}