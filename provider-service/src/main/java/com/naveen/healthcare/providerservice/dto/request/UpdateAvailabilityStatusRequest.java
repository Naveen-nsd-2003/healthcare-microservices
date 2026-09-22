package com.naveen.healthcare.providerservice.dto.request;

import com.naveen.healthcare.providerservice.enums.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAvailabilityStatusRequest {

    @NotNull(message = "Availability status is required")
    private AvailabilityStatus status;
}