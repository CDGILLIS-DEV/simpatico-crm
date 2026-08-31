package com.simpatico.crm.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object record representing a Buyer response payload.
 */
public record BuyerResponse(
        UUID id,
        String firstName,
        String lastName,
        String companyName,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String zipCode,
        String country,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
