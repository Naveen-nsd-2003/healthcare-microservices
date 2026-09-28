package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.entity.Appointment;

public interface AppointmentEventPublisher {

    void publishAppointmentBooked(
            Appointment appointment
    );

    void publishAppointmentConfirmed(
            Appointment appointment
    );

    void publishAppointmentCompleted(
            Appointment appointment
    );

    void publishAppointmentCancelled(
            Appointment appointment
    );
}