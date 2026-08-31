package com.simpatico.crm.repository;

import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing Lead entities.
 */
@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID>, JpaSpecificationExecutor<Lead> {

    /**
     * Find all leads associated with a specific buyer.
     *
     * @param buyerId the unique ID of the buyer.
     * @return a List of associated Leads.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"buyer"})
    java.util.Optional<Lead> findById(UUID id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"buyer"})
    List<Lead> findByBuyerId(UUID buyerId);

    /**
     * Find all leads in a specific status.
     *
     * @param status the status to query.
     * @return a List of Leads matching the status.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"buyer"})
    List<Lead> findByStatus(LeadStatus status);
}
