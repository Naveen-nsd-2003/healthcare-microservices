package com.naveen.healthcare.payersservice.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class MemberVerificationResponse {

    private UUID memberId;

    private String email;

    private LocalDate dateOfBirth;

    private String status;
}