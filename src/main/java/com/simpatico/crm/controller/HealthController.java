package com.simpatico.crm.controller;

import com.simpatico.crm.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for checking API service health status.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * Endpoint to check if the CRM API is active.
     *
     * @return a JSON response indicating the status is "UP".
     */
    @GetMapping
    public ResponseEntity<HealthResponse> getHealth() {
        return ResponseEntity.ok(new HealthResponse("UP", "Simpatico CRM API is running"));
    }
}
