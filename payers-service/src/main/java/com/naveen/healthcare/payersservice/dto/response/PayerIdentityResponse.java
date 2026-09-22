package com.naveen.healthcare.payersservice.dto.response;

import com.naveen.healthcare.payersservice.enums.PayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class PayerIdentityResponse {

    private UUID payerId;

    private String adminEmail;

    private String payerOrganizationName;

    private PayerStatus status;
}