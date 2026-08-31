package com.simpatico.crm.repository;

import com.simpatico.crm.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing MatchRecord entities.
 */
@Repository
public interface MatchRepository extends JpaRepository<MatchRecord, UUID> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"lead", "lead.buyer", "inventory", "inventory.supplier"})
    java.util.Optional<MatchRecord> findById(UUID id);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"lead", "lead.buyer", "inventory", "inventory.supplier"})
    org.springframework.data.domain.Page<MatchRecord> findAll(org.springframework.data.domain.Pageable pageable);

    /**
     * Find all matches associated with a specific Lead.
     *
     * @param leadId the unique ID of the Lead.
     * @return a List of associated MatchRecords.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"lead", "lead.buyer", "inventory", "inventory.supplier"})
    List<MatchRecord> findByLeadId(UUID leadId);

    /**
     * Find a match by Lead ID and Inventory ID.
     *
     * @param leadId the Lead ID.
     * @param inventoryId the Inventory ID.
     * @return an Optional containing the MatchRecord if found.
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"lead", "lead.buyer", "inventory", "inventory.supplier"})
    Optional<MatchRecord> findByLeadIdAndInventoryId(UUID leadId, UUID inventoryId);
}
