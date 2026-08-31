package com.simpatico.crm.dto;

import com.simpatico.crm.entity.LeadSource;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object capturing public lead submissions from the public website form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicLeadSubmission {

    // Buyer Information
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

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    // Lead Requirements
    @NotBlank(message = "Inventory category is required")
    @Size(max = 50, message = "Inventory category must not exceed 50 characters")
    private String inventoryCategory;

    @NotBlank(message = "Inventory condition is required")
    @Size(max = 50, message = "Inventory condition must not exceed 50 characters")
    private String inventoryCondition;

    @Positive(message = "Requested quantity must be greater than zero")
    private Integer requestedQuantity;

    @Positive(message = "Budget must be greater than zero")
    private BigDecimal budget;

    @Size(max = 50, message = "Purchase frequency must not exceed 50 characters")
    private String purchaseFrequency;

    private String additionalRequirements;

    private LeadSource source;

    // Honeypot field for bot/spam prevention
    @Size(max = 50)
    private String faxNumber;
}
