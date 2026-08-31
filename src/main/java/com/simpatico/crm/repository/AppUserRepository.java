package com.simpatico.crm.repository;

import com.simpatico.crm.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing AppUser entities.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    /**
     * Find user by username.
     *
     * @param username the username.
     * @return an Optional containing the AppUser if found.
     */
    Optional<AppUser> findByUsername(String username);
}
