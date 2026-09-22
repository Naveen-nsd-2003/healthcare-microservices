package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.request.CreateDoctorAvailabilityRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateAvailabilityStatusRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateDoctorAvailabilityRequest;
import com.naveen.healthcare.providerservice.dto.response.DoctorAvailabilityResponse;

import java.util.List;
import java.util.UUID;

public interface DoctorAvailabilityService {

    DoctorAvailabilityResponse createAvailability(
            String authenticatedProviderEmail,
            CreateDoctorAvailabilityRequest request
    );

    List<DoctorAvailabilityResponse> getMyAvailabilities(
            String authenticatedProviderEmail
    );

    List<DoctorAvailabilityResponse> getMyDoctorAvailabilities(
            String authenticatedProviderEmail,
            UUID doctorId
    );

    DoctorAvailabilityResponse getMyAvailabilityById(
            String authenticatedProviderEmail,
            UUID availabilityId
    );

    DoctorAvailabilityResponse updateMyAvailability(
            String authenticatedProviderEmail,
            UUID availabilityId,
            UpdateDoctorAvailabilityRequest request
    );

    DoctorAvailabilityResponse updateMyAvailabilityStatus(
            String authenticatedProviderEmail,
            UUID availabilityId,
            UpdateAvailabilityStatusRequest request
    );

    void deleteMyAvailability(
            String authenticatedProviderEmail,
            UUID availabilityId
    );

    List<DoctorAvailabilityResponse> getAvailableDoctorSchedules(
            UUID doctorId
    );
}