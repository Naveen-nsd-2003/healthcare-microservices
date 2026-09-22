package com.naveen.healthcare.appointmentservice.controller;

import com.naveen.healthcare.appointmentservice.dto.request.BookAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.CancelAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.CompleteAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.request.ProviderCancelAppointmentRequest;
import com.naveen.healthcare.appointmentservice.dto.response.AppointmentResponse;
import com.naveen.healthcare.appointmentservice.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('MEMBER')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            Authentication authentication,
            @Valid @RequestBody BookAppointmentRequest request
    ) {

        AppointmentResponse response =
                appointmentService.bookAppointment(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me")
    public ResponseEntity<List<AppointmentResponse>>
    getMyAppointments(
            Authentication authentication
    ) {

        List<AppointmentResponse> response =
                appointmentService.getMyAppointments(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/me/{appointmentId}")
    public ResponseEntity<AppointmentResponse>
    getMyAppointmentById(
            Authentication authentication,
            @PathVariable UUID appointmentId
    ) {

        AppointmentResponse response =
                appointmentService.getMyAppointmentById(
                        authentication.getName(),
                        appointmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('MEMBER')")
    @PatchMapping("/me/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse>
    cancelMyAppointment(
            Authentication authentication,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request
    ) {

        AppointmentResponse response =
                appointmentService.cancelMyAppointment(
                        authentication.getName(),
                        appointmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider")
    public ResponseEntity<List<AppointmentResponse>>
    getProviderAppointments(
            Authentication authentication
    ) {

        List<AppointmentResponse> response =
                appointmentService.getProviderAppointments(
                        authentication.getName()
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @GetMapping("/provider/{appointmentId}")
    public ResponseEntity<AppointmentResponse>
    getProviderAppointmentById(
            Authentication authentication,
            @PathVariable UUID appointmentId
    ) {

        AppointmentResponse response =
                appointmentService.getProviderAppointmentById(
                        authentication.getName(),
                        appointmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{appointmentId}/confirm")
    public ResponseEntity<AppointmentResponse>
    confirmAppointment(
            Authentication authentication,
            @PathVariable UUID appointmentId
    ) {

        AppointmentResponse response =
                appointmentService.confirmAppointment(
                        authentication.getName(),
                        appointmentId
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{appointmentId}/cancel")
    public ResponseEntity<AppointmentResponse>
    cancelProviderAppointment(
            Authentication authentication,
            @PathVariable UUID appointmentId,
            @Valid
            @RequestBody
            ProviderCancelAppointmentRequest request
    ) {

        AppointmentResponse response =
                appointmentService.cancelProviderAppointment(
                        authentication.getName(),
                        appointmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{appointmentId}/complete")
    public ResponseEntity<AppointmentResponse>
    completeAppointment(
            Authentication authentication,
            @PathVariable UUID appointmentId,
            @Valid @RequestBody CompleteAppointmentRequest request
    ) {

        AppointmentResponse response =
                appointmentService.completeAppointment(
                        authentication.getName(),
                        appointmentId,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROVIDER')")
    @PatchMapping("/provider/{appointmentId}/no-show")
    public ResponseEntity<AppointmentResponse>
    markAppointmentAsNoShow(
            Authentication authentication,
            @PathVariable UUID appointmentId
    ) {

        AppointmentResponse response =
                appointmentService.markAppointmentAsNoShow(
                        authentication.getName(),
                        appointmentId
                );

        return ResponseEntity.ok(response);
    }
}