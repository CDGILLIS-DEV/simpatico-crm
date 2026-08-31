package com.simpatico.crm.repository;

import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests to verify Flyway schema migrations, JPA entity mappings,
 * relationship constraints, and auditable timestamp hooks.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchemaIntegrationTests {

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private LeadRepository leadRepository;

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
    void shouldPersistBuyerAndLeadsWithCascadingAndAuditTimestamps() {
        // Arrange
        Buyer buyer = Buyer.builder()
                .firstName("John")
                .lastName("Doe")
                .companyName("Doe Liquidations")
                .email("john.doe@example.com")
                .phone("123-456-7890")
                .addressLine1("123 Main St")
                .city("Dallas")
                .state("TX")
                .zipCode("75001")
                .active(true)
                .build();

        Lead lead1 = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(100)
                .budget(new BigDecimal("5000.00"))
                .preferredGeographicArea("Texas")
                .purchaseFrequency("MONTHLY")
                .additionalRequirements("Must contain laptops")
                .status(LeadStatus.NEW)
                .source(LeadSource.GOOGLE_ADS)
                .build();

        Lead lead2 = Lead.builder()
                .inventoryCategory("APPAREL")
                .inventoryCondition("NEW")
                .requestedQuantity(500)
                .budget(new BigDecimal("12000.00"))
                .preferredGeographicArea("East Coast")
                .purchaseFrequency("ONCE")
                .status(LeadStatus.CONTACTED)
                .source(LeadSource.ORGANIC_SEARCH)
                .build();

        buyer.addLead(lead1);
        buyer.addLead(lead2);

        // Act
        Buyer savedBuyer = buyerRepository.save(buyer);
        buyerRepository.flush();
        entityManager.clear(); // clear first level cache to force loading from database

        // Assert
        assertThat(savedBuyer.getId()).isNotNull();

        // Reload from database to verify entities mapping
        Optional<Buyer> foundBuyerOpt = buyerRepository.findById(savedBuyer.getId());
        assertThat(foundBuyerOpt).isPresent();
        Buyer foundBuyer = foundBuyerOpt.get();
        assertThat(foundBuyer.getLeads()).hasSize(2);
        assertThat(foundBuyer.getCreatedAt()).isNotNull();
        assertThat(foundBuyer.getUpdatedAt()).isNotNull();

        // Verify Leads are persisted and referenced correctly
        List<Lead> leads = leadRepository.findByBuyerId(foundBuyer.getId());
        assertThat(leads).hasSize(2);
        
        Lead savedLead1 = leads.stream()
                .filter(l -> l.getInventoryCategory().equals("ELECTRONICS"))
                .findFirst()
                .orElseThrow();
        assertThat(savedLead1.getId()).isNotNull();
        assertThat(savedLead1.getBuyer().getId()).isEqualTo(foundBuyer.getId());
        assertThat(savedLead1.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(savedLead1.getSource()).isEqualTo(LeadSource.GOOGLE_ADS);
        assertThat(savedLead1.getCreatedAt()).isNotNull();
        assertThat(savedLead1.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // Arrange
        Buyer buyer1 = Buyer.builder()
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .build();
        buyerRepository.save(buyer1);
        buyerRepository.flush();

        Buyer buyer2 = Buyer.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("alice@example.com") // duplicate email
                .build();

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            buyerRepository.saveAndFlush(buyer2);
        });
    }

    @Test
    void shouldFindLeadsByStatus() {
        // Arrange
        Buyer buyer = Buyer.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .build();

        Lead lead = Lead.builder()
                .inventoryCategory("HOME_GOODS")
                .inventoryCondition("SHELF_PULLS")
                .status(LeadStatus.QUALIFIED)
                .source(LeadSource.DIRECT)
                .build();

        buyer.addLead(lead);
        buyerRepository.save(buyer);
        buyerRepository.flush();
        entityManager.clear();

        // Act
        List<Lead> qualifiedLeads = leadRepository.findByStatus(LeadStatus.QUALIFIED);

        // Assert
        assertThat(qualifiedLeads).hasSize(1);
        assertThat(qualifiedLeads.get(0).getInventoryCategory()).isEqualTo("HOME_GOODS");
    }
}
