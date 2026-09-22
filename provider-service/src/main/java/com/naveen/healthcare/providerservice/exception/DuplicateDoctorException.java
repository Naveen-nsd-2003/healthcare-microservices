package com.naveen.healthcare.providerservice.exception;

public class DuplicateDoctorException extends RuntimeException {

    public DuplicateDoctorException(String message) {
        super(message);
    }
}