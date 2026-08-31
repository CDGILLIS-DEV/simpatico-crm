package com.simpatico.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.InventoryCreateRequest;
import com.simpatico.crm.dto.InventoryResponse;
import com.simpatico.crm.dto.InventorySearchCriteria;
import com.simpatico.crm.dto.InventoryUpdateRequest;
import com.simpatico.crm.dto.SupplierBriefResponse;
import com.simpatico.crm.entity.InventoryStatus;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.service.InventoryService;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class InventoryControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventoryService inventoryService;

    @Test
    void shouldCreateInventorySuccessfully() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        InventoryCreateRequest request = InventoryCreateRequest.builder()
                .supplierId(supplierId)
                .title("Bulk Returns Pallet")
                .category("ELECTRONICS")
                .condition("RETURNS")
                .quantity(12)
                .unitType("PALLETS")
                .askingPrice(new BigDecimal("1500.00"))
                .location("Warehouse A")
                .build();

        SupplierBriefResponse supplierBrief = new SupplierBriefResponse(supplierId, "Suppliers Inc", null, "info@supp.com", null);
        InventoryResponse response = new InventoryResponse(
                inventoryId, supplierBrief, "Bulk Returns Pallet", "ELECTRONICS", "RETURNS",
                null, 12, "PALLETS", new BigDecimal("1500.00"), "Warehouse A",
                InventoryStatus.AVAILABLE, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(inventoryService.createInventory(any(InventoryCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(inventoryId.toString()))
                .andExpect(jsonPath("$.supplier.id").value(supplierId.toString()))
                .andExpect(jsonPath("$.askingPrice").value(1500.00))
                .andExpect(jsonPath("$.availabilityStatus").value("AVAILABLE"));
    }

    @Test
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        UUID supplierId = UUID.randomUUID();
        InventoryCreateRequest request = InventoryCreateRequest.builder()
                .supplierId(supplierId)
                .title("Returns Pallet")
                .category("CLOTHING")
                .condition("RETURNS")
                .quantity(5)
                .unitType("PALLETS")
                .askingPrice(new BigDecimal("500.00"))
                .build();

        when(inventoryService.createInventory(any(InventoryCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Supplier with ID '" + supplierId + "' not found"));

        mockMvc.perform(post("/api/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturnBadRequestWhenInventoryValidationFails() throws Exception {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
                // negative quantity and price
                .quantity(-5)
                .askingPrice(new BigDecimal("-100.00"))
                .build();

        mockMvc.perform(post("/api/inventories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldGetInventoryById() throws Exception {
        UUID id = UUID.randomUUID();
        InventoryResponse response = new InventoryResponse(
                id, null, "Office Chairs Pallet", "FURNITURE", "NEW",
                null, 1, "PALLET", new BigDecimal("750.00"), "Chicago",
                InventoryStatus.AVAILABLE, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(inventoryService.getInventoryById(id)).thenReturn(response);

        mockMvc.perform(get("/api/inventories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("Office Chairs Pallet"));
    }

    @Test
    void shouldSearchInventoriesWithFilters() throws Exception {
        UUID id = UUID.randomUUID();
        InventoryResponse response = new InventoryResponse(
                id, null, "Office Chairs Pallet", "FURNITURE", "NEW",
                null, 1, "PALLET", new BigDecimal("750.00"), "Chicago",
                InventoryStatus.AVAILABLE, OffsetDateTime.now(), OffsetDateTime.now()
        );

        Page<InventoryResponse> page = new PageImpl<>(Collections.singletonList(response), PageRequest.of(0, 10), 1);

        when(inventoryService.searchInventories(any(InventorySearchCriteria.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/inventories?category=FURNITURE&minPrice=500&maxPrice=1000&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldUpdateInventorySuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        InventoryUpdateRequest request = InventoryUpdateRequest.builder()
                .title("Updated Chairs")
                .category("FURNITURE")
                .condition("NEW")
                .quantity(3)
                .unitType("PALLET")
                .askingPrice(new BigDecimal("900.00"))
                .availabilityStatus(InventoryStatus.RESERVED)
                .build();

        InventoryResponse response = new InventoryResponse(
                id, null, "Updated Chairs", "FURNITURE", "NEW",
                null, 3, "PALLET", new BigDecimal("900.00"), null,
                InventoryStatus.RESERVED, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(inventoryService.updateInventory(eq(id), any(InventoryUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/inventories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Chairs"))
                .andExpect(jsonPath("$.availabilityStatus").value("RESERVED"));
    }

    @Test
    void shouldDeactivateInventorySuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/inventories/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnBadRequestForInvalidInventoryStatus() throws Exception {
        String badRequestBody = "{\"availabilityStatus\":\"INVALID_STATUS\"}";

        mockMvc.perform(put("/api/inventories/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(badRequestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
