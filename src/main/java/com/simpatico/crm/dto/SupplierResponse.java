package com.simpatico.crm.dto;

import com.simpatico.crm.entity.SupplierStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Data Transfer Object record representing a Supplier response payload.
 */
public record SupplierResponse(
        UUID id,
        String companyName,
        String contactName,
        String email,
        String phone,
        String city,
        String state,
        String country,
        String website,
        SupplierStatus status,
        String notes,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
