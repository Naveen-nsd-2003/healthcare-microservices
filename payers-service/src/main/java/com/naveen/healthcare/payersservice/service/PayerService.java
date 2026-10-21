package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.request.ApprovePayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.CreateMyPayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.CreatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.RejectPayerOrganizationRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PayerResponse;

import java.util.List;
import java.util.UUID;

public interface PayerService {

    PayerResponse createPayer(
            CreatePayerRequest request
    );

    PayerResponse createMyOrganization(
            String authenticatedEmail,
            CreateMyPayerOrganizationRequest request
    );

    List<PayerResponse> getAllPayers();

    PayerResponse getPayerById(
            UUID payerId
    );

    PayerResponse getMyOrganization(
            String authenticatedEmail
    );

    PayerResponse updateMyOrganization(
            String authenticatedEmail,
            UpdatePayerRequest request
    );

    PayerResponse approvePayerOrganization(
            UUID payerId,
            String authenticatedAdminEmail,
            ApprovePayerOrganizationRequest request
    );

    PayerResponse rejectPayerOrganization(
            UUID payerId,
            String authenticatedAdminEmail,
            RejectPayerOrganizationRequest request
    );

    PayerResponse updatePayerStatus(
            UUID payerId,
            UpdatePayerStatusRequest request
    );
}