package com.naveen.healthcare.providerservice.dto.request;

import com.naveen.healthcare.providerservice.enums.ProviderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProviderStatusRequest {

    @NotNull(message = "Provider status is required")
    private ProviderStatus status;
}