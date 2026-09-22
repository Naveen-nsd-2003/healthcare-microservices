package com.naveen.healthcare.providerservice.exception;

public class InvalidProviderOperationException extends RuntimeException {

    public InvalidProviderOperationException(String message) {
        super(message);
    }
}