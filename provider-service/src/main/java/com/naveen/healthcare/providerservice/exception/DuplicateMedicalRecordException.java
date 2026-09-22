package com.naveen.healthcare.providerservice.exception;

public class DuplicateMedicalRecordException
        extends RuntimeException {

    public DuplicateMedicalRecordException(
            String message
    ) {
        super(message);
    }
}