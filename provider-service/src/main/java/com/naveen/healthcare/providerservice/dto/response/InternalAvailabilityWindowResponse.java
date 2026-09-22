package com.naveen.healthcare.providerservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class InternalAvailabilityWindowResponse {

    private LocalTime startTime;

    private LocalTime endTime;

    private Integer slotDurationMinutes;
}