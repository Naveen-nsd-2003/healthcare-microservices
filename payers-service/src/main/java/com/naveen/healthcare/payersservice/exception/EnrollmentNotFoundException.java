package com.naveen.healthcare.payersservice.exception;

public class EnrollmentNotFoundException
        extends RuntimeException {

    public EnrollmentNotFoundException(
            String message
    ) {
        super(message);
    }
}