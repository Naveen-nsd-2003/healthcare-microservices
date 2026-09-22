package com.naveen.healthcare.providerservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(
            max = 150,
            message = "Department name must not exceed 150 characters"
    )
    private String departmentName;

    @NotBlank(message = "Department description is required")
    @Size(
            max = 1000,
            message = "Department description must not exceed 1000 characters"
    )
    private String description;

    @Size(
            max = 20,
            message = "Contact number must not exceed 20 characters"
    )
    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Contact number must contain between 10 and 20 valid characters"
    )
    private String contactNumber;

    @Size(
            max = 150,
            message = "Department location must not exceed 150 characters"
    )
    private String location;
}