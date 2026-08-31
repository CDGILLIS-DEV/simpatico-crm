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

    /**
     * Find all matches associated with a specific Lead.
     *
     * @param leadId the unique ID of the Lead.
     * @return a List of associated MatchRecords.
     */
    List<MatchRecord> findByLeadId(UUID leadId);

    /**
     * Find a match by Lead ID and Inventory ID.
     *
     * @param leadId the Lead ID.
     * @param inventoryId the Inventory ID.
     * @return an Optional containing the MatchRecord if found.
     */
    Optional<MatchRecord> findByLeadIdAndInventoryId(UUID leadId, UUID inventoryId);
}
