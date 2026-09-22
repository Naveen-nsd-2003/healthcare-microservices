package com.naveen.healthcare.payersservice.exception;

public class InvalidPolicyClaimException extends RuntimeException {

    public InvalidPolicyClaimException(String message) {
        super(message);
    }
}