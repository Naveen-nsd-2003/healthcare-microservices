package com.naveen.healthcare.claimservice.exception;

public class ClaimNotFoundException extends RuntimeException {

    public ClaimNotFoundException(String message) {
        super(message);

    }
}