package com.naveen.healthcare.providerservice.exception;

public class DoctorAvailabilityNotFoundException
        extends RuntimeException {

    public DoctorAvailabilityNotFoundException(
            String message
    ) {
        super(message);
    }
}
