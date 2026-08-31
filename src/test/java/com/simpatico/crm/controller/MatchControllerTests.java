package com.simpatico.crm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simpatico.crm.dto.MatchCreateRequest;
import com.simpatico.crm.dto.MatchResponse;
import com.simpatico.crm.dto.MatchStatusUpdateRequest;
import com.simpatico.crm.entity.MatchStatus;
import com.simpatico.crm.exception.DuplicateResourceException;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.service.MatchService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MatchController.class)
@ActiveProfiles("test")
class MatchControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MatchService matchService;

    @Test
    void shouldCreateMatchSuccessfully() throws Exception {
        UUID matchId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();

        MatchCreateRequest request = MatchCreateRequest.builder()
                .leadId(leadId)
                .inventoryId(inventoryId)
                .notes("Manual review match.")
                .build();

        MatchResponse response = new MatchResponse(
                matchId, null, null, MatchStatus.INITIAL, "Manual review match.",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(matchService.createMatch(any(MatchCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(matchId.toString()))
                .andExpect(jsonPath("$.status").value("INITIAL"));
    }

    @Test
    void shouldReturnConflictOnDuplicateMatchCreation() throws Exception {
        UUID leadId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();

        MatchCreateRequest request = MatchCreateRequest.builder()
                .leadId(leadId)
                .inventoryId(inventoryId)
                .build();

        when(matchService.createMatch(any(MatchCreateRequest.class)))
                .thenThrow(new DuplicateResourceException("Match record already exists"));

        mockMvc.perform(post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldReturnBadRequestWhenCreateFieldsAreMissing() throws Exception {
        MatchCreateRequest request = MatchCreateRequest.builder().build();

        mockMvc.perform(post("/api/matches")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void shouldGetMatchById() throws Exception {
        UUID id = UUID.randomUUID();
        MatchResponse response = new MatchResponse(
                id, null, null, MatchStatus.REVIEWED, "Reviewed notes.",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(matchService.getMatchById(id)).thenReturn(response);

        mockMvc.perform(get("/api/matches/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("REVIEWED"));
    }

    @Test
    void shouldReturnNotFoundWhenMatchDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(matchService.getMatchById(id)).thenThrow(new ResourceNotFoundException("Match record not found"));

        mockMvc.perform(get("/api/matches/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldUpdateMatchStatusSuccessfully() throws Exception {
        UUID id = UUID.randomUUID();
        MatchStatusUpdateRequest request = MatchStatusUpdateRequest.builder()
                .status(MatchStatus.PRESENTED)
                .notes("Sent proposal.")
                .build();

        MatchResponse response = new MatchResponse(
                id, null, null, MatchStatus.PRESENTED, "Sent proposal.",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(matchService.updateMatchStatus(eq(id), any(MatchStatusUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(patch("/api/matches/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESENTED"))
                .andExpect(jsonPath("$.notes").value("Sent proposal."));
    }

    @Test
    void shouldGetMatchesForLead() throws Exception {
        UUID leadId = UUID.randomUUID();
        MatchResponse response = new MatchResponse(
                UUID.randomUUID(), null, null, MatchStatus.INITIAL, null,
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(matchService.getMatchesForLead(leadId)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(get("/api/leads/{leadId}/matches", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("INITIAL"));
    }

    @Test
    void shouldGeneratePotentialMatches() throws Exception {
        UUID leadId = UUID.randomUUID();
        MatchResponse response = new MatchResponse(
                UUID.randomUUID(), null, null, MatchStatus.INITIAL, "Auto-generated potential match by Rule-Based Match Engine.",
                OffsetDateTime.now(), OffsetDateTime.now()
        );

        when(matchService.generatePotentialMatches(leadId)).thenReturn(Collections.singletonList(response));

        mockMvc.perform(post("/api/leads/{leadId}/matches/generate", leadId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("INITIAL"))
                .andExpect(jsonPath("$[0].notes").value("Auto-generated potential match by Rule-Based Match Engine."));
    }
}
