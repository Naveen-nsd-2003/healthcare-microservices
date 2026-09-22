package com.naveen.healthcare.notificationservice.service;

import com.naveen.healthcare.notificationservice.dto.event.HealthcareEvent;

public interface NotificationEventService {

    void processEvent(
            HealthcareEvent event
    );
}