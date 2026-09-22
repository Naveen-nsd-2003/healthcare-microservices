package com.naveen.healthcare.providerservice.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateMedicalRecordRequest {

    @NotBlank(message = "Chief complaint is required")
    @Size(
            max = 1000,
            message = "Chief complaint must not exceed 1000 characters"
    )
    private String chiefComplaint;

    @NotBlank(message = "Diagnosis is required")
    @Size(
            max = 2000,
            message = "Diagnosis must not exceed 2000 characters"
    )
    private String diagnosis;

    @Size(
            max = 3000,
            message = "Clinical observations must not exceed 3000 characters"
    )
    private String clinicalObservations;

    @NotBlank(message = "Treatment plan is required")
    @Size(
            max = 3000,
            message = "Treatment plan must not exceed 3000 characters"
    )
    private String treatmentPlan;

    @Size(
            max = 3000,
            message = "Prescription notes must not exceed 3000 characters"
    )
    private String prescriptionNotes;

    @FutureOrPresent(
            message = "Follow-up date cannot be in the past"
    )
    private LocalDate followUpDate;
}