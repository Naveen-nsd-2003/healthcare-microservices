package com.naveen.healthcare.payersservice.dto.request;

import com.naveen.healthcare.payersservice.enums.PolicyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePolicyStatusRequest {

    @NotNull(message = "Policy status is required")
    private PolicyStatus status;
}