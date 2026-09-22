package com.naveen.healthcare.payersservice.exception;

public class DuplicateEnrollmentException
        extends RuntimeException {

    public DuplicateEnrollmentException(
            String message
    ) {
        super(message);
    }
}