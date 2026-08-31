package com.simpatico.crm.service;

import com.simpatico.crm.dto.LeadCreateRequest;
import com.simpatico.crm.dto.LeadResponse;
import com.simpatico.crm.dto.LeadSearchCriteria;
import com.simpatico.crm.dto.LeadUpdateRequest;
import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadStatus;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.mapper.LeadMapper;
import com.simpatico.crm.repository.BuyerRepository;
import com.simpatico.crm.repository.LeadRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for managing Lead business operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final BuyerRepository buyerRepository;
    private final LeadMapper leadMapper;

    @Override
    public LeadResponse createLead(LeadCreateRequest request) {
        Buyer buyer = buyerRepository.findById(request.getBuyerId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer with ID '" + request.getBuyerId() + "' not found"));
        
        Lead lead = leadMapper.toEntity(request, buyer);
        Lead saved = leadRepository.save(lead);
        return leadMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(UUID id) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead with ID '" + id + "' not found"));
        return leadMapper.toResponse(lead);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadResponse> searchLeads(LeadSearchCriteria criteria, Pageable pageable) {
        Specification<Lead> spec = buildSpecification(criteria);
        return leadRepository.findAll(spec, pageable).map(leadMapper::toResponse);
    }

    @Override
    public LeadResponse updateLead(UUID id, LeadUpdateRequest request) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead with ID '" + id + "' not found"));
        
        leadMapper.updateEntity(request, lead);
        Lead updated = leadRepository.save(lead);
        return leadMapper.toResponse(updated);
    }

    @Override
    public LeadResponse updateLeadStatus(UUID id, LeadStatus status) {
        Lead lead = leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead with ID '" + id + "' not found"));
        
        lead.setStatus(status);
        Lead updated = leadRepository.save(lead);
        return leadMapper.toResponse(updated);
    }

    /**
     * Builds a dynamic JPA Specification based on search criteria parameters.
     */
    private Specification<Lead> buildSpecification(LeadSearchCriteria criteria) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("buyer", jakarta.persistence.criteria.JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
            }
            if (criteria.getSource() != null) {
                predicates.add(cb.equal(root.get("source"), criteria.getSource()));
            }
            if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("inventoryCategory")), "%" + criteria.getCategory().toLowerCase() + "%"));
            }
            if (criteria.getBuyerId() != null) {
                predicates.add(cb.equal(root.get("buyer").get("id"), criteria.getBuyerId()));
            }
            if (criteria.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
            }
            if (criteria.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
