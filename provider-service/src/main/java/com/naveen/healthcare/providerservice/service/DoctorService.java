package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.request.CreateDoctorRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorStatusRequest;
import com.naveen.healthcare.providerservice.dto.response.DoctorResponse;

import java.util.List;
import java.util.UUID;

public interface DoctorService {

    DoctorResponse createDoctor(
            String authenticatedProviderEmail,
            CreateDoctorRequest request
    );

    List<DoctorResponse> getMyDoctors(
            String authenticatedProviderEmail
    );

    DoctorResponse getMyDoctorById(
            String authenticatedProviderEmail,
            UUID doctorId
    );

    DoctorResponse updateMyDoctor(
            String authenticatedProviderEmail,
            UUID doctorId,
            UpdateDoctorRequest request
    );

    DoctorResponse updateMyDoctorStatus(
            String authenticatedProviderEmail,
            UUID doctorId,
            UpdateDoctorStatusRequest request
    );

    List<DoctorResponse> getAvailableDoctors();

    DoctorResponse getAvailableDoctorById(
            UUID doctorId
    );

    List<DoctorResponse> getAvailableDoctorsByProvider(
            UUID providerId
    );

    List<DoctorResponse> getAvailableDoctorsByDepartment(
            UUID departmentId
    );
}