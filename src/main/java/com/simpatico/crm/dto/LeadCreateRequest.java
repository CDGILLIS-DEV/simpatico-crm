package com.simpatico.crm.dto;

import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Data Transfer Object for creating a new Lead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadCreateRequest {

    @NotNull(message = "Buyer ID is required")
    private UUID buyerId;

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

    @Size(max = 100, message = "Preferred geographic area must not exceed 100 characters")
    private String preferredGeographicArea;

    @Size(max = 50, message = "Purchase frequency must not exceed 50 characters")
    private String purchaseFrequency;

    private String additionalRequirements;

    private LeadStatus status;

    @NotNull(message = "Lead source is required")
    private LeadSource source;
}
