package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.dto.response.MedicalRecordAppointmentVerificationResponse;

import java.util.UUID;

public interface AppointmentMedicalRecordVerificationService {

    MedicalRecordAppointmentVerificationResponse
    verifyCompletedAppointmentForMedicalRecord(
            String authenticatedProviderEmail,
            UUID appointmentId
    );
}