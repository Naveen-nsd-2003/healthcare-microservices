package com.naveen.healthcare.providerservice.exception;

public class DuplicateDepartmentException extends RuntimeException {

    public DuplicateDepartmentException(String message) {
        super(message);
    }
}