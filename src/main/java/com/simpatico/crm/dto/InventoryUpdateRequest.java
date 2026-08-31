package com.simpatico.crm.dto;

import com.simpatico.crm.entity.InventoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

/**
 * Data Transfer Object for updating an existing Inventory offering.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotBlank(message = "Condition is required")
    @Size(max = 50, message = "Condition must not exceed 50 characters")
    private String condition;

    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @NotBlank(message = "Unit type is required")
    @Size(max = 20, message = "Unit type must not exceed 20 characters")
    private String unitType;

    @NotNull(message = "Asking price is required")
    @Positive(message = "Asking price must be greater than zero")
    private BigDecimal askingPrice;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @NotNull(message = "Availability status is required")
    private InventoryStatus availabilityStatus;
}
