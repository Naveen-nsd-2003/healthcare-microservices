package com.naveen.healthcare.appointmentservice.client;

import com.naveen.healthcare.appointmentservice.config.FeignClientConfig;
import com.naveen.healthcare.appointmentservice.dto.response.MemberVerificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "member-service",
        configuration = FeignClientConfig.class
)
public interface MemberServiceClient {

    @GetMapping("/internal/members/me/verification")
    MemberVerificationResponse
    getAuthenticatedMemberVerification();
}