package com.naveen.healthcare.providerservice.exception;

public class AppointmentServiceCommunicationException
        extends RuntimeException {

    public AppointmentServiceCommunicationException(
            String message
    ) {
        super(message);
    }

    public AppointmentServiceCommunicationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
