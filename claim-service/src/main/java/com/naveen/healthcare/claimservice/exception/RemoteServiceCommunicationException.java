package com.naveen.healthcare.claimservice.exception;

public class RemoteServiceCommunicationException
        extends RuntimeException {

    public RemoteServiceCommunicationException(
            String message
    ) {
        super(message);
    }

    public RemoteServiceCommunicationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
