package com.naveen.healthcare.payersservice.exception;

public class InvalidEnrollmentOperationException
        extends RuntimeException {

    public InvalidEnrollmentOperationException(
            String message
    ) {
        super(message);
    }
}