package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.dto.request.CreateMedicalRecordRequest;
import com.naveen.healthcare.providerservice.dto.request.UpdateMedicalRecordRequest;
import com.naveen.healthcare.providerservice.dto.response.MedicalRecordResponse;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordService {

    MedicalRecordResponse createMedicalRecord(
            String authenticatedProviderEmail,
            CreateMedicalRecordRequest request
    );

    List<MedicalRecordResponse> getProviderMedicalRecords(
            String authenticatedProviderEmail
    );

    List<MedicalRecordResponse> getProviderMedicalRecordsByDoctor(
            String authenticatedProviderEmail,
            UUID doctorId
    );

    MedicalRecordResponse getProviderMedicalRecordById(
            String authenticatedProviderEmail,
            UUID medicalRecordId
    );

    MedicalRecordResponse updateProviderMedicalRecord(
            String authenticatedProviderEmail,
            UUID medicalRecordId,
            UpdateMedicalRecordRequest request
    );

    List<MedicalRecordResponse> getMyMedicalRecords(
            String authenticatedMemberEmail
    );

    MedicalRecordResponse getMyMedicalRecordById(
            String authenticatedMemberEmail,
            UUID medicalRecordId
    );
}