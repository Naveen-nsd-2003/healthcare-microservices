package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.request.CreateEnrollmentRequest;
import com.naveen.healthcare.payersservice.dto.request.ReviewEnrollmentRequest;
import com.naveen.healthcare.payersservice.dto.response.EnrollmentResponse;

import java.util.List;
import java.util.UUID;

public interface EnrollmentService {

    EnrollmentResponse createEnrollment(
            String authenticatedMemberEmail,
            CreateEnrollmentRequest request
    );

    List<EnrollmentResponse> getMyEnrollments(
            String authenticatedMemberEmail
    );

    EnrollmentResponse getMyEnrollmentById(
            String authenticatedMemberEmail,
            UUID enrollmentId
    );

    EnrollmentResponse cancelMyEnrollment(
            String authenticatedMemberEmail,
            UUID enrollmentId
    );

    List<EnrollmentResponse> getPayerEnrollments(
            String authenticatedPayerEmail
    );

    EnrollmentResponse getPayerEnrollmentById(
            String authenticatedPayerEmail,
            UUID enrollmentId
    );

    EnrollmentResponse approveEnrollment(
            String authenticatedPayerEmail,
            UUID enrollmentId,
            ReviewEnrollmentRequest request
    );

    EnrollmentResponse rejectEnrollment(
            String authenticatedPayerEmail,
            UUID enrollmentId,
            ReviewEnrollmentRequest request
    );
}