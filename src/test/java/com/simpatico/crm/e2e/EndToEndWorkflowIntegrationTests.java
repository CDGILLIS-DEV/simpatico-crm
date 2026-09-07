package com.simpatico.crm.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.*;
import com.simpatico.crm.entity.*;
import com.simpatico.crm.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-End Integration Tests validating the full business workflow:
 * Buyer, Lead, Supplier, Inventory, Match, and Public Lead Form submission.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EndToEndWorkflowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private com.simpatico.crm.config.RateLimitingFilter rateLimitingFilter;

    @BeforeEach
    void setUp() {
        rateLimitingFilter.clearRateLimits();
        matchRepository.deleteAll();
        inventoryRepository.deleteAll();
        supplierRepository.deleteAll();
        leadRepository.deleteAll();
        buyerRepository.deleteAll();
    }

    // ==========================================
    // 1. BUYER LIFECYCLE TESTS
    // ==========================================

    @Test
    @DisplayName("Buyer Lifecycle: Create -> Retrieve -> Update -> Deactivate -> Nonexistent")
    @WithMockUser(roles = "ADMIN")
    void testBuyerLifecycle() throws Exception {
        // 1. Create Buyer
        BuyerCreateRequest createReq = BuyerCreateRequest.builder()
                .firstName("Alice")
                .lastName("Walker")
                .companyName("Walker Trading Co")
                .email("alice.walker@example.com")
                .phone("+15559876543")
                .city("Dallas")
                .state("TX")
                .zipCode("75201")
                .country("USA")
                .build();

        String createResContent = mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Walker"))
                .andExpect(jsonPath("$.email").value("alice.walker@example.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        BuyerResponse buyerResp = objectMapper.readValue(createResContent, BuyerResponse.class);
        UUID buyerId = buyerResp.id();

        // 2. Retrieve Buyer
        mockMvc.perform(get("/api/buyers/{id}", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(buyerId.toString()))
                .andExpect(jsonPath("$.companyName").value("Walker Trading Co"));

        // 3. Update Buyer
        BuyerUpdateRequest updateReq = BuyerUpdateRequest.builder()
                .firstName("Alice M.")
                .lastName("Walker-Smith")
                .email("alice.walker@example.com")
                .companyName("Walker Global")
                .build();

        mockMvc.perform(put("/api/buyers/{id}", buyerId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice M."))
                .andExpect(jsonPath("$.lastName").value("Walker-Smith"))
                .andExpect(jsonPath("$.companyName").value("Walker Global"));

        // 4. Deactivate Buyer
        mockMvc.perform(delete("/api/buyers/{id}", buyerId).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/buyers/{id}", buyerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        // 5. Retrieve Nonexistent Buyer
        UUID randomId = UUID.randomUUID();
        mockMvc.perform(get("/api/buyers/{id}", randomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("not found")));
    }

    // ==========================================
    // 2. LEAD LIFECYCLE TESTS
    // ==========================================

    @Test
    @DisplayName("Lead Lifecycle: Create -> Associate -> Retrieve -> Update -> Status Change -> Filter & Paginate -> Nonexistent")
    @WithMockUser(roles = "ADMIN")
    void testLeadLifecycle() throws Exception {
        // Create parent buyer
        Buyer buyer = buyerRepository.save(Buyer.builder()
                .firstName("Bob")
                .lastName("Jones")
                .email("bob.jones@example.com")
                .active(true)
                .build());

        // 1. Create Lead associated with Buyer
        LeadCreateRequest createLeadReq = LeadCreateRequest.builder()
                .buyerId(buyer.getId())
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(200)
                .budget(new BigDecimal("15000.00"))
                .preferredGeographicArea("Texas")
                .purchaseFrequency("MONTHLY")
                .additionalRequirements("Include smartphones")
                .source(LeadSource.ORGANIC_SEARCH)
                .build();

        String leadResContent = mockMvc.perform(post("/api/leads").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createLeadReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.buyer.id").value(buyer.getId().toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.source").value("ORGANIC_SEARCH"))
                .andReturn().getResponse().getContentAsString();

        LeadResponse leadResp = objectMapper.readValue(leadResContent, LeadResponse.class);
        UUID leadId = leadResp.id();

        // 2. Retrieve Lead
        mockMvc.perform(get("/api/leads/{id}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(leadId.toString()))
                .andExpect(jsonPath("$.inventoryCategory").value("ELECTRONICS"));

        // 3. Update Lead
        LeadUpdateRequest updateLeadReq = LeadUpdateRequest.builder()
                .inventoryCategory("CONSUMER_ELECTRONICS")
                .inventoryCondition("REFURBISHED")
                .requestedQuantity(250)
                .budget(new BigDecimal("18000.00"))
                .status(LeadStatus.CONTACTED)
                .source(LeadSource.ORGANIC_SEARCH)
                .build();

        mockMvc.perform(put("/api/leads/{id}", leadId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateLeadReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryCategory").value("CONSUMER_ELECTRONICS"))
                .andExpect(jsonPath("$.inventoryCondition").value("REFURBISHED"))
                .andExpect(jsonPath("$.status").value("CONTACTED"));

        // 4. Change Lead Status
        LeadStatusUpdateRequest statusReq = new LeadStatusUpdateRequest(LeadStatus.QUALIFIED);
        mockMvc.perform(patch("/api/leads/{id}/status", leadId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUALIFIED"));

        // 5. Filter & Paginate Leads
        mockMvc.perform(get("/api/leads?status=QUALIFIED&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(leadId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 6. Retrieve Nonexistent Lead
        UUID randomLeadId = UUID.randomUUID();
        mockMvc.perform(get("/api/leads/{id}", randomLeadId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ==========================================
    // 3. SUPPLIER LIFECYCLE TESTS
    // ==========================================

    @Test
    @DisplayName("Supplier Lifecycle: Create -> Retrieve -> Update -> Deactivate")
    @WithMockUser(roles = "ADMIN")
    void testSupplierLifecycle() throws Exception {
        // 1. Create Supplier
        SupplierCreateRequest supplierReq = SupplierCreateRequest.builder()
                .companyName("Tech Liquidators LLC")
                .contactName("Sarah Connor")
                .email("sarah@techliquidators.com")
                .phone("+15553334444")
                .city("Austin")
                .state("TX")
                .country("USA")
                .website("https://techliquidators.com")
                .status(SupplierStatus.ACTIVE)
                .notes("Primary electronics vendor")
                .build();

        String resContent = mockMvc.perform(post("/api/suppliers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(supplierReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.companyName").value("Tech Liquidators LLC"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();

        SupplierResponse supplierResp = objectMapper.readValue(resContent, SupplierResponse.class);
        UUID supplierId = supplierResp.id();

        // 2. Retrieve Supplier
        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactName").value("Sarah Connor"));

        // 3. Update Supplier
        SupplierUpdateRequest updateSupplierReq = SupplierUpdateRequest.builder()
                .companyName("Tech Liquidators Global")
                .contactName("Sarah Connor")
                .email("sarah@techliquidators.com")
                .status(SupplierStatus.ACTIVE)
                .build();

        mockMvc.perform(put("/api/suppliers/{id}", supplierId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateSupplierReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Tech Liquidators Global"));

        // 4. Deactivate Supplier
        mockMvc.perform(delete("/api/suppliers/{id}", supplierId).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    // ==========================================
    // 4. INVENTORY LIFECYCLE TESTS
    // ==========================================

    @Test
    @DisplayName("Inventory Lifecycle: Create -> Associate -> Retrieve -> Update -> Change Availability -> Filter")
    @WithMockUser(roles = "ADMIN")
    void testInventoryLifecycle() throws Exception {
        // Create supplier
        Supplier supplier = supplierRepository.save(Supplier.builder()
                .companyName("Apex Surplus")
                .email("apex@surplus.com")
                .status(SupplierStatus.ACTIVE)
                .build());

        // 1. Create Inventory associated with Supplier
        InventoryCreateRequest invReq = InventoryCreateRequest.builder()
                .supplierId(supplier.getId())
                .title("Bulk Laptops Lot 50")
                .category("ELECTRONICS")
                .condition("RETURNS")
                .description("50 mixed brand customer return laptops")
                .quantity(50)
                .unitType("UNITS")
                .askingPrice(new BigDecimal("7500.00"))
                .location("Warehouse A, TX")
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        String invResContent = mockMvc.perform(post("/api/inventories").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.supplier.id").value(supplier.getId().toString()))
                .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"))
                .andReturn().getResponse().getContentAsString();

        InventoryResponse invResp = objectMapper.readValue(invResContent, InventoryResponse.class);
        UUID invId = invResp.id();

        // 2. Retrieve Inventory
        mockMvc.perform(get("/api/inventories/{id}", invId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bulk Laptops Lot 50"));

        // 3. Update Inventory
        InventoryUpdateRequest updateInvReq = InventoryUpdateRequest.builder()
                .title("Bulk Laptops Lot 50 - Updated")
                .category("ELECTRONICS")
                .condition("RETURNS")
                .quantity(45)
                .unitType("UNITS")
                .askingPrice(new BigDecimal("7000.00"))
                .availabilityStatus(InventoryStatus.RESERVED)
                .build();

        mockMvc.perform(put("/api/inventories/{id}", invId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateInvReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(45))
                .andExpect(jsonPath("$.availabilityStatus").value("RESERVED"));

        // 4. Change Availability / Deactivate
        mockMvc.perform(delete("/api/inventories/{id}", invId).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/inventories/{id}", invId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availabilityStatus").value("INACTIVE"));

        // 5. Filter Inventory
        mockMvc.perform(get("/api/inventories?category=ELECTRONICS&availability=INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ==========================================
    // 5. MATCHING LIFECYCLE TESTS
    // ==========================================

    @Test
    @DisplayName("Matching Lifecycle: Create Potential Match -> Retrieve -> Update Status -> Retrieve Lead Matches")
    @WithMockUser(roles = "ADMIN")
    void testMatchingLifecycle() throws Exception {
        Buyer buyer = buyerRepository.save(Buyer.builder()
                .firstName("David")
                .lastName("Miller")
                .email("david@miller.com")
                .build());

        Lead lead = leadRepository.save(Lead.builder()
                .buyer(buyer)
                .inventoryCategory("TOYS")
                .inventoryCondition("NEW")
                .requestedQuantity(100)
                .budget(new BigDecimal("2000.00"))
                .status(LeadStatus.QUALIFIED)
                .source(LeadSource.DIRECT)
                .build());

        Supplier supplier = supplierRepository.save(Supplier.builder()
                .companyName("Toy World Wholesalers")
                .email("contact@toyworld.com")
                .status(SupplierStatus.ACTIVE)
                .build());

        Inventory inventory = inventoryRepository.save(Inventory.builder()
                .supplier(supplier)
                .title("Pallet of Action Figures")
                .category("TOYS")
                .condition("NEW")
                .quantity(100)
                .unitType("UNITS")
                .askingPrice(new BigDecimal("1800.00"))
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build());

        // 1. Create Potential Match
        MatchCreateRequest matchReq = MatchCreateRequest.builder()
                .leadId(lead.getId())
                .inventoryId(inventory.getId())
                .notes("Excellent budget alignment")
                .build();

        String matchResContent = mockMvc.perform(post("/api/matches").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(matchReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.status").value("INITIAL"))
                .andReturn().getResponse().getContentAsString();

        MatchResponse matchResp = objectMapper.readValue(matchResContent, MatchResponse.class);
        UUID matchId = matchResp.id();

        // 2. Retrieve Match by ID
        mockMvc.perform(get("/api/matches/{id}", matchId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matchId.toString()));

        // 3. Update Match Status
        MatchStatusUpdateRequest statusUpdate = new MatchStatusUpdateRequest(MatchStatus.PRESENTED, "Presented proposal to buyer");
        mockMvc.perform(patch("/api/matches/{id}/status", matchId).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESENTED"))
                .andExpect(jsonPath("$.notes").value("Presented proposal to buyer"));

        // 4. Retrieve Matches for Lead
        mockMvc.perform(get("/api/leads/{leadId}/matches", lead.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(matchId.toString()));
    }

    // ==========================================
    // 6. PUBLIC LEAD WORKFLOW TESTS
    // ==========================================

    @Test
    @DisplayName("Public Lead Workflow: Public Submission -> New Buyer & Lead Created -> Second Submission Reuses Buyer")
    void testPublicLeadWorkflow() throws Exception {
        PublicLeadSubmission firstSubmission = PublicLeadSubmission.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .companyName("Peanuts Wholesale")
                .email("charlie.brown@peanuts.com")
                .phone("+15552221111")
                .city("Minneapolis")
                .state("MN")
                .country("USA")
                .inventoryCategory("APPAREL")
                .inventoryCondition("OVERSTOCK")
                .requestedQuantity(1000)
                .budget(new BigDecimal("25000.00"))
                .purchaseFrequency("QUARTERLY")
                .additionalRequirements("Summer apparel only")
                .source(LeadSource.DIRECT)
                .build();

        // 1. Submit public lead form for new buyer
        String firstResponseContent = mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.leadId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PublicLeadResponse firstPublicResp = objectMapper.readValue(firstResponseContent, PublicLeadResponse.class);
        UUID lead1Id = firstPublicResp.leadId();

        // 2. Verify Buyer & Lead created in database with expected parameters & relationships
        Optional<Buyer> createdBuyerOpt = buyerRepository.findByEmail("charlie.brown@peanuts.com");
        assertTrue(createdBuyerOpt.isPresent(), "Buyer should be created in DB");
        Buyer createdBuyer = createdBuyerOpt.get();
        assertEquals("Charlie", createdBuyer.getFirstName());
        assertEquals("Brown", createdBuyer.getLastName());
        assertEquals("Peanuts Wholesale", createdBuyer.getCompanyName());

        Optional<Lead> createdLead1Opt = leadRepository.findById(lead1Id);
        assertTrue(createdLead1Opt.isPresent(), "Lead 1 should be created in DB");
        Lead lead1 = createdLead1Opt.get();
        assertEquals(createdBuyer.getId(), lead1.getBuyer().getId());
        assertEquals(LeadStatus.NEW, lead1.getStatus());
        assertEquals(LeadSource.DIRECT, lead1.getSource());
        assertEquals("APPAREL", lead1.getInventoryCategory());

        // 3. Submit second lead form for the same buyer email
        PublicLeadSubmission secondSubmission = PublicLeadSubmission.builder()
                .firstName("Charlie")
                .lastName("Brown")
                .companyName("Peanuts Wholesale")
                .email("charlie.brown@peanuts.com") // Same buyer email
                .phone("+15552221111")
                .city("Minneapolis")
                .state("MN")
                .inventoryCategory("HOME_GOODS")
                .inventoryCondition("NEW")
                .requestedQuantity(500)
                .budget(new BigDecimal("10000.00"))
                .purchaseFrequency("MONTHLY")
                .build();

        String secondResponseContent = mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(secondSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.leadId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        PublicLeadResponse secondPublicResp = objectMapper.readValue(secondResponseContent, PublicLeadResponse.class);
        UUID lead2Id = secondPublicResp.leadId();
        assertNotEquals(lead1Id, lead2Id, "Second lead must have a distinct ID");

        // 4. Verify duplicate buyer was NOT created, and both leads belong to the same buyer
        long buyerCount = buyerRepository.count();
        assertEquals(1, buyerCount, "Buyer count should remain 1");

        List<Lead> buyerLeads = leadRepository.findByBuyerId(createdBuyer.getId());
        assertEquals(2, buyerLeads.size(), "Buyer should have exactly 2 associated leads");
        assertThat(buyerLeads).extracting(Lead::getId).containsExactlyInAnyOrder(lead1Id, lead2Id);
    }
}
