package com.simpatico.crm.dto;

import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Filter criteria class containing parameters used to search for Leads dynamically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadSearchCriteria {

    private LeadStatus status;

    private LeadSource source;

    private String category;

    private UUID buyerId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime createdAfter;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime createdBefore;
}
