package com.simpatico.crm.controller;

import com.simpatico.crm.dto.SupplierCreateRequest;
import com.simpatico.crm.dto.SupplierResponse;
import com.simpatico.crm.dto.SupplierUpdateRequest;
import com.simpatico.crm.service.SupplierService;
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
 * REST Controller exposing administrative CRUD routes under /api/suppliers for Supplier management.
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /**
     * POST /api/suppliers : Register a new supplier.
     */
    @PostMapping
    public ResponseEntity<SupplierResponse> createSupplier(@Valid @RequestBody SupplierCreateRequest request) {
        SupplierResponse created = supplierService.createSupplier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/suppliers/{id} : Retrieve supplier details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable UUID id) {
        SupplierResponse supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    /**
     * GET /api/suppliers : List suppliers using pagination.
     */
    @GetMapping
    public ResponseEntity<Page<SupplierResponse>> listSuppliers(@PageableDefault(size = 10) Pageable pageable) {
        Page<SupplierResponse> suppliers = supplierService.listSuppliers(pageable);
        return ResponseEntity.ok(suppliers);
    }

    /**
     * PUT /api/suppliers/{id} : Update details of an existing supplier.
     */
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable UUID id,
            @Valid @RequestBody SupplierUpdateRequest request
    ) {
        SupplierResponse updated = supplierService.updateSupplier(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/suppliers/{id} : Logically deactivates a supplier (sets status to INACTIVE).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateSupplier(@PathVariable UUID id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
