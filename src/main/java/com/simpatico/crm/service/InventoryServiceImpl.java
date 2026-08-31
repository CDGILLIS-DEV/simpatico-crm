package com.simpatico.crm.service;

import com.simpatico.crm.dto.InventoryCreateRequest;
import com.simpatico.crm.dto.InventoryResponse;
import com.simpatico.crm.dto.InventorySearchCriteria;
import com.simpatico.crm.dto.InventoryUpdateRequest;
import com.simpatico.crm.entity.Inventory;
import com.simpatico.crm.entity.InventoryStatus;
import com.simpatico.crm.entity.Supplier;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.mapper.InventoryMapper;
import com.simpatico.crm.repository.InventoryRepository;
import com.simpatico.crm.repository.SupplierRepository;
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
 * Service implementation managing Inventory business rules, matching, and dynamic searching.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryResponse createInventory(InventoryCreateRequest request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID '" + request.getSupplierId() + "' not found"));

        Inventory inventory = inventoryMapper.toEntity(request, supplier);
        Inventory saved = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item with ID '" + id + "' not found"));
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> searchInventories(InventorySearchCriteria criteria, Pageable pageable) {
        Specification<Inventory> spec = buildSpecification(criteria);
        return inventoryRepository.findAll(spec, pageable).map(inventoryMapper::toResponse);
    }

    @Override
    public InventoryResponse updateInventory(UUID id, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item with ID '" + id + "' not found"));

        inventoryMapper.updateEntity(request, inventory);
        Inventory updated = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(updated);
    }

    @Override
    public void deactivateInventory(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item with ID '" + id + "' not found"));
        inventory.setAvailabilityStatus(InventoryStatus.INACTIVE);
        inventoryRepository.save(inventory);
    }

    /**
     * Builds dynamic JPA Specification filtering for category, condition, supplier, location, status, and prices.
     */
    private Specification<Inventory> buildSpecification(InventorySearchCriteria criteria) {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("supplier", jakarta.persistence.criteria.JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getCategory() != null && !criteria.getCategory().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("category")), "%" + criteria.getCategory().toLowerCase() + "%"));
            }
            if (criteria.getCondition() != null && !criteria.getCondition().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("condition")), criteria.getCondition().toLowerCase()));
            }
            if (criteria.getSupplierId() != null) {
                predicates.add(cb.equal(root.get("supplier").get("id"), criteria.getSupplierId()));
            }
            if (criteria.getLocation() != null && !criteria.getLocation().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + criteria.getLocation().toLowerCase() + "%"));
            }
            if (criteria.getAvailability() != null) {
                predicates.add(cb.equal(root.get("availabilityStatus"), criteria.getAvailability()));
            }
            if (criteria.getMinPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("askingPrice"), criteria.getMinPrice()));
            }
            if (criteria.getMaxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("askingPrice"), criteria.getMaxPrice()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
