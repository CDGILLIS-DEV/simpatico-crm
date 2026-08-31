package com.simpatico.crm.dto;

import com.simpatico.crm.entity.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Data Transfer Object for changing the status of a Lead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadStatusUpdateRequest {

    @NotNull(message = "Lead status is required")
    private LeadStatus status;
}
