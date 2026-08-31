package com.simpatico.crm.dto;

import com.simpatico.crm.entity.MatchStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Data Transfer Object for changing the status of a Match.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchStatusUpdateRequest {

    @NotNull(message = "Match status is required")
    private MatchStatus status;

    private String notes;
}
