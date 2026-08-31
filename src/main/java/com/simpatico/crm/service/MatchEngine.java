package com.simpatico.crm.service;

import com.simpatico.crm.entity.Inventory;
import com.simpatico.crm.entity.InventoryStatus;
import com.simpatico.crm.entity.Lead;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Modular rule-based matching engine comparing Lead targets with Inventory specs.
 * Rules assess Category, Condition, Budget caps, Quantity levels, and Location scores.
 */
@Component
public class MatchEngine {

    public static final double MIN_MATCH_THRESHOLD = 0.75;

    /**
     * Determines whether a Lead and Inventory record qualify as a potential Match.
     */
    public boolean isMatch(Lead lead, Inventory inventory) {
        return calculateScore(lead, inventory) >= MIN_MATCH_THRESHOLD;
    }

    /**
     * Calculates compatibility score (0.0 to 1.0).
     */
    public double calculateScore(Lead lead, Inventory inventory) {
        if (lead == null || inventory == null) {
            return 0.0;
        }

        // Only match against AVAILABLE inventory offerings
        if (inventory.getAvailabilityStatus() != InventoryStatus.AVAILABLE) {
            return 0.0;
        }

        // 1. Inventory Category (Mandatory exact case-insensitive match)
        if (lead.getInventoryCategory() == null || inventory.getCategory() == null ||
                !lead.getInventoryCategory().equalsIgnoreCase(inventory.getCategory())) {
            return 0.0;
        }

        // 2. Inventory Condition (Mandatory exact case-insensitive match)
        if (lead.getInventoryCondition() == null || inventory.getCondition() == null ||
                !lead.getInventoryCondition().equalsIgnoreCase(inventory.getCondition())) {
            return 0.0;
        }

        // 3. Price vs Budget (Mandatory check - price cannot exceed lead budget)
        double priceScore = 1.0;
        if (lead.getBudget() != null) {
            BigDecimal budget = lead.getBudget();
            BigDecimal askingPrice = inventory.getAskingPrice();
            if (askingPrice != null) {
                if (askingPrice.compareTo(budget) > 0) {
                    return 0.0;
                }
            }
        }

        // 4. Quantity (Mandatory check - inventory quantity must be >= 70% of requested quantity)
        double quantityScore = 1.0;
        if (lead.getRequestedQuantity() != null && lead.getRequestedQuantity() > 0) {
            int requested = lead.getRequestedQuantity();
            int available = inventory.getQuantity() != null ? inventory.getQuantity() : 0;
            if (available < requested * 0.70) {
                return 0.0;
            } else if (available < requested) {
                quantityScore = (double) available / requested;
            }
        }

        // 5. Location Preference (Optional factor - score boost if location matches preference)
        double geoScore = 1.0;
        if (lead.getPreferredGeographicArea() != null && !lead.getPreferredGeographicArea().isBlank() &&
                inventory.getLocation() != null && !inventory.getLocation().isBlank()) {
            String preference = lead.getPreferredGeographicArea().toLowerCase();
            String location = inventory.getLocation().toLowerCase();
            if (!location.contains(preference) && !preference.contains(location)) {
                geoScore = 0.8;
            }
        }

        // Composite scoring from price, quantity, and location factors
        return (priceScore + quantityScore + geoScore) / 3.0;
    }
}
