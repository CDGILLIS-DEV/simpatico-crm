package com.simpatico.crm.mapper;

import com.simpatico.crm.dto.SupplierCreateRequest;
import com.simpatico.crm.dto.SupplierResponse;
import com.simpatico.crm.dto.SupplierUpdateRequest;
import com.simpatico.crm.dto.SupplierBriefResponse;
import com.simpatico.crm.entity.Supplier;
import com.simpatico.crm.entity.SupplierStatus;
import org.springframework.stereotype.Component;

/**
 * Component class to map between Supplier entities and their input/output DTOs.
 */
@Component
public class SupplierMapper {

    /**
     * Map a SupplierCreateRequest payload to a new Supplier JPA Entity.
     */
    public Supplier toEntity(SupplierCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Supplier.builder()
                .companyName(request.getCompanyName())
                .contactName(request.getContactName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry() != null && !request.getCountry().isBlank() ? request.getCountry() : "USA")
                .website(request.getWebsite())
                .status(request.getStatus() != null ? request.getStatus() : SupplierStatus.PENDING)
                .notes(request.getNotes())
                .build();
    }

    /**
     * Update an existing Supplier entity object's fields using data from a SupplierUpdateRequest.
     */
    public void updateEntity(SupplierUpdateRequest request, Supplier entity) {
        if (request == null || entity == null) {
            return;
        }
        entity.setCompanyName(request.getCompanyName());
        entity.setContactName(request.getContactName());
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setCity(request.getCity());
        entity.setState(request.getState());
        entity.setCountry(request.getCountry());
        entity.setWebsite(request.getWebsite());
        entity.setStatus(request.getStatus());
        entity.setNotes(request.getNotes());
    }

    /**
     * Map a Supplier entity object to a SupplierResponse record.
     */
    public SupplierResponse toResponse(Supplier entity) {
        if (entity == null) {
            return null;
        }
        return new SupplierResponse(
                entity.getId(),
                entity.getCompanyName(),
                entity.getContactName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.getCity(),
                entity.getState(),
                entity.getCountry(),
                entity.getWebsite(),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Map a Supplier entity to its brief representation.
     */
    public SupplierBriefResponse toBriefResponse(Supplier entity) {
        if (entity == null) {
            return null;
        }
        return new SupplierBriefResponse(
                entity.getId(),
                entity.getCompanyName(),
                entity.getContactName(),
                entity.getEmail(),
                entity.getPhone()
        );
    }
}
