package com.naveen.healthcare.payersservice.exception;

public class InvalidPolicyOperationException extends RuntimeException {

    public InvalidPolicyOperationException(String message) {
        super(message);
    }
}