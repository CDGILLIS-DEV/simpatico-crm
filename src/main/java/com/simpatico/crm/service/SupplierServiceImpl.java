package com.simpatico.crm.service;

import com.simpatico.crm.dto.SupplierCreateRequest;
import com.simpatico.crm.dto.SupplierResponse;
import com.simpatico.crm.dto.SupplierUpdateRequest;
import com.simpatico.crm.entity.Supplier;
import com.simpatico.crm.entity.SupplierStatus;
import com.simpatico.crm.exception.DuplicateResourceException;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.mapper.SupplierMapper;
import com.simpatico.crm.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service implementation managing Supplier business operations and validation checks.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponse createSupplier(SupplierCreateRequest request) {
        if (supplierRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Supplier with email '" + request.getEmail() + "' already exists");
        }
        Supplier supplier = supplierMapper.toEntity(request);
        Supplier saved = supplierRepository.save(supplier);
        return supplierMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getSupplierById(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID '" + id + "' not found"));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> listSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable).map(supplierMapper::toResponse);
    }

    @Override
    public SupplierResponse updateSupplier(UUID id, SupplierUpdateRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID '" + id + "' not found"));

        supplierRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new DuplicateResourceException("Supplier with email '" + request.getEmail() + "' already exists");
            }
        });

        supplierMapper.updateEntity(request, supplier);
        Supplier updated = supplierRepository.save(supplier);
        return supplierMapper.toResponse(updated);
    }

    @Override
    public void deactivateSupplier(UUID id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier with ID '" + id + "' not found"));
        supplier.setStatus(SupplierStatus.INACTIVE);
        supplierRepository.save(supplier);
    }
}
