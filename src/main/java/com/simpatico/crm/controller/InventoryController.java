package com.simpatico.crm.controller;

import com.simpatico.crm.dto.InventoryCreateRequest;
import com.simpatico.crm.dto.InventoryResponse;
import com.simpatico.crm.dto.InventorySearchCriteria;
import com.simpatico.crm.dto.InventoryUpdateRequest;
import com.simpatico.crm.service.InventoryService;
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
 * REST Controller exposing administrative CRUD routes under /api/inventories for Inventory offerings.
 */
@RestController
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * POST /api/inventories : Register a new inventory item.
     */
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@Valid @RequestBody InventoryCreateRequest request) {
        InventoryResponse created = inventoryService.createInventory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/inventories/{id} : Retrieve inventory offering details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getInventoryById(@PathVariable UUID id) {
        InventoryResponse inventory = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(inventory);
    }

    /**
     * GET /api/inventories : Search and filter inventory offerings dynamically with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> searchInventories(
            InventorySearchCriteria criteria,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<InventoryResponse> page = inventoryService.searchInventories(criteria, pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * PUT /api/inventories/{id} : Update details of an existing inventory item.
     */
    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryUpdateRequest request
    ) {
        InventoryResponse updated = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/inventories/{id} : Logically deactivates an inventory offering (sets availability to INACTIVE).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateInventory(@PathVariable UUID id) {
        inventoryService.deactivateInventory(id);
        return ResponseEntity.noContent().build();
    }
}
