package com.simpatico.crm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Data Transfer Object for updating an existing Buyer.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^$|^[\\d\\s()+-]{7,20}$", message = "Phone must be a valid phone number format between 7 and 20 digits")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 150, message = "Address line 1 must not exceed 150 characters")
    private String addressLine1;

    @Size(max = 150, message = "Address line 2 must not exceed 150 characters")
    private String addressLine2;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Size(max = 20, message = "Zip code must not exceed 20 characters")
    private String zipCode;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;
}
