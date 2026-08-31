package com.simpatico.crm.service;

import com.simpatico.crm.dto.BuyerCreateRequest;
import com.simpatico.crm.dto.BuyerResponse;
import com.simpatico.crm.dto.BuyerUpdateRequest;
import com.simpatico.crm.entity.Buyer;
import com.simpatico.crm.exception.DuplicateResourceException;
import com.simpatico.crm.exception.ResourceNotFoundException;
import com.simpatico.crm.mapper.BuyerMapper;
import com.simpatico.crm.repository.BuyerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for managing Buyer business operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;
    private final BuyerMapper buyerMapper;

    @Override
    public BuyerResponse createBuyer(BuyerCreateRequest request) {
        if (buyerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Buyer with email '" + request.getEmail() + "' already exists");
        }
        Buyer buyer = buyerMapper.toEntity(request);
        Buyer saved = buyerRepository.save(buyer);
        return buyerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BuyerResponse getBuyerById(UUID id) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer with ID '" + id + "' not found"));
        return buyerMapper.toResponse(buyer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BuyerResponse> getAllBuyers(Pageable pageable) {
        return buyerRepository.findAll(pageable)
                .map(buyerMapper::toResponse);
    }

    @Override
    public BuyerResponse updateBuyer(UUID id, BuyerUpdateRequest request) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer with ID '" + id + "' not found"));

        // If email is being changed, verify uniqueness
        if (!buyer.getEmail().equalsIgnoreCase(request.getEmail())) {
            Optional<Buyer> existing = buyerRepository.findByEmail(request.getEmail());
            if (existing.isPresent()) {
                throw new DuplicateResourceException("Buyer with email '" + request.getEmail() + "' already exists");
            }
        }

        buyerMapper.updateEntity(request, buyer);
        Buyer updated = buyerRepository.save(buyer);
        return buyerMapper.toResponse(updated);
    }

    @Override
    public void deactivateBuyer(UUID id) {
        Buyer buyer = buyerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer with ID '" + id + "' not found"));
        buyer.setActive(false);
        buyerRepository.save(buyer);
    }
}
