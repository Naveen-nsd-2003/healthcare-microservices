package com.naveen.healthcare.providerservice.dto.request;

import com.naveen.healthcare.providerservice.enums.ProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateProviderOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(
            max = 200,
            message = "Organization name must not exceed 200 characters"
    )
    private String organizationName;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    @NotBlank(message = "Registration number is required")
    @Size(
            min = 3,
            max = 100,
            message = "Registration number must contain between 3 and 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9./_-]+$",
            message = "Registration number contains invalid characters"
    )
    private String registrationNumber;

    @NotBlank(message = "License number is required")
    @Size(
            min = 3,
            max = 100,
            message = "License number must contain between 3 and 100 characters"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9./_-]+$",
            message = "License number contains invalid characters"
    )
    private String licenseNumber;

    @NotBlank(message = "Provider administrator email is required")
    @Email(message = "Provider administrator email must be valid")
    @Size(
            max = 150,
            message = "Provider administrator email must not exceed 150 characters"
    )
    private String adminEmail;

    @NotBlank(message = "Contact number is required")
    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Contact number must contain between 10 and 20 valid characters"
    )
    private String contactNumber;

    @Size(
            max = 255,
            message = "Website must not exceed 255 characters"
    )
    @Pattern(
            regexp = "^(https?://)?([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}(:[0-9]{1,5})?(/.*)?$",
            message = "Website must be a valid HTTP o* HTTPS URL"
            )
    private String website;

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

    @NotNull(
            message = "Emergency services availability is required"
    )
    private Boolean emergencyServicesAvailable;
}