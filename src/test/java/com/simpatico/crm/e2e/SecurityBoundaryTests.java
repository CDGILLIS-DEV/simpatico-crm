package com.simpatico.crm.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.BuyerCreateRequest;
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

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests verifying role-based access control, security boundaries,
 * and authentication enforcement on public vs administrative endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityBoundaryTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Security: Unauthenticated public request to administrative /api/buyers redirects to login (302 Redirect)")
    void testUnauthenticatedAccessToBuyersDenied() throws Exception {
        mockMvc.perform(get("/api/buyers"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Security: Unauthenticated public request to administrative /api/leads redirects to login")
    void testUnauthenticatedAccessToLeadsDenied() throws Exception {
        mockMvc.perform(get("/api/leads"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Security: Unauthenticated public request to administrative /api/suppliers redirects to login")
    void testUnauthenticatedAccessToSuppliersDenied() throws Exception {
        mockMvc.perform(get("/api/suppliers"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Security: Unauthenticated public request to administrative /api/inventories redirects to login")
    void testUnauthenticatedAccessToInventoriesDenied() throws Exception {
        mockMvc.perform(get("/api/inventories"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Security: Unauthenticated public request to administrative /api/matches redirects to login")
    void testUnauthenticatedAccessToMatchesDenied() throws Exception {
        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Security: Unauthenticated public POST request to create buyer redirects to login")
    void testUnauthenticatedBuyerCreationDenied() throws Exception {
        BuyerCreateRequest request = BuyerCreateRequest.builder()
                .firstName("Hacker")
                .lastName("User")
                .email("hacker@malicious.com")
                .build();

        mockMvc.perform(post("/api/buyers").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @DisplayName("Security: Public landing page API /api/public/leads is allowed without authentication")
    void testPublicLeadEndpointAllowed() throws Exception {
        String publicJson = """
                {
                    "firstName": "Public",
                    "lastName": "Visitor",
                    "email": "public.visitor@example.com",
                    "inventoryCategory": "BOOKS",
                    "inventoryCondition": "NEW"
                }
                """;

        mockMvc.perform(post("/api/public/leads")
                .contentType(MediaType.APPLICATION_JSON)
                .content(publicJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("Security: Public health check /api/health is allowed without authentication")
    void testPublicHealthEndpointAllowed() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Security: Admin user with ROLE_ADMIN is granted access to administrative endpoints")
    @WithMockUser(roles = "ADMIN")
    void testAdminRoleGrantedAccess() throws Exception {
        mockMvc.perform(get("/api/buyers"))
                .andExpect(status().isOk());
    }
}
