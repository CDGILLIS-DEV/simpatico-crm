package com.simpatico.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.BuyerCreateRequest;
import com.simpatico.crm.dto.BuyerResponse;
import com.simpatico.crm.dto.BuyerUpdateRequest;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.service.BuyerService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * REST controller unit tests for verifying validation, response statuses,
 * mapping pathways, logical deactivations, and pagination controls.
 */
@WebMvcTest(BuyerController.class)
@ActiveProfiles("test")
class BuyerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BuyerService buyerService;

    @Test
    void shouldCreateBuyerSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("123-456-7890")
                .companyName("Test Corp")
                .build();

        BuyerResponse response = new BuyerResponse(
                id, "John", "Doe", "Test Corp", "john.doe@example.com", "123-456-7890",
                null, null, null, null, null, "USA", true, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(buyerService.createBuyer(any(BuyerCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/buyers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email") // invalid format
                .build();

        mockMvc.perform(post("/api/buyers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("Email must be a valid email address"));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .email("john.doe@example.com")
                // Missing required firstName and lastName
                .build();

        mockMvc.perform(post("/api/buyers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    void shouldGetBuyerByIdSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        BuyerResponse response = new BuyerResponse(
                id, "John", "Doe", "Test Corp", "john.doe@example.com", "123-456-7890",
                null, null, null, null, null, "USA", true, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(buyerService.getBuyerById(id)).thenReturn(response);

        mockMvc.perform(get("/api/buyers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    void shouldReturnNotFoundWhenBuyerDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(buyerService.getBuyerById(id)).thenThrow(new ResourceNotFoundException("Buyer not found"));

        mockMvc.perform(get("/api/buyers/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Buyer not found"));
    }

    @Test
    void shouldUpdateBuyerSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        BuyerUpdateRequest request = BuyerUpdateRequest.builder()
                .firstName("Johnny")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        BuyerResponse response = new BuyerResponse(
                id, "Johnny", "Doe", null, "john.doe@example.com", null,
                null, null, null, null, null, "USA", true, OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(buyerService.updateBuyer(eq(id), any(BuyerUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/buyers/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"));
    }

    @Test
    void shouldDeactivateBuyerSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/buyers/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(buyerService).deactivateBuyer(id);
    }

    @Test
    void shouldListBuyersWithPagination() throws Exception {
        UUID id = UUID.randomUUID();
        BuyerResponse response = new BuyerResponse(
                id, "John", "Doe", null, "john.doe@example.com", null,
                null, null, null, null, null, "USA", true, OffsetDateTime.now(), OffsetDateTime.now()
        );

        Page<BuyerResponse> page = new PageImpl<>(Collections.singletonList(response), PageRequest.of(0, 10), 1);

        when(buyerService.getAllBuyers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/buyers?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.size").value(10));
    }
}
