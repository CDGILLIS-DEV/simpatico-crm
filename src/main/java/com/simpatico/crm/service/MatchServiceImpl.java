package com.simpatico.crm.service;

import com.simpatico.crm.dto.MatchCreateRequest;
import com.simpatico.crm.dto.MatchResponse;
import com.simpatico.crm.dto.MatchStatusUpdateRequest;
import com.simpatico.crm.entity.*;
import com.simpatico.crm.exception.DuplicateResourceException;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.mapper.MatchMapper;
import com.simpatico.crm.repository.InventoryRepository;
import com.simpatico.crm.repository.LeadRepository;
import com.simpatico.crm.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation managing Lead-to-Inventory Match logic, states, and algorithm runs.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MatchServiceImpl implements MatchService {

    private final MatchRepository matchRepository;
    private final LeadRepository leadRepository;
    private final InventoryRepository inventoryRepository;
    private final MatchMapper matchMapper;
    private final MatchEngine matchEngine;

    @Override
    public MatchResponse createMatch(MatchCreateRequest request) {
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead with ID '" + request.getLeadId() + "' not found"));

        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item with ID '" + request.getInventoryId() + "' not found"));

        matchRepository.findByLeadIdAndInventoryId(lead.getId(), inventory.getId()).ifPresent(m -> {
            throw new DuplicateResourceException("Match record between lead '" + lead.getId() + "' and inventory '" + inventory.getId() + "' already exists");
        });

        MatchRecord matchRecord = MatchRecord.builder()
                .lead(lead)
                .inventory(inventory)
                .status(MatchStatus.INITIAL)
                .notes(request.getNotes())
                .build();

        MatchRecord saved = matchRepository.save(matchRecord);
        return matchMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchResponse getMatchById(UUID id) {
        MatchRecord matchRecord = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match record with ID '" + id + "' not found"));
        return matchMapper.toResponse(matchRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MatchResponse> listMatches(Pageable pageable) {
        return matchRepository.findAll(pageable).map(matchMapper::toResponse);
    }

    @Override
    public MatchResponse updateMatchStatus(UUID id, MatchStatusUpdateRequest request) {
        MatchRecord matchRecord = matchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Match record with ID '" + id + "' not found"));

        matchRecord.setStatus(request.getStatus());
        if (request.getNotes() != null) {
            matchRecord.setNotes(request.getNotes());
        }

        MatchRecord updated = matchRepository.save(matchRecord);
        return matchMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatchesForLead(UUID leadId) {
        if (!leadRepository.existsById(leadId)) {
            throw new ResourceNotFoundException("Lead with ID '" + leadId + "' not found");
        }
        return matchRepository.findByLeadId(leadId).stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchResponse> generatePotentialMatches(UUID leadId) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead with ID '" + leadId + "' not found"));

        if (lead.getInventoryCategory() == null || lead.getInventoryCondition() == null) {
            return new ArrayList<>();
        }

        // Efficient pre-filtering: query AVAILABLE inventory matching category & condition
        Specification<Inventory> spec = (root, query, cb) -> cb.and(
                cb.equal(cb.lower(root.get("category")), lead.getInventoryCategory().toLowerCase()),
                cb.equal(cb.lower(root.get("condition")), lead.getInventoryCondition().toLowerCase()),
                cb.equal(root.get("availabilityStatus"), InventoryStatus.AVAILABLE)
        );

        List<Inventory> candidateInventory = inventoryRepository.findAll(spec);
        List<MatchRecord> generatedMatches = new ArrayList<>();

        for (Inventory inventory : candidateInventory) {
            if (matchEngine.isMatch(lead, inventory)) {
                // Verify we do not insert duplicate match records
                if (matchRepository.findByLeadIdAndInventoryId(leadId, inventory.getId()).isEmpty()) {
                    MatchRecord potentialMatch = MatchRecord.builder()
                            .lead(lead)
                            .inventory(inventory)
                            .status(MatchStatus.INITIAL)
                            .notes("Auto-generated potential match by Rule-Based Match Engine.")
                            .build();
                    generatedMatches.add(matchRepository.save(potentialMatch));
                }
            }
        }

        return generatedMatches.stream()
                .map(matchMapper::toResponse)
                .collect(Collectors.toList());
    }
}
