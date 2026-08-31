package com.simpatico.crm.dto;

import java.util.UUID;

/**
 * Data Transfer Object record representing brief details of a Buyer inside Lead payloads.
 */
public record BuyerBriefResponse(
        UUID id,
        String firstName,
        String lastName,
        String companyName,
        String email,
        String phone
) {}
