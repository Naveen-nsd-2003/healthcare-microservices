package com.naveen.healthcare.appointmentservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class AppointmentSlotResponse {

    private LocalTime startTime;

    private LocalTime endTime;
}