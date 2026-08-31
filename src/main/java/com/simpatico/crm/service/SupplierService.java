package com.simpatico.crm.service;

import com.simpatico.crm.dto.SupplierCreateRequest;
import com.simpatico.crm.dto.SupplierResponse;
import com.simpatico.crm.dto.SupplierUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface defining business logic operations for Supplier profiles management.
 */
public interface SupplierService {

    /**
     * Create and persist a new Supplier.
     *
     * @param request the create request details.
     * @return the created Supplier response DTO.
     */
    SupplierResponse createSupplier(SupplierCreateRequest request);

    /**
     * Retrieve Supplier by ID.
     *
     * @param id the unique ID of the supplier.
     * @return the Supplier details.
     */
    SupplierResponse getSupplierById(UUID id);

    /**
     * List all suppliers using pagination.
     *
     * @param pageable pagination parameters.
     * @return a page of suppliers.
     */
    Page<SupplierResponse> listSuppliers(Pageable pageable);

    /**
     * Update details of an existing Supplier.
     *
     * @param id the unique ID of the supplier.
     * @param request the update request details.
     * @return the updated Supplier details.
     */
    SupplierResponse updateSupplier(UUID id, SupplierUpdateRequest request);

    /**
     * Deactivate a supplier (sets status to INACTIVE).
     *
     * @param id the unique ID of the supplier to deactivate.
     */
    void deactivateSupplier(UUID id);
}
