package com.naveen.healthcare.payersservice.exception;

public class InvalidPayerOperationException extends RuntimeException
{
    public InvalidPayerOperationException(
            String message
    ) {
        super(message);
    }
}
