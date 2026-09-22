package com.naveen.healthcare.appointmentservice.exception;

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
