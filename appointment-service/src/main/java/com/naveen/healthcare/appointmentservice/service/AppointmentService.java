package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.dto.request.BookAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.CancelAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.CompleteAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.ProviderCancelAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.response.AppointmentResponse;

import java.util.List;
import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse bookAppointment(
            String authenticatedMemberEmail,
            BookAppointmentRequest request
    );

    List<AppointmentResponse> getMyAppointments(
            String authenticatedMemberEmail
    );

    AppointmentResponse getMyAppointmentById(
            String authenticatedMemberEmail,
            UUID appointmentId
    );

    AppointmentResponse cancelMyAppointment(
            String authenticatedMemberEmail,
            UUID appointmentId,
            CancelAppointmentRequest request
    );

    List<AppointmentResponse> getProviderAppointments(
            String authenticatedProviderEmail
    );

    AppointmentResponse getProviderAppointmentById(
            String authenticatedProviderEmail,
            UUID appointmentId
    );

    AppointmentResponse confirmAppointment(
            String authenticatedProviderEmail,
            UUID appointmentId
    );

    AppointmentResponse cancelProviderAppointment(
            String authenticatedProviderEmail,
            UUID appointmentId,
            ProviderCancelAppointmentRequest request
    );

    AppointmentResponse completeAppointment(
            String authenticatedProviderEmail,
            UUID appointmentId,
            CompleteAppointmentRequest request
    );

    AppointmentResponse markAppointmentAsNoShow(
            String authenticatedProviderEmail,
            UUID appointmentId
    );
}