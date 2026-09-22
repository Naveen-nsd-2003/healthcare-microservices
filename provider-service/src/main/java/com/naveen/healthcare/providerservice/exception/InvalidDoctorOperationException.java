package com.naveen.healthcare.providerservice.exception;

public class InvalidDoctorOperationException extends RuntimeException {

    public InvalidDoctorOperationException(String message) {
        super(message);
    }
}