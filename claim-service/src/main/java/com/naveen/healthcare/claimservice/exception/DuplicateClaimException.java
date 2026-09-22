package com.naveen.healthcare.claimservice.exception;

public class DuplicateClaimException extends RuntimeException {

    public DuplicateClaimException(String message) {
        super(message);
    }
}