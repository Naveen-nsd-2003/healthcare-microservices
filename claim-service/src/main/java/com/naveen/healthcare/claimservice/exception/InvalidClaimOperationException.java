package com.naveen.healthcare.claimservice.exception;

public class InvalidClaimOperationException
        extends RuntimeException {

    public InvalidClaimOperationException(String message) {
        super(message);
    }
}