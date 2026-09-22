package com.naveen.healthcare.notificationservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MarkAllReadResponse {

    private String message;

    private int updatedCount;

    private LocalDateTime readAt;
}