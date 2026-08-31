package com.simpatico.crm.dto;

import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object record representing a Lead response payload.
 */
public record LeadResponse(
        UUID id,
        BuyerBriefResponse buyer,
        String inventoryCategory,
        String inventoryCondition,
        Integer requestedQuantity,
        BigDecimal budget,
        String preferredGeographicArea,
        String purchaseFrequency,
        String additionalRequirements,
        LeadStatus status,
        LeadSource source,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
