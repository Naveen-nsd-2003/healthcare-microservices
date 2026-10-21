package com.naveen.healthcare.providerservice.service;

import com.naveen.healthcare.providerservice.entity.MedicalRecord;

public interface MedicalRecordEventPublisher {

    void publishMedicalRecordCreated(
            MedicalRecord medicalRecord
    );

    void publishMedicalRecordAmended(
            MedicalRecord medicalRecord
    );
}