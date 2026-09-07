package com.simpatico.crm.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.*;
import com.simpatico.crm.entity.LeadSource;
import com.simpatico.crm.entity.LeadStatus;
import com.simpatico.crm.entity.SupplierStatus;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests verifying request validation rules, HTTP status codes,
 * and error payload structure consistency without stack trace leaks.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ValidationAndErrorHandlingTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Validation: Reject buyer creation missing required fields (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testBuyerMissingRequiredFields() throws Exception {
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .companyName("No Name LLC") // missing firstName, lastName, email
                .build();

        mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @DisplayName("Validation: Reject invalid email format on buyer creation (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testBuyerInvalidEmailFormat() throws Exception {
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("not-an-email-address")
                .build();

        mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @Test
    @DisplayName("Validation: Reject invalid phone format (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testBuyerInvalidPhoneFormat() throws Exception {
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("abc-invalid-phone-xyz")
                .build();

        mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("phone"));
    }

    @Test
    @DisplayName("Validation: Reject excessively long string values (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testExcessivelyLongStrings() throws Exception {
        String longString = "A".repeat(150); // Exceeds max 50 chars for firstName
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .firstName(longString)
                .lastName("Doe")
                .email("john.long@example.com")
                .build();

        mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("firstName"));
    }

    @Test
    @DisplayName("Validation: Reject negative quantity and negative budget on lead creation (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testLeadNegativeQuantityAndBudget() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .buyerId(UUID.randomUUID())
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("NEW")
                .requestedQuantity(-10) // invalid negative quantity
                .budget(new BigDecimal("-500.00")) // invalid negative budget
                .source(LeadSource.DIRECT)
                .build();

        mockMvc.perform(post("/api/leads").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    @DisplayName("Validation: Reject missing buyer ID on lead creation (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testLeadMissingBuyerId() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("NEW")
                .source(LeadSource.DIRECT)
                .build();

        mockMvc.perform(post("/api/leads").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("buyerId"));
    }

    @Test
    @DisplayName("Validation: Reject invalid enum values in JSON payload (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testInvalidEnumValue() throws Exception {
        String invalidJson = """
                {
                    "companyName": "Test Supplier",
                    "email": "supplier@test.com",
                    "status": "NONEXISTENT_STATUS_ENUM"
                }
                """;

        mockMvc.perform(post("/api/suppliers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON request or invalid parameters")));
    }

    @Test
    @DisplayName("Validation: Reject malformed JSON body (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testMalformedJsonBody() throws Exception {
        String brokenJson = "{\"firstName\": \"John\", \"lastName\": }"; // syntax error

        mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(brokenJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Malformed JSON request")));
    }

    @Test
    @DisplayName("Validation: Reject malformed UUID path parameter (400 Bad Request)")
    @WithMockUser(roles = "ADMIN")
    void testMalformedUuidPathParam() throws Exception {
        mockMvc.perform(get("/api/buyers/not-a-valid-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid parameter format")));
    }

    @Test
    @DisplayName("Error Handling: Nonexistent supplier ID on inventory creation returns 404 Not Found")
    @WithMockUser(roles = "ADMIN")
    void testInventoryNonexistentSupplier() throws Exception {
        InventoryCreateRequest request = InventoryCreateRequest.builder()
                .supplierId(UUID.randomUUID()) // nonexistent supplier
                .title("Orphaned Inventory Lot")
                .category("ELECTRONICS")
                .condition("NEW")
                .quantity(10)
                .unitType("UNITS")
                .askingPrice(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/api/inventories").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Supplier with ID")));
    }

    @Test
    @DisplayName("Error Handling: Nonexistent buyer ID on lead creation returns 404 Not Found")
    @WithMockUser(roles = "ADMIN")
    void testLeadNonexistentBuyer() throws Exception {
        LeadCreateRequest request = LeadCreateRequest.builder()
                .buyerId(UUID.randomUUID()) // nonexistent buyer
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("NEW")
                .source(LeadSource.DIRECT)
                .build();

        mockMvc.perform(post("/api/leads").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Buyer with ID")));
    }

    @Test
    @DisplayName("Security & Leak Prevention: Confirm 500 error does NOT expose stack traces or internal code paths")
    @WithMockUser(roles = "ADMIN")
    void testErrorSanitizationNoStackTraceLeak() throws Exception {
        mockMvc.perform(get("/api/buyers/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.sql").doesNotExist())
                .andExpect(jsonPath("$.path").value("/api/buyers/00000000-0000-0000-0000-000000000000"));
    }
}
