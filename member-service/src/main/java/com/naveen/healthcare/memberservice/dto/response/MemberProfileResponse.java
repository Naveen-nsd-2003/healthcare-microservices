package com.naveen.healthcare.memberservice.dto.response;

import com.naveen.healthcare.memberservice.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class MemberProfileResponse {

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private String phoneNumber;

    private String addressLine;

    private String city;

    private String state;

    private String postalCode;

    private String country;

    private String emergencyContactName;

    private String emergencyContactNumber;

    private MemberStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}