package com.simpatico.crm.dto;

import com.simpatico.crm.entity.SupplierStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Data Transfer Object for registering a new Supplier.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierCreateRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 100, message = "Company name must not exceed 100 characters")
    private String companyName;

    @Size(max = 100, message = "Contact name must not exceed 100 characters")
    private String contactName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Pattern(regexp = "^$|^[\\d\\s()+-]{7,20}$", message = "Phone must be a valid phone format between 7 and 20 digits")
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @Size(max = 100, message = "Website URL must not exceed 100 characters")
    private String website;

    private SupplierStatus status;

    private String notes;
}
