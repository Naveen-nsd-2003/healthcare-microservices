package com.naveen.healthcare.providerservice.exception;

public class DuplicateProviderException extends RuntimeException {

    public DuplicateProviderException(String message) {
        super(message);
    }
}