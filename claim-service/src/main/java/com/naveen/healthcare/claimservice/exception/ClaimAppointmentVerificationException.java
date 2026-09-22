package com.naveen.healthcare.claimservice.exception;

public class ClaimAppointmentVerificationException
        extends RuntimeException {

    public ClaimAppointmentVerificationException(
            String message
    ) {
        super(message);
    }
}
