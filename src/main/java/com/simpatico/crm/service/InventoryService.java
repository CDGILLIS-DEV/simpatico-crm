package com.simpatico.crm.service;

import com.simpatico.crm.dto.InventoryCreateRequest;
import com.simpatico.crm.dto.InventoryResponse;
import com.simpatico.crm.dto.InventorySearchCriteria;
import com.simpatico.crm.dto.InventoryUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface defining business logic operations for Inventory offerings management.
 */
public interface InventoryService {

    /**
     * Register a new Inventory item associated with a Supplier.
     *
     * @param request the create request details.
     * @return the registered Inventory details.
     */
    InventoryResponse createInventory(InventoryCreateRequest request);

    /**
     * Retrieve Inventory offering details by ID.
     *
     * @param id the unique ID of the inventory offering.
     * @return the Inventory details.
     */
    InventoryResponse getInventoryById(UUID id);

    /**
     * Search and filter Inventory dynamically using criteria and pagination.
     *
     * @param criteria search parameters.
     * @param pageable pagination parameters.
     * @return a page of Inventory offerings.
     */
    Page<InventoryResponse> searchInventories(InventorySearchCriteria criteria, Pageable pageable);

    /**
     * Update details of an existing Inventory offering.
     *
     * @param id the unique ID of the inventory item.
     * @param request the update details.
     * @return the updated Inventory details.
     */
    InventoryResponse updateInventory(UUID id, InventoryUpdateRequest request);

    /**
     * Deactivate an inventory item (sets availability status to INACTIVE).
     *
     * @param id the unique ID of the inventory item to deactivate.
     */
    void deactivateInventory(UUID id);
}
