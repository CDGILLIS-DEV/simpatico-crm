package com.simpatico.crm.dto;

import com.simpatico.crm.entity.InventoryStatus;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Filter criteria class containing parameters used to search for Inventory dynamically.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySearchCriteria {

    private String category;

    private String condition;

    private UUID supplierId;

    private String location;

    private InventoryStatus availability;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;
}
