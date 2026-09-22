package com.naveen.healthcare.appointmentservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
public class AvailabilityWindowResponse {

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDurationMinutes;
}