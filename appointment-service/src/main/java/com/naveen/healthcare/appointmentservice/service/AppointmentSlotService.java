package com.naveen.healthcare.appointmentservice.service;

import com.naveen.healthcare.appointmentservice.dto.response.AvailableSlotsResponse;

import java.time.LocalDate;
import java.util.UUID;

public interface AppointmentSlotService {

    AvailableSlotsResponse getAvailableSlots(
            UUID doctorId,
            LocalDate appointmentDate
    );
}