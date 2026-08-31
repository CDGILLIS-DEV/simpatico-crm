package com.simpatico.crm.dto;

import com.simpatico.crm.entity.MatchStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object record representing a Match response payload.
 */
public record MatchResponse(
        UUID id,
        LeadResponse lead,
        InventoryResponse inventory,
        MatchStatus status,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
