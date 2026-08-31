package com.simpatico.crm.controller;

import com.simpatico.crm.dto.BuyerCreateRequest;
import com.simpatico.crm.dto.BuyerResponse;
import com.simpatico.crm.dto.BuyerUpdateRequest;
import com.simpatico.crm.service.BuyerService;
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
 * REST Controller exposing REST endpoints under /api/buyers for Buyer administration.
 */
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    /**
     * POST /api/buyers : Create a new buyer profile.
     */
    @PostMapping
    public ResponseEntity<BuyerResponse> createBuyer(@Valid @RequestBody BuyerCreateRequest request) {
        BuyerResponse created = buyerService.createBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/buyers/{id} : Retrieve details of a buyer profile by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponse> getBuyerById(@PathVariable UUID id) {
        BuyerResponse buyer = buyerService.getBuyerById(id);
        return ResponseEntity.ok(buyer);
    }

    /**
     * GET /api/buyers : List all buyer profiles supporting pagination.
     */
    @GetMapping
    public ResponseEntity<Page<BuyerResponse>> getAllBuyers(@PageableDefault(size = 10) Pageable pageable) {
        Page<BuyerResponse> buyers = buyerService.getAllBuyers(pageable);
        return ResponseEntity.ok(buyers);
    }

    /**
     * PUT /api/buyers/{id} : Update fields of an existing buyer profile.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BuyerResponse> updateBuyer(
            @PathVariable UUID id,
            @Valid @RequestBody BuyerUpdateRequest request
    ) {
        BuyerResponse updated = buyerService.updateBuyer(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/buyers/{id} : Logically delete/deactivate a buyer profile.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateBuyer(@PathVariable UUID id) {
        buyerService.deactivateBuyer(id);
        return ResponseEntity.noContent().build();
    }
}
