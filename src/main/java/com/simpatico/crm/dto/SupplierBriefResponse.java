package com.simpatico.crm.dto;

import java.util.UUID;

/**
 * Data Transfer Object record representing brief details of a Supplier inside Inventory payloads.
 */
public record SupplierBriefResponse(
        UUID id,
        String companyName,
        String contactName,
        String email,
        String phone
) {}
