package com.naveen.healthcare.memberservice.service;

import com.naveen.healthcare.memberservice.dto.request.CreateMemberProfileRequest;
import com.naveen.healthcare.memberservice.dto.request.UpdateMemberProfileRequest;
import com.naveen.healthcare.memberservice.dto.request.UpdateMemberStatusRequest;
import com.naveen.healthcare.memberservice.dto.response.InternalMemberVerificationResponse;
import com.naveen.healthcare.memberservice.dto.response.MemberProfileResponse;

import java.util.List;
import java.util.UUID;

public interface MemberProfileService {

    MemberProfileResponse createMyProfile(
            String authenticatedEmail,
            CreateMemberProfileRequest request
    );

    MemberProfileResponse getMyProfile(
            String authenticatedEmail
    );

    MemberProfileResponse updateMyProfile(
            String authenticatedEmail,
            UpdateMemberProfileRequest request
    );

    List<MemberProfileResponse> getAllMemberProfiles();

    MemberProfileResponse getMemberProfileById(
            UUID memberId
    );

    MemberProfileResponse updateMemberStatus(
            UUID memberId,
            UpdateMemberStatusRequest request
    );

    InternalMemberVerificationResponse getInternalVerification(
            String authenticatedEmail
    );
}