package com.naveen.healthcare.claimservice.exception;

public class ClaimMedicalRecordVerificationException
        extends RuntimeException {

    public ClaimMedicalRecordVerificationException(
            String message
    ) {
        super(message);
    }
}
