package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.request.CreateDepartmentRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDepartmentRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDepartmentStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.DepartmentResponse;

import java.util.List;
import java.util.UUID;

public interface DepartmentService {

    DepartmentResponse createDepartment(
            String authenticatedProviderEmail,
            CreateDepartmentRequest request
    );

    List<DepartmentResponse> getMyDepartments(
            String authenticatedProviderEmail
    );

    DepartmentResponse getMyDepartmentById(
            String authenticatedProviderEmail,
            UUID departmentId
    );

    DepartmentResponse updateMyDepartment(
            String authenticatedProviderEmail,
            UUID departmentId,
            UpdateDepartmentRequest request
    );

    DepartmentResponse updateMyDepartmentStatus(
            String authenticatedProviderEmail,
            UUID departmentId,
            UpdateDepartmentStatusRequest request
    );

    List<DepartmentResponse> getAvailableDepartments(
            UUID providerId
    );

    DepartmentResponse getAvailableDepartmentById(
            UUID departmentId
    );
}