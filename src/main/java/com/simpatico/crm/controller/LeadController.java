package com.simpatico.crm.controller;

import com.simpatico.crm.dto.LeadCreateRequest;
import com.simpatico.crm.dto.LeadResponse;
import com.simpatico.crm.dto.LeadSearchCriteria;
import com.simpatico.crm.dto.LeadStatusUpdateRequest;
import com.simpatico.crm.dto.LeadUpdateRequest;
import com.simpatico.crm.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller exposing REST endpoints under /api/leads for Lead management.
 */
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    /**
     * POST /api/leads : Create a new purchasing opportunity (lead) for a buyer.
     */
    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody LeadCreateRequest request) {
        LeadResponse created = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/leads/{id} : Retrieve details of a lead profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable UUID id) {
        LeadResponse lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    /**
     * GET /api/leads : Search and filter leads dynamically with pagination support.
     */
    @GetMapping
    public ResponseEntity<Page<LeadResponse>> searchLeads(
            LeadSearchCriteria criteria,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<LeadResponse> leads = leadService.searchLeads(criteria, pageable);
        return ResponseEntity.ok(leads);
    }

    /**
     * PUT /api/leads/{id} : Update details of an existing lead.
     */
    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable UUID id,
            @Valid @RequestBody LeadUpdateRequest request
    ) {
        LeadResponse updated = leadService.updateLead(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/leads/{id}/status : Update only the status of an existing lead.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<LeadResponse> updateLeadStatus(
            @PathVariable UUID id,
            @Valid @RequestBody LeadStatusUpdateRequest request
    ) {
        LeadResponse updated = leadService.updateLeadStatus(id, request.getStatus());
        return ResponseEntity.ok(updated);
    }
}
