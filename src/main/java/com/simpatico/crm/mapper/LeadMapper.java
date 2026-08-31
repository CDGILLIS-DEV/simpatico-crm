package com.simpatico.crm.mapper;

import com.simpatico.crm.dto.BuyerBriefResponse;
import com.simpatico.crm.dto.LeadCreateRequest;
import com.simpatico.crm.dto.LeadResponse;
import com.simpatico.crm.dto.LeadUpdateRequest;
import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.entity.Lead;
import com.simpatico.crm.entity.LeadStatus;
import org.springframework.stereotype.Component;

/**
 * Component class to map between Lead entities and their input/output DTOs.
 */
@Component
public class LeadMapper {

    /**
     * Map a LeadCreateRequest payload and associated Buyer to a new Lead JPA Entity.
     */
    public Lead toEntity(LeadCreateRequest request, Buyer buyer) {
        if (request == null) {
            return null;
        }
        return Lead.builder()
                .buyer(buyer)
                .inventoryCategory(request.getInventoryCategory())
                .inventoryCondition(request.getInventoryCondition())
                .requestedQuantity(request.getRequestedQuantity())
                .budget(request.getBudget())
                .preferredGeographicArea(request.getPreferredGeographicArea())
                .purchaseFrequency(request.getPurchaseFrequency())
                .additionalRequirements(request.getAdditionalRequirements())
                .status(request.getStatus() != null ? request.getStatus() : LeadStatus.NEW)
                .source(request.getSource())
                .build();
    }

    /**
     * Update an existing Lead entity object's fields using data from a LeadUpdateRequest.
     */
    public void updateEntity(LeadUpdateRequest request, Lead entity) {
        if (request == null || entity == null) {
            return;
        }
        entity.setInventoryCategory(request.getInventoryCategory());
        entity.setInventoryCondition(request.getInventoryCondition());
        entity.setRequestedQuantity(request.getRequestedQuantity());
        entity.setBudget(request.getBudget());
        entity.setPreferredGeographicArea(request.getPreferredGeographicArea());
        entity.setPurchaseFrequency(request.getPurchaseFrequency());
        entity.setAdditionalRequirements(request.getAdditionalRequirements());
        entity.setStatus(request.getStatus());
        entity.setSource(request.getSource());
    }

    /**
     * Map a Lead entity object to a LeadResponse record.
     */
    public LeadResponse toResponse(Lead entity) {
        if (entity == null) {
            return null;
        }
        return new LeadResponse(
                entity.getId(),
                toBriefResponse(entity.getBuyer()),
                entity.getInventoryCategory(),
                entity.getInventoryCondition(),
                entity.getRequestedQuantity(),
                entity.getBudget(),
                entity.getPreferredGeographicArea(),
                entity.getPurchaseFrequency(),
                entity.getAdditionalRequirements(),
                entity.getStatus(),
                entity.getSource(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Map a Buyer entity to its brief representation.
     */
    public BuyerBriefResponse toBriefResponse(Buyer buyer) {
        if (buyer == null) {
            return null;
        }
        return new BuyerBriefResponse(
                buyer.getId(),
                buyer.getFirstName(),
                buyer.getLastName(),
                buyer.getCompanyName(),
                buyer.getEmail(),
                buyer.getPhone()
        );
    }
}
