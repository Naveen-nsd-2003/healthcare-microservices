package com.naveen.healthcare.payersservice.dto.request;

import com.naveen.healthcare.payersservice.enums.InsurancePlanStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInsurancePlanStatusRequest {

    @NotNull(message = "Insurance plan status is required")
    private InsurancePlanStatus status;
}