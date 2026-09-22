package com.naveen.healthcare.memberservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateMemberProfileRequest {

    @NotBlank(message = "First name is required")
    @Size(
            max = 100,
            message = "First name must not exceed 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z .'-]*$",
            message = "First name contains invalid characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(
            max = 100,
            message = "Last name must not exceed 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z .'-]*$",
            message = "Last name contains invalid characters"
    )
    private String lastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Phone number must contain between 10 and 20 valid characters"
    )
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(
            max = 255,
            message = "Address must not exceed 255 characters"
    )
    private String addressLine;

    @NotBlank(message = "City is required")
    @Size(
            max = 100,
            message = "City must not exceed 100 characters"
    )
    private String city;

    @NotBlank(message = "State is required")
    @Size(
            max = 100,
            message = "State must not exceed 100 characters"
    )
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(
            regexp = "^[A-Za-z0-9 -]{3,20}$",
            message = "Postal code contains invalid characters"
    )
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Size(
            max = 100,
            message = "Country must not exceed 100 characters"
    )
    private String country;

    @NotBlank(message = "Emergency contact name is required")
    @Size(
            max = 150,
            message = "Emergency contact name must not exceed 150 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z][A-Za-z .'-]*$",
            message = "Emergency contact name contains invalid characters"
    )
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact number is required")
    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Emergency contact number must contain between 10 and 20 valid characters"
    )
    private String emergencyContactNumber;
}