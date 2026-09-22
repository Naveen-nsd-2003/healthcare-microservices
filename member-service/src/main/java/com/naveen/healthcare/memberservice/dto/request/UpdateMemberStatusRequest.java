package com.naveen.healthcare.memberservice.dto.request;

import com.naveen.healthcare.memberservice.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateMemberStatusRequest {

    @NotNull(message = "Member status is required")
    private MemberStatus status;
}