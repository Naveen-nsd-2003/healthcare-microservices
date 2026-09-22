package com.naveen.healthcare.payersservice.service;

import com.naveen.healthcare.payersservice.dto.request.CreateInsurancePlanRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdateInsurancePlanRequest;
import com.naveen.healthcare.payersservice.dto.request.UpdateInsurancePlanStatusRequest;
import com.naveen.healthcare.payersservice.dto.response.InsurancePlanResponse;

import java.util.List;
import java.util.UUID;

public interface InsurancePlanService {

    InsurancePlanResponse createPlan(
            String authenticatedEmail,
            CreateInsurancePlanRequest request
    );

    List<InsurancePlanResponse> getMyPlans(
            String authenticatedEmail
    );

    InsurancePlanResponse getMyPlanById(
            String authenticatedEmail,
            UUID planId
    );

    InsurancePlanResponse updateMyPlan(
            String authenticatedEmail,
            UUID planId,
            UpdateInsurancePlanRequest request
    );

    InsurancePlanResponse updateMyPlanStatus(
            String authenticatedEmail,
            UUID planId,
            UpdateInsurancePlanStatusRequest request
    );

    List<InsurancePlanResponse> getAvailablePlans();

    InsurancePlanResponse getAvailablePlanById(
            UUID planId
    );

    List<InsurancePlanResponse> getAllPlansForAdmin();

    InsurancePlanResponse getAnyPlanByIdForAdmin(
            UUID planId
    );
}