package com.simpatico.crm.e2e;

import com.simpatico.crm.dto.PublicLeadSubmission;
import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import com.simpatico.crm.exception.SpamDetectedException;
import com.simpatico.crm.repository.BuyerRepository;
import com.simpatico.crm.repository.LeadRepository;
import com.simpatico.crm.service.PublicLeadService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests verifying database schema constraints, transaction boundary integrity,
 * foreign keys, unique email constraints, NOT NULL enforcement, and rollback safety.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseIntegrityAndTransactionTests {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private PublicLeadService publicLeadService;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        leadRepository.deleteAll();
        buyerRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Database Integrity: Enforce unique email constraint on buyers")
    void testUniqueEmailConstraint() {
        Buyer buyer1 = Buyer.builder()
                .firstName("Unique1")
                .lastName("Test")
                .email("duplicate@example.com")
                .build();
        buyerRepository.saveAndFlush(buyer1);

        Buyer buyer2 = Buyer.builder()
                .firstName("Unique2")
                .lastName("Test")
                .email("duplicate@example.com") // Same email
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            buyerRepository.saveAndFlush(buyer2);
        });
    }

    @Test
    @DisplayName("Database Integrity: Enforce NOT NULL / validation constraint on buyer first_name")
    void testNotNullFirstNameConstraint() {
        Buyer buyer = Buyer.builder()
                .lastName("Doe")
                .email("nullname@example.com")
                .build();

        assertThrows(Exception.class, () -> {
            buyerRepository.saveAndFlush(buyer);
        });
    }

    @Test
    @DisplayName("Database Integrity: Enforce foreign key relationship from lead to buyer")
    void testLeadBuyerForeignKeyAssociation() {
        Buyer buyer = Buyer.builder()
                .firstName("Parent")
                .lastName("Buyer")
                .email("parent.buyer@example.com")
                .build();

        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("NEW")
                .status(LeadStatus.NEW)
                .source(LeadSource.DIRECT)
                .build();

        buyer.addLead(lead);

        Buyer savedBuyer = buyerRepository.saveAndFlush(buyer);
        entityManager.clear();

        Buyer foundBuyer = buyerRepository.findById(savedBuyer.getId()).orElseThrow();
        assertThat(foundBuyer.getLeads()).hasSize(1);
        assertThat(foundBuyer.getLeads().get(0).getBuyer().getId()).isEqualTo(savedBuyer.getId());
    }

    @Test
    @DisplayName("Transaction Safety: Spam honeypot exception rolls back transaction completely without orphaned records")
    void testSpamHoneypotTransactionRollback() {
        PublicLeadSubmission spamSubmission = PublicLeadSubmission.builder()
                .firstName("Spam")
                .lastName("User")
                .email("spam.user@example.com")
                .inventoryCategory("TOYS")
                .inventoryCondition("NEW")
                .faxNumber("SPAM_HONEYPOT_FILLED") // Fails spam check
                .build();

        assertThrows(SpamDetectedException.class, () -> {
            publicLeadService.registerPublicLead(spamSubmission);
        });

        // Confirm database contains NO partially created buyer or lead records
        assertThat(buyerRepository.findByEmail("spam.user@example.com")).isEmpty();
        assertThat(leadRepository.findAll()).isEmpty();
    }
}
