package com.simpatico.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.PublicLeadSubmission;
import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import com.simpatico.crm.repository.BuyerRepository;
import com.simpatico.crm.repository.LeadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full integration tests verifying public landing page endpoint processing,
 * database updates/matching rules, rate limit filter blocks, and spam filtering.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicLeadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private com.simpatico.crm.config.RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter.clearRateLimits();
        leadRepository.deleteAll();
        buyerRepository.deleteAll();
    }

    @Test
    void shouldCreateNewBuyerAndLeadOnValidSubmission() throws Exception {
        PublicLeadSubmission submission = PublicLeadSubmission.builder()
                .firstName("John")
                .lastName("Smith")
                .companyName("Smith Enterprises")
                .email("john.smith@example.com")
                .phone("+15551234567")
                .city("Chicago")
                .state("IL")
                .country("USA")
                .inventoryCategory("TOYS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(500)
                .budget(new BigDecimal("10000.00"))
                .purchaseFrequency("MONTHLY")
                .additionalRequirements("Need grading details")
                .build();

        mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Your request has been received."))
                .andExpect(jsonPath("$.leadId").isNotEmpty());

        // Verify Buyer was created
        Optional<Buyer> createdBuyer = buyerRepository.findByEmail("john.smith@example.com");
        assertTrue(createdBuyer.isPresent());
        assertEquals("John", createdBuyer.get().getFirstName());
        assertEquals("Smith", createdBuyer.get().getLastName());
        assertEquals("Smith Enterprises", createdBuyer.get().getCompanyName());

        // Verify Lead was created and associated
        List<Lead> leads = leadRepository.findByBuyerId(createdBuyer.get().getId());
        assertEquals(1, leads.size());
        Lead lead = leads.get(0);
        assertEquals("TOYS", lead.getInventoryCategory());
        assertEquals(LeadStatus.NEW, lead.getStatus());
        assertEquals(LeadSource.DIRECT, lead.getSource());
    }

    @Test
    void shouldAssociateLeadWithExistingBuyer() throws Exception {
        // Pre-create buyer
        Buyer existingBuyer = Buyer.builder()
                .firstName("Alice")
                .lastName("Brown")
                .email("alice.brown@example.com")
                .active(true)
                .build();
        existingBuyer = buyerRepository.save(existingBuyer);

        PublicLeadSubmission submission = PublicLeadSubmission.builder()
                .firstName("Alice")
                .lastName("Brown")
                .email("alice.brown@example.com")
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("NEW")
                .requestedQuantity(10)
                .budget(new BigDecimal("1200.50"))
                .build();

        mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify no new buyer was created
        long buyerCount = buyerRepository.count();
        assertEquals(1, buyerCount);

        // Verify lead is associated with existing buyer
        List<Lead> leads = leadRepository.findByBuyerId(existingBuyer.getId());
        assertEquals(1, leads.size());
        assertEquals("ELECTRONICS", leads.get(0).getInventoryCategory());
    }

    @Test
    void shouldRejectSpamSubmissionFilledHoneypot() throws Exception {
        PublicLeadSubmission submission = PublicLeadSubmission.builder()
                .firstName("Spam")
                .lastName("Bot")
                .email("bot@spam.com")
                .inventoryCategory("STUFF")
                .inventoryCondition("BAD")
                .faxNumber("SOME_SPAM_VALUE") // Honeypot filled
                .build();

        mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Spam submission detected"));
    }

    @Test
    void shouldRejectInvalidSubmissionMissingRequiredFields() throws Exception {
        PublicLeadSubmission submission = PublicLeadSubmission.builder()
                // missing required fields
                .email("invalid-email")
                .build();

        mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submission)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldTriggerRateLimitingWhenLimitExceeded() throws Exception {
        PublicLeadSubmission submission = PublicLeadSubmission.builder()
                .firstName("Rate")
                .lastName("Limit")
                .email("rate.limit@example.com")
                .inventoryCategory("TEST")
                .inventoryCondition("TEST")
                .build();

        String body = objectMapper.writeValueAsString(submission);

        // First 10 requests allowed under test config (default limit = 10)
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/api/public/leads")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk());
        }

        // 11th request must be rate-limited
        mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
