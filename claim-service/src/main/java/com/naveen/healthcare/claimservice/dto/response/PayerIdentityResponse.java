package com.naveen.healthcare.claimservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PayerIdentityResponse {

    private UUID payerId;

    private String adminEmail;

    private String payerOrganizationName;

    private String status;

}