package com.naveen.healthcare.appointmentservice.exception;

public class InvalidAppointmentOperationException
        extends RuntimeException {

    public InvalidAppointmentOperationException(String message) {
        super(message);
    }
}