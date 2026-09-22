package com.naveen.healthcare.payersservice.dto.request;

import com.naveen.healthcare.payersservice.enums.PayerStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdatePayerStatusRequest {


    @NotNull(
            message = "Payer status is required"
    )
    private PayerStatus status;
}