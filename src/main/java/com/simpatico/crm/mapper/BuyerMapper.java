package com.simpatico.crm.mapper;

import com.simpatico.crm.dto.BuyerCreateRequest;
import com.simpatico.crm.dto.BuyerResponse;
import com.simpatico.crm.dto.BuyerUpdateRequest;
import com.simpatico.crm.entity.Buyer;
import org.springframework.stereotype.Component;

/**
 * Component class to map between Buyer entities and their input/output DTOs.
 */
@Component
public class BuyerMapper {

    /**
     * Map a BuyerCreateRequest payload to a new Buyer JPA Entity.
     */
    public Buyer toEntity(BuyerCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Buyer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .companyName(request.getCompanyName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry() != null && !request.getCountry().isBlank() ? request.getCountry() : "USA")
                .active(true)
                .build();
    }

    /**
     * Update an existing Buyer entity object's fields using data from a BuyerUpdateRequest.
     */
    public void updateEntity(BuyerUpdateRequest request, Buyer entity) {
        if (request == null || entity == null) {
            return;
        }
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setCompanyName(request.getCompanyName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setAddressLine1(request.getAddressLine1());
        entity.setAddressLine2(request.getAddressLine2());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setZipCode(request.getZipCode());
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            entity.setCountry(request.getCountry());
        }
    }

    /**
     * Map a Buyer entity object to a BuyerResponse record.
     */
    public BuyerResponse toResponse(Buyer entity) {
        if (entity == null) {
            return null;
        }
        return new BuyerResponse(
                entity.getId(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getCompanyName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getAddressLine1(),
                entity.getAddressLine2(),
                entity.getCity(),
                entity.getState(),
                entity.getZipCode(),
                entity.getCountry(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
