package com.naveen.healthcare.providerservice.exception;

public class DuplicateDoctorAvailabilityException
        extends RuntimeException {

    public DuplicateDoctorAvailabilityException(
            String message
    ) {
        super(message);
    }
}
