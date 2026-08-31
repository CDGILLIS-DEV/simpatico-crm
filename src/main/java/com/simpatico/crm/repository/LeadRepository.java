package com.simpatico.crm.repository;

import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing Lead entities.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    /**
     * Find all leads associated with a specific buyer.
     *
     * @param buyerId the unique ID of the buyer.
     * @return a List of associated Leads.
     */
    List<Lead> findByBuyerId(UUID buyerId);

    /**
     * Find all leads in a specific status.
     *
     * @param status the status to query.
     * @return a List of Leads matching the status.
     */
    List<Lead> findByStatus(LeadStatus status);
}
