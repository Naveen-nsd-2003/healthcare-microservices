package com.naveen.healthcare.providerservice.exception;

public class InvalidMedicalRecordOperationException
        extends RuntimeException {

    public InvalidMedicalRecordOperationException(
            String message
    ) {
        super(message);
    }
}