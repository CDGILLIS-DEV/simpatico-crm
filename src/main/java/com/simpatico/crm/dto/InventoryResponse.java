package com.simpatico.crm.dto;

import com.simpatico.crm.entity.InventoryStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object record representing an Inventory response payload.
 */
public record InventoryResponse(
        UUID id,
        SupplierBriefResponse supplier,
        String title,
        String category,
        String condition,
        String description,
        Integer quantity,
        String unitType,
        BigDecimal askingPrice,
        String location,
        InventoryStatus availabilityStatus,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
