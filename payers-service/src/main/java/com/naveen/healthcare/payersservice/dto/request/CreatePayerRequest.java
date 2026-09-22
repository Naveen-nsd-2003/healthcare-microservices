package com.naveen.healthcare.payersservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class CreatePayerRequest {


    @NotBlank(
            message = "Organization name is required"
    )
    @Size(
            max = 150,
            message = "Organization name must not exceed 150 characters"
    )
    private String organizationName;


    @NotBlank(
            message = "Registration number is required"
    )
    @Size(
            max = 100,
            message = "Registration number must not exceed 100 characters"
    )
    private String registrationNumber;


    @NotBlank(
            message = "Admin email is required"
    )
    @Email(
            message = "Invalid admin email format"
    )
    @Size(
            max = 150,
            message = "Admin email must not exceed 150 characters"
    )
    private String adminEmail;


    @NotBlank(
            message = "Contact number is required"
    )
    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Invalid contact number"
    )
    private String contactNumber;


    @Size(
            max = 255,
            message = "Website must not exceed 255 characters"
    )
    private String website;


    @NotBlank(
            message = "Address is required"
    )
    @Size(
            max = 255,
            message = "Address must not exceed 255 characters"
    )
    private String addressLine;


    @NotBlank(
            message = "City is required"
    )
    @Size(
            max = 100,
            message = "City must not exceed 100 characters"
    )
    private String city;


    @NotBlank(
            message = "State is required"
    )
    @Size(
            max = 100,
            message = "State must not exceed 100 characters"
    )
    private String state;


    @NotBlank(
            message = "Postal code is required"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9 -]{3,20}$",
            message = "Invalid postal code"
    )
    private String postalCode;


    @NotBlank(
            message = "Country is required"
    )
    @Size(
            max = 100,
            message = "Country must not exceed 100 characters"
    )
    private String country;
}