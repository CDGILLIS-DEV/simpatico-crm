package com.simpatico.crm.controller;

import com.simpatico.crm.dto.MatchCreateRequest;
import com.simpatico.crm.dto.MatchResponse;
import com.simpatico.crm.dto.MatchStatusUpdateRequest;
import com.simpatico.crm.service.MatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller exposing administrative match registry, status updates, and generation endpoints.
 */
@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    /**
     * POST /api/matches : Create a new match record manually.
     */
    @PostMapping("/api/matches")
    public ResponseEntity<MatchResponse> createMatch(@Valid @RequestBody MatchCreateRequest request) {
        MatchResponse created = matchService.createMatch(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/matches : List matches (paginated).
     */
    @GetMapping("/api/matches")
    public ResponseEntity<Page<MatchResponse>> listMatches(@PageableDefault(size = 10) Pageable pageable) {
        Page<MatchResponse> matches = matchService.listMatches(pageable);
        return ResponseEntity.ok(matches);
    }

    /**
     * GET /api/matches/{id} : Retrieve match details by ID.
     */
    @GetMapping("/api/matches/{id}")
    public ResponseEntity<MatchResponse> getMatchById(@PathVariable UUID id) {
        MatchResponse match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    /**
     * PATCH /api/matches/{id}/status : Update the status of an existing match.
     */
    @PatchMapping("/api/matches/{id}/status")
    public ResponseEntity<MatchResponse> updateMatchStatus(
            @PathVariable UUID id,
            @Valid @RequestBody MatchStatusUpdateRequest request
    ) {
        MatchResponse updated = matchService.updateMatchStatus(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/leads/{leadId}/matches : List existing matches associated with a specific Lead.
     */
    @GetMapping("/api/leads/{leadId}/matches")
    public ResponseEntity<List<MatchResponse>> getMatchesForLead(@PathVariable UUID leadId) {
        List<MatchResponse> matches = matchService.getMatchesForLead(leadId);
        return ResponseEntity.ok(matches);
    }

    /**
     * POST /api/leads/{leadId}/matches/generate : Trigger rule-based matching engine to discover potential matches.
     */
    @PostMapping("/api/leads/{leadId}/matches/generate")
    public ResponseEntity<List<MatchResponse>> generatePotentialMatches(@PathVariable UUID leadId) {
        List<MatchResponse> generated = matchService.generatePotentialMatches(leadId);
        return ResponseEntity.ok(generated);
    }
}
