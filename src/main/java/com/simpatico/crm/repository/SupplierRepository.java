package com.simpatico.crm.repository;

import com.simpatico.crm.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing Supplier entities.
 */
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    /**
     * Find a supplier by email address.
     *
     * @param email the email address to look up.
     * @return an Optional of the matching Supplier.
     */
    Optional<Supplier> findByEmail(String email);
}
