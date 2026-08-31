package com.simpatico.crm.mapper;

import com.simpatico.crm.dto.InventoryCreateRequest;
import com.simpatico.crm.dto.InventoryResponse;
import com.simpatico.crm.dto.InventoryUpdateRequest;
import com.simpatico.crm.entity.Inventory;
import com.simpatico.crm.entity.InventoryStatus;
import com.simpatico.crm.entity.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component class to map between Inventory entities and their input/output DTOs.
 */
@Component
@RequiredArgsConstructor
public class InventoryMapper {

    private final SupplierMapper supplierMapper;

    /**
     * Map an InventoryCreateRequest payload and associated Supplier to a new Inventory JPA Entity.
     */
    public Inventory toEntity(InventoryCreateRequest request, Supplier supplier) {
        if (request == null) {
            return null;
        }
        return Inventory.builder()
                .supplier(supplier)
                .title(request.getTitle())
                .category(request.getCategory())
                .condition(request.getCondition())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .unitType(request.getUnitType())
                .askingPrice(request.getAskingPrice())
                .location(request.getLocation())
                .availabilityStatus(request.getAvailabilityStatus() != null ? request.getAvailabilityStatus() : InventoryStatus.AVAILABLE)
                .build();
    }

    /**
     * Update an existing Inventory entity object's fields using data from an InventoryUpdateRequest.
     */
    public void updateEntity(InventoryUpdateRequest request, Inventory entity) {
        if (request == null || entity == null) {
            return;
        }
        entity.setTitle(request.getTitle());
        entity.setCategory(request.getCategory());
        entity.setCondition(request.getCondition());
        entity.setDescription(request.getDescription());
        entity.setQuantity(request.getQuantity());
        entity.setUnitType(request.getUnitType());
        entity.setAskingPrice(request.getAskingPrice());
        entity.setLocation(request.getLocation());
        entity.setAvailabilityStatus(request.getAvailabilityStatus());
    }

    /**
     * Map an Inventory entity object to an InventoryResponse record.
     */
    public InventoryResponse toResponse(Inventory entity) {
        if (entity == null) {
            return null;
        }
        return new InventoryResponse(
                entity.getId(),
                supplierMapper.toBriefResponse(entity.getSupplier()),
                entity.getTitle(),
                entity.getCategory(),
                entity.getCondition(),
                entity.getDescription(),
                entity.getQuantity(),
                entity.getUnitType(),
                entity.getAskingPrice(),
                entity.getLocation(),
                entity.getAvailabilityStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
