package com.naveen.healthcare.memberservice.dto.response;

import com.naveen.healthcare.memberservice.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class InternalMemberVerificationResponse {

    private UUID memberId;

    private String email;

    private LocalDate dateOfBirth;

    private MemberStatus status;
}