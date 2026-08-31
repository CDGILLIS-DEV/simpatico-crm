package com.simpatico.crm.service;

import com.simpatico.crm.dto.LeadCreateRequest;
import com.simpatico.crm.dto.LeadResponse;
import com.simpatico.crm.dto.LeadSearchCriteria;
import com.simpatico.crm.dto.LeadUpdateRequest;
import com.simpatico.crm.entity.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface defining business logic operations for Lead management.
 */
public interface LeadService {

    /**
     * Create and persist a new Lead, verifying the associated Buyer exists.
     *
     * @param request the create request details.
     * @return the created Lead response.
     */
    LeadResponse createLead(LeadCreateRequest request);

    /**
     * Retrieve a Lead by its ID.
     *
     * @param id the unique ID of the lead.
     * @return the Lead response.
     */
    LeadResponse getLeadById(UUID id);

    /**
     * Search and filter Leads dynamically using criteria and pagination.
     *
     * @param criteria the filter values.
     * @param pageable pagination parameters.
     * @return a page of Lead responses.
     */
    Page<LeadResponse> searchLeads(LeadSearchCriteria criteria, Pageable pageable);

    /**
     * Update an existing Lead's details.
     *
     * @param id the unique ID of the lead to update.
     * @param request the update request details.
     * @return the updated Lead response.
     */
    LeadResponse updateLead(UUID id, LeadUpdateRequest request);

    /**
     * Update only the status of an existing Lead.
     *
     * @param id the unique ID of the lead to update.
     * @param status the new status.
     * @return the updated Lead response.
     */
    LeadResponse updateLeadStatus(UUID id, LeadStatus status);
}
