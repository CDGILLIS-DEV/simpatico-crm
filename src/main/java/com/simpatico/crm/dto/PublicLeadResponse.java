package com.simpatico.crm.dto;

import java.util.UUID;

/**
 * Data Transfer Object record representing a simple public lead response.
 */
public record PublicLeadResponse(
        boolean success,
        String message,
        UUID leadId
) {}
