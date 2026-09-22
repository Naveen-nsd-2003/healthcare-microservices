package com.naveen.healthcare.providerservice.exception;

public class InvalidDoctorAvailabilityException
        extends RuntimeException {

    public InvalidDoctorAvailabilityException(
            String message
    ) {
        super(message);
    }
}
