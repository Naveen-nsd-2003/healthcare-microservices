package com.naveen.healthcare.memberservice.serviceimpl;

import com.naveen.healthcare.memberservice.dto.request.CreateMemberProfileRequest;
import com.naveen.healthcare.memberservice.dto.request.UpdateMemberProfileRequest;
import com.naveen.healthcare.memberservice.dto.request.UpdateMemberStatusRequest;
import com.naveen.healthcare.memberservice.dto.response.InternalMemberVerificationResponse;
import com.naveen.healthcare.memberservice.dto.response.MemberProfileResponse;
import com.naveen.healthcare.memberservice.entity.MemberProfile;
import com.naveen.healthcare.memberservice.enums.MemberStatus;
import com.naveen.healthcare.memberservice.exception.DuplicateMemberProfileException;
import com.naveen.healthcare.memberservice.exception.MemberProfileNotFoundException;
import com.naveen.healthcare.memberservice.repository.MemberProfileRepository;
import com.naveen.healthcare.memberservice.service.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberProfileServiceImpl
        implements MemberProfileService {

    private final MemberProfileRepository memberProfileRepository;

    @Override
    @Transactional
    public MemberProfileResponse createMyProfile(
            String authenticatedEmail,
            CreateMemberProfileRequest request
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        if (memberProfileRepository
                .existsByEmailIgnoreCase(normalizedEmail)) {

            throw new DuplicateMemberProfileException(
                    "A Member profile already exists for "
                            + "the authenticated email"
            );
        }

        MemberProfile memberProfile =
                MemberProfile.builder()
                        .email(normalizedEmail)
                        .firstName(
                                normalizeRequiredText(
                                        request.getFirstName()
                                )
                        )
                        .lastName(
                                normalizeRequiredText(
                                        request.getLastName()
                                )
                        )
                        .dateOfBirth(
                                request.getDateOfBirth()
                        )
                        .phoneNumber(
                                normalizeRequiredText(
                                        request.getPhoneNumber()
                                )
                        )
                        .addressLine(
                                normalizeRequiredText(
                                        request.getAddressLine()
                                )
                        )
                        .city(
                                normalizeRequiredText(
                                        request.getCity()
                                )
                        )
                        .state(
                                normalizeRequiredText(
                                        request.getState()
                                )
                        )
                        .postalCode(
                                normalizeRequiredText(
                                        request.getPostalCode()
                                )
                        )
                        .country(
                                normalizeRequiredText(
                                        request.getCountry()
                                )
                        )
                        .emergencyContactName(
                                normalizeRequiredText(
                                        request.getEmergencyContactName()
                                )
                        )
                        .emergencyContactNumber(
                                normalizeRequiredText(
                                        request.getEmergencyContactNumber()
                                )
                        )
                        .status(MemberStatus.ACTIVE)
                        .build();

        MemberProfile savedMemberProfile =
                memberProfileRepository.save(memberProfile);

        return mapToResponse(savedMemberProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse getMyProfile(
            String authenticatedEmail
    ) {

        MemberProfile memberProfile =
                findMemberProfileByEmail(authenticatedEmail);

        return mapToResponse(memberProfile);
    }

    @Override
    @Transactional
    public MemberProfileResponse updateMyProfile(
            String authenticatedEmail,
            UpdateMemberProfileRequest request
    ) {

        MemberProfile memberProfile =
                findMemberProfileByEmail(authenticatedEmail);

        validateMemberCanUpdateProfile(memberProfile);

        memberProfile.setFirstName(
                normalizeRequiredText(
                        request.getFirstName()
                )
        );

        memberProfile.setLastName(
                normalizeRequiredText(
                        request.getLastName()
                )
        );

        memberProfile.setDateOfBirth(
                request.getDateOfBirth()
        );

        memberProfile.setPhoneNumber(
                normalizeRequiredText(
                        request.getPhoneNumber()
                )
        );

        memberProfile.setAddressLine(
                normalizeRequiredText(
                        request.getAddressLine()
                )
        );

        memberProfile.setCity(
                normalizeRequiredText(
                        request.getCity()
                )
        );

        memberProfile.setState(
                normalizeRequiredText(
                        request.getState()
                )
        );

        memberProfile.setPostalCode(
                normalizeRequiredText(
                        request.getPostalCode()
                )
        );

        memberProfile.setCountry(
                normalizeRequiredText(
                        request.getCountry()
                )
        );

        memberProfile.setEmergencyContactName(
                normalizeRequiredText(
                        request.getEmergencyContactName()
                )
        );

        memberProfile.setEmergencyContactNumber(
                normalizeRequiredText(
                        request.getEmergencyContactNumber()
                )
        );

        MemberProfile updatedMemberProfile =
                memberProfileRepository.save(memberProfile);

        return mapToResponse(updatedMemberProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberProfileResponse> getAllMemberProfiles() {

        return memberProfileRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfileById(
            UUID memberId
    ) {

        MemberProfile memberProfile =
                findMemberProfileById(memberId);

        return mapToResponse(memberProfile);
    }

    @Override
    @Transactional
    public MemberProfileResponse updateMemberStatus(
            UUID memberId,
            UpdateMemberStatusRequest request
    ) {

        MemberProfile memberProfile =
                findMemberProfileById(memberId);

        MemberStatus requestedStatus =
                request.getStatus();

        if (memberProfile.getStatus() == requestedStatus) {

            throw new IllegalArgumentException(
                    "Member profile is already in "
                            + requestedStatus
                            + " status"
            );
        }

        memberProfile.setStatus(requestedStatus);

        MemberProfile updatedMemberProfile =
                memberProfileRepository.save(memberProfile);

        return mapToResponse(updatedMemberProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public InternalMemberVerificationResponse getInternalVerification(
            String authenticatedEmail
    ) {

        MemberProfile memberProfile =
                findMemberProfileByEmail(authenticatedEmail);

        return mapToInternalVerificationResponse(memberProfile);
    }

    private MemberProfile findMemberProfileByEmail(
            String authenticatedEmail
    ) {

        String normalizedEmail =
                normalizeEmail(authenticatedEmail);

        return memberProfileRepository
                .findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() ->
                        new MemberProfileNotFoundException(
                                "Member profile not found for "
                                        + "authenticated email: "
                                        + normalizedEmail
                        )
                );
    }

    private MemberProfile findMemberProfileById(
            UUID memberId
    ) {

        return memberProfileRepository
                .findById(memberId)
                .orElseThrow(() ->
                        new MemberProfileNotFoundException(
                                "Member profile not found with ID: "
                                        + memberId
                        )
                );
    }

    private void validateMemberCanUpdateProfile(
            MemberProfile memberProfile
    ) {

        if (memberProfile.getStatus()
                == MemberStatus.SUSPENDED) {

            throw new IllegalArgumentException(
                    "A suspended Member profile cannot be updated"
            );
        }

        if (memberProfile.getStatus()
                == MemberStatus.INACTIVE) {

            throw new IllegalArgumentException(
                    "An inactive Member profile cannot be updated"
            );
        }
    }

    private MemberProfileResponse mapToResponse(
            MemberProfile memberProfile
    ) {

        return MemberProfileResponse.builder()
                .id(
                        memberProfile.getId()
                )
                .email(
                        memberProfile.getEmail()
                )
                .firstName(
                        memberProfile.getFirstName()
                )
                .lastName(
                        memberProfile.getLastName()
                )
                .dateOfBirth(
                        memberProfile.getDateOfBirth()
                )
                .phoneNumber(
                        memberProfile.getPhoneNumber()
                )
                .addressLine(
                        memberProfile.getAddressLine()
                )
                .city(
                        memberProfile.getCity()
                )
                .state(
                        memberProfile.getState()
                )
                .postalCode(
                        memberProfile.getPostalCode()
                )
                .country(
                        memberProfile.getCountry()
                )
                .emergencyContactName(
                        memberProfile.getEmergencyContactName()
                )
                .emergencyContactNumber(
                        memberProfile.getEmergencyContactNumber()
                )
                .status(
                        memberProfile.getStatus()
                )
                .createdAt(
                        memberProfile.getCreatedAt()
                )
                .updatedAt(
                        memberProfile.getUpdatedAt()
                )
                .build();
    }

    private InternalMemberVerificationResponse
    mapToInternalVerificationResponse(
            MemberProfile memberProfile
    ) {

        return InternalMemberVerificationResponse.builder()
                .memberId(
                        memberProfile.getId()
                )
                .email(
                        memberProfile.getEmail()
                )
                .dateOfBirth(
                        memberProfile.getDateOfBirth()
                )
                .status(
                        memberProfile.getStatus()
                )
                .build();
    }

    private String normalizeEmail(
            String email
    ) {

        if (email == null || email.isBlank()) {

            throw new IllegalArgumentException(
                    "Authenticated email is required"
            );
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeRequiredText(
            String value
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    "Required profile information cannot be empty"
            );
        }

        return value.trim();
    }
}