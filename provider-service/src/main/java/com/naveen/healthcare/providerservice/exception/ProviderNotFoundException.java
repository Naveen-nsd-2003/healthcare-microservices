package com.naveen.healthcare.providerservice.exception;

public class ProviderNotFoundException extends RuntimeException {

    public ProviderNotFoundException(String message) {
        super(message);
    }
}