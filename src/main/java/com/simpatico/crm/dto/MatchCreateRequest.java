package com.simpatico.crm.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * Data Transfer Object for manually registering a Lead-to-Inventory Match.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchCreateRequest {

    @NotNull(message = "Lead ID is required")
    private UUID leadId;

    @NotNull(message = "Inventory ID is required")
    private UUID inventoryId;

    private String notes;
}
