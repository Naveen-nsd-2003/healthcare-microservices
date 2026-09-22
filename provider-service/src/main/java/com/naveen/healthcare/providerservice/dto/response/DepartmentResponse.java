package com.naveen.healthcare.providerservice.dto.response;

import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class DepartmentResponse {

    private UUID id;

    private String departmentName;

    private String departmentCode;

    private String description;

    private String contactNumber;

    private String location;

    private DepartmentStatus status;

    private UUID providerId;

    private String providerOrganizationName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}