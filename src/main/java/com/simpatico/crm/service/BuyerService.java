package com.simpatico.crm.service;

import com.simpatico.crm.dto.BuyerCreateRequest;
import com.simpatico.crm.dto.BuyerResponse;
import com.simpatico.crm.dto.BuyerUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface defining business logic operations for Buyer management.
 */
public interface BuyerService {

    /**
     * Create and persist a new Buyer.
     *
     * @param request the create request details.
     * @return the created Buyer response.
     */
    BuyerResponse createBuyer(BuyerCreateRequest request);

    /**
     * Retrieve a Buyer by their ID.
     *
     * @param id the unique ID of the buyer.
     * @return the Buyer response.
     */
    BuyerResponse getBuyerById(UUID id);

    /**
     * Retrieve a paginated list of all Buyers.
     *
     * @param pageable pagination parameters.
     * @return a page of Buyer responses.
     */
    Page<BuyerResponse> getAllBuyers(Pageable pageable);

    /**
     * Update an existing Buyer's details.
     *
     * @param id the unique ID of the buyer to update.
     * @param request the update request details.
     * @return the updated Buyer response.
     */
    BuyerResponse updateBuyer(UUID id, BuyerUpdateRequest request);

    /**
     * Logically deactivate a Buyer by setting active status to false.
     *
     * @param id the unique ID of the buyer to deactivate.
     */
    void deactivateBuyer(UUID id);
}
