package com.simpatico.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.SupplierCreateRequest;
import com.simpatico.crm.dto.SupplierResponse;
import com.simpatico.crm.dto.SupplierUpdateRequest;
import com.simpatico.crm.entity.SupplierStatus;
import com.simpatico.crm.exception.DuplicateResourceException;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.service.SupplierService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
@ActiveProfiles("test")
class SupplierControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SupplierService supplierService;

    @Test
    void shouldCreateSupplierSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierCreateRequest request = SupplierCreateRequest.builder()
                .companyName("Test Supplier Inc")
                .contactName("Bob Jones")
                .email("bob@supplier.com")
                .phone("+15559876543")
                .build();

        SupplierResponse response = new SupplierResponse(
                id, "Test Supplier Inc", "Bob Jones", "bob@supplier.com", "+15559876543",
                null, null, "USA", null, SupplierStatus.PENDING, null, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(supplierService.createSupplier(any(SupplierCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.companyName").value("Test Supplier Inc"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturnConflictWhenEmailExists() throws Exception {
        SupplierCreateRequest request = SupplierCreateRequest.builder()
                .companyName("Duplicate Inc")
                .email("duplicate@supplier.com")
                .build();

        when(supplierService.createSupplier(any(SupplierCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Supplier with email 'duplicate@supplier.com' already exists"));

        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Supplier with email 'duplicate@supplier.com' already exists"));
    }

    @Test
    void shouldReturnBadRequestWhenCreateFieldsAreInvalid() throws Exception {
        SupplierCreateRequest request = SupplierCreateRequest.builder()
                // company name and email missing
                .build();

        mockMvc.perform(post("/api/suppliers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldGetSupplierById() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierResponse response = new SupplierResponse(
                id, "Supplier Ltd", null, "info@supplier.ltd", null,
                null, null, "USA", null, SupplierStatus.ACTIVE, null, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(supplierService.getSupplierById(id)).thenReturn(response);

        mockMvc.perform(get("/api/suppliers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.companyName").value("Supplier Ltd"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void shouldReturnNotFoundWhenSupplierDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(supplierService.getSupplierById(id)).thenThrow(new ResourceNotFoundException("Supplier with ID '" + id + "' not found"));

        mockMvc.perform(get("/api/suppliers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldUpdateSupplierSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        SupplierUpdateRequest request = SupplierUpdateRequest.builder()
                .companyName("Supplier Updated")
                .email("updated@supplier.com")
                .status(SupplierStatus.BLOCKED)
                .build();

        SupplierResponse response = new SupplierResponse(
                id, "Supplier Updated", null, "updated@supplier.com", null,
                null, null, "USA", null, SupplierStatus.BLOCKED, null, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(supplierService.updateSupplier(eq(id), any(SupplierUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/suppliers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Supplier Updated"))
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void shouldDeactivateSupplierSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/suppliers/{id}", id))
                .andExpect(status().isNoContent());
    }
}
