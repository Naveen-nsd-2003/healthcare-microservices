package com.naveen.healthcare.providerservice.dto.request;

import com.naveen.healthcare.providerservice.enums.DepartmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDepartmentStatusRequest {

    @NotNull(message = "Department status is required")
    private DepartmentStatus status;
}