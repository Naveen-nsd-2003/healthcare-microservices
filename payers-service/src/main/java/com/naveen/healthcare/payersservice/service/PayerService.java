package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.request.CreatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdatePayerStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.PayerResponse;

import java.util.List;
import java.util.UUID;


public interface PayerService {


    PayerResponse createPayer(
            CreatePayerRequest request
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


    PayerResponse updatePayerStatus(
            UUID payerId,
            UpdatePayerStatusRequest request
    );
}