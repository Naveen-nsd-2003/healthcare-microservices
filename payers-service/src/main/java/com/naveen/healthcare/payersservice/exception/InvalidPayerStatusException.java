package com.naveen.healthcare.payersservice.exception;

public class InvalidPayerStatusException
        extends RuntimeException {

    public InvalidPayerStatusException(
            String message
    ) {

        super(message);
    }
}
