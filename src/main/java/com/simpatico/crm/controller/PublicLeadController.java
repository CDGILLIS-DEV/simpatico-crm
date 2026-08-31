package com.simpatico.crm.controller;

import com.simpatico.crm.dto.PublicLeadResponse;
import com.simpatico.crm.dto.PublicLeadSubmission;
import com.simpatico.crm.service.PublicLeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public unauthenticated REST controller exposing landing page lead registration APIs.
 */
@RestController
@RequestMapping("/api/public/leads")
@RequiredArgsConstructor
public class PublicLeadController {

    private final PublicLeadService publicLeadService;

    /**
     * POST /api/public/leads : Ingest public lead registration submissions.
     */
    @PostMapping
    public ResponseEntity<PublicLeadResponse> submitPublicLead(@Valid @RequestBody PublicLeadSubmission submission) {
        PublicLeadResponse response = publicLeadService.registerPublicLead(submission);
        return ResponseEntity.ok(response);
    }
}
