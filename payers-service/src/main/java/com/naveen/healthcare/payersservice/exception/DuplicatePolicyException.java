package com.naveen.healthcare.payersservice.exception;

public class DuplicatePolicyException extends RuntimeException {

    public DuplicatePolicyException(String message) {
        super(message);
    }
}