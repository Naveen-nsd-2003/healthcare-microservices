package com.naveen.healthcare.authservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(
            min = 4,
            max = 50,
            message = "Username must be between 4 and 50 characters"
    )
    private String username;

    @NotBlank(message = "First name is required")
    @Size(
            max = 50,
            message = "First name must not exceed 50 characters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(
            max = 50,
            message = "Last name must not exceed 50 characters"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(
            max = 150,
            message = "Email must not exceed 150 characters"
    )
    private String email;

    @Pattern(
            regexp = "^[0-9+()\\- ]{10,20}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;
}