package com.simpatico.crm.service;

import com.simpatico.crm.dto.MatchCreateRequest;
import com.simpatico.crm.dto.MatchResponse;
import com.simpatico.crm.dto.MatchStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface defining business logic operations for Lead-to-Inventory Matches.
 */
public interface MatchService {

    /**
     * Manually registers a match pairing.
     *
     * @param request the match details.
     * @return the created MatchResponse DTO.
     */
    MatchResponse createMatch(MatchCreateRequest request);

    /**
     * Retrieve Match details by ID.
     *
     * @param id the unique ID of the match record.
     * @return the Match details.
     */
    MatchResponse getMatchById(UUID id);

    /**
     * List all matches (paginated).
     *
     * @param pageable pagination parameters.
     * @return a page of matches.
     */
    Page<MatchResponse> listMatches(Pageable pageable);

    /**
     * Update the status of a specific match.
     *
     * @param id the unique ID of the match.
     * @param request the status update.
     * @return the updated Match details.
     */
    MatchResponse updateMatchStatus(UUID id, MatchStatusUpdateRequest request);

    /**
     * Retrieve all matches associated with a specific Lead.
     *
     * @param leadId the Lead ID.
     * @return a List of Match responses.
     */
    List<MatchResponse> getMatchesForLead(UUID leadId);

    /**
     * Run the Matching Engine algorithm to discover and register potential matches for a Lead.
     *
     * @param leadId the Lead ID.
     * @return a List of generated Match responses.
     */
    List<MatchResponse> generatePotentialMatches(UUID leadId);
}
