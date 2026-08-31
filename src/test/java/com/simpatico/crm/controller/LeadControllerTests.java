package com.simpatico.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.*;
import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.service.LeadService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST controller unit tests verifying routing, status updates, validation error reporting,
 * and paginated queries for Lead resources.
 */
@WebMvcTest(LeadController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LeadControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LeadService leadService;

    @Test
    void shouldCreateLeadSuccessfully() throws Exception {
        UUID leadId = UUID.randomUUID();
        UUID buyerId = UUID.randomUUID();

        LeadCreateRequest request = LeadCreateRequest.builder()
                .buyerId(buyerId)
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(50)
                .budget(new BigDecimal("2500.00"))
                .source(LeadSource.GOOGLE_ADS)
                .build();

        BuyerBriefResponse buyerBrief = new BuyerBriefResponse(buyerId, "Jane", "Doe", "Acme", "jane@example.com", null);
        LeadResponse response = new LeadResponse(
                leadId, buyerBrief, "ELECTRONICS", "RETURNS", 50, new BigDecimal("2500.00"),
                null, null, null, LeadStatus.NEW, LeadSource.GOOGLE_ADS, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(leadService.createLead(any(LeadCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(leadId.toString()))
                .andExpect(jsonPath("$.buyer.id").value(buyerId.toString()))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.source").value("GOOGLE_ADS"));
    }

    @Test
    void shouldReturnNotFoundWhenBuyerDoesNotExist() throws Exception {
        UUID buyerId = UUID.randomUUID();
        LeadCreateRequest request = LeadCreateRequest.builder()
                .buyerId(buyerId)
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .source(LeadSource.GOOGLE_ADS)
                .build();

        when(leadService.createLead(any(LeadCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Buyer with ID '" + buyerId + "' not found"));

        mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Buyer with ID '" + buyerId + "' not found"));
    }

    @Test
    void shouldReturnBadRequestWhenLeadValidationFails() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                // missing required fields & negative quantity
                .requestedQuantity(-10)
                .build();

        mockMvc.perform(post("/api/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldGetLeadByIdSuccessfully() throws Exception {
        UUID leadId = UUID.randomUUID();
        LeadResponse response = new LeadResponse(
                leadId, null, "APPAREL", "NEW", 200, new BigDecimal("4500.00"),
                null, null, null, LeadStatus.CONTACTED, LeadSource.ORGANIC_SEARCH, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(leadService.getLeadById(leadId)).thenReturn(response);

        mockMvc.perform(get("/api/leads/{id}", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(leadId.toString()))
                .andExpect(jsonPath("$.inventoryCategory").value("APPAREL"))
                .andExpect(jsonPath("$.status").value("CONTACTED"));
    }

    @Test
    void shouldUpdateLeadStatusSuccessfully() throws Exception {
        UUID leadId = UUID.randomUUID();
        LeadStatusUpdateRequest request = LeadStatusUpdateRequest.builder()
                .status(LeadStatus.QUALIFIED)
                .build();

        LeadResponse response = new LeadResponse(
                leadId, null, "ELECTRONICS", "RETURNS", 50, new BigDecimal("2500.00"),
                null, null, null, LeadStatus.QUALIFIED, LeadSource.GOOGLE_ADS, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(leadService.updateLeadStatus(eq(leadId), eq(LeadStatus.QUALIFIED))).thenReturn(response);

        mockMvc.perform(patch("/api/leads/{id}/status", leadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUALIFIED"));
    }

    @Test
    void shouldUpdateLeadSuccessfully() throws Exception {
        UUID leadId = UUID.randomUUID();
        LeadUpdateRequest request = LeadUpdateRequest.builder()
                .inventoryCategory("APPAREL")
                .inventoryCondition("SHELF_PULLS")
                .requestedQuantity(150)
                .budget(new BigDecimal("3000.00"))
                .status(LeadStatus.MATCHING)
                .source(LeadSource.DIRECT)
                .build();

        LeadResponse response = new LeadResponse(
                leadId, null, "APPAREL", "SHELF_PULLS", 150, new BigDecimal("3000.00"),
                null, null, null, LeadStatus.MATCHING, LeadSource.DIRECT, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(leadService.updateLead(eq(leadId), any(LeadUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/leads/{id}", leadId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inventoryCategory").value("APPAREL"))
                .andExpect(jsonPath("$.inventoryCondition").value("SHELF_PULLS"))
                .andExpect(jsonPath("$.status").value("MATCHING"));
    }

    @Test
    void shouldSearchLeadsWithFiltersAndPagination() throws Exception {
        UUID leadId = UUID.randomUUID();
        LeadResponse response = new LeadResponse(
                leadId, null, "ELECTRONICS", "RETURNS", 50, new BigDecimal("2500.00"),
                null, null, null, LeadStatus.NEW, LeadSource.GOOGLE_ADS, OffsetDateTime.now(), OffsetDateTime.now()
        );

        Page<LeadResponse> page = new PageImpl<>(Collections.singletonList(response), PageRequest.of(0, 10), 1);

        when(leadService.searchLeads(any(LeadSearchCriteria.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/leads?status=NEW&category=ELECTRONICS&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(leadId.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnBadRequestForInvalidStatus() throws Exception {
        String badRequestBody = "{\"status\":\"INVALID_STATUS\"}";

        mockMvc.perform(patch("/api/leads/{id}/status", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
