package com.naveen.healthcare.providerservice.exception;

public class MedicalRecordNotFoundException
        extends RuntimeException {

    public MedicalRecordNotFoundException(
            String message
    ) {
        super(message);
    }
}