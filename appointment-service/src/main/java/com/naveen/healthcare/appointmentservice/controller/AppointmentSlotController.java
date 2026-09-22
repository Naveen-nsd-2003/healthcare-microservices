package com.naveen.healthcare.appointmentservice.controller;

import com.naveen.healthcare.appointmentservice.dto.response.AvailableSlotsResponse;
import com.naveen.healthcare.appointmentservice.service.AppointmentSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments/slots")
@RequiredArgsConstructor
public class AppointmentSlotController {

    private final AppointmentSlotService appointmentSlotService;

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<AvailableSlotsResponse>
    getAvailableSlots(
            @PathVariable UUID doctorId,

            @RequestParam("date")
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate appointmentDate
    ) {

        AvailableSlotsResponse response =
                appointmentSlotService.getAvailableSlots(
                        doctorId,
                        appointmentDate
                );

        return ResponseEntity.ok(response);
    }
}