package com.naveen.healthcare.providerservice.dto.request;

import com.naveen.healthcare.providerservice.enums.DoctorStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDoctorStatusRequest {

    @NotNull(message = "Doctor status is required")
    private DoctorStatus status;
}