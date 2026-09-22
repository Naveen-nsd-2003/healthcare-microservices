package com.naveen.healthcare.payersservice.dto.response;

import com.naveen.healthcare.payersservice.enums.PayerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Builder
@AllArgsConstructor
public class PayerResponse {


    private UUID id;


    private String organizationName;


    private String registrationNumber;


    private String adminEmail;


    private String contactNumber;


    private String website;


    private String addressLine;


    private String city;


    private String state;


    private String postalCode;


    private String country;


    private PayerStatus status;


    private LocalDateTime createdAt;


    private LocalDateTime updatedAt;
}