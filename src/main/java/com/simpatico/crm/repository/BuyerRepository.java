package com.simpatico.crm.repository;

import com.simpatico.crm.entity.Buyer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Buyer entities.
 */
@Repository
public interface BuyerRepository extends JpaRepository<Buyer, UUID> {

    /**
     * Find a buyer by their unique email.
     *
     * @param email the email to search for.
     * @return an Optional containing the Buyer if found.
     */
    Optional<Buyer> findByEmail(String email);
}
