package com.simpatico.crm.mapper;

import com.simpatico.crm.dto.MatchResponse;
import com.simpatico.crm.entity.MatchRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component class to map between Match entities and their output DTOs.
 */
@Component
@RequiredArgsConstructor
public class MatchMapper {

    private final LeadMapper leadMapper;
    private final InventoryMapper inventoryMapper;

    /**
     * Map a MatchRecord entity object to a MatchResponse record.
     */
    public MatchResponse toResponse(MatchRecord entity) {
        if (entity == null) {
            return null;
        }
        return new MatchResponse(
                entity.getId(),
                leadMapper.toResponse(entity.getLead()),
                inventoryMapper.toResponse(entity.getInventory()),
                entity.getStatus(),
                entity.getNotes(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
