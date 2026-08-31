package com.simpatico.crm.service;

import com.simpatico.crm.entity.Inventory;
import com.simpatico.crm.entity.InventoryStatus;
import com.simpatico.crm.entity.Lead;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MatchEngineTests {

    private final MatchEngine matchEngine = new MatchEngine();

    @Test
    void shouldMatchWhenAllCriteriaAlignExactly() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(100)
                .budget(new BigDecimal("10000.00"))
                .preferredGeographicArea("New York")
                .build();

        Inventory inventory = Inventory.builder()
                .category("Electronics")
                .condition("Returns")
                .quantity(100)
                .askingPrice(new BigDecimal("9000.00"))
                .location("New York")
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        assertTrue(matchEngine.isMatch(lead, inventory));
        assertEquals(1.0, matchEngine.calculateScore(lead, inventory), 0.001);
    }

    @Test
    void shouldFailMatchWhenCategoryDoesNotMatch() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .build();

        Inventory inventory = Inventory.builder()
                .category("CLOTHING")
                .condition("RETURNS")
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        assertFalse(matchEngine.isMatch(lead, inventory));
        assertEquals(0.0, matchEngine.calculateScore(lead, inventory), 0.001);
    }

    @Test
    void shouldFailMatchWhenConditionDoesNotMatch() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("NEW")
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        assertFalse(matchEngine.isMatch(lead, inventory));
        assertEquals(0.0, matchEngine.calculateScore(lead, inventory), 0.001);
    }

    @Test
    void shouldFailMatchWhenAskingPriceExceedsBudget() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .budget(new BigDecimal("10000.00"))
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("RETURNS")
                .askingPrice(new BigDecimal("10001.00"))
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        assertFalse(matchEngine.isMatch(lead, inventory));
        assertEquals(0.0, matchEngine.calculateScore(lead, inventory), 0.001);
    }

    @Test
    void shouldMatchWhenAskingPriceIsExactlyBudget() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .budget(new BigDecimal("10000.00"))
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("RETURNS")
                .askingPrice(new BigDecimal("10000.00"))
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        assertTrue(matchEngine.isMatch(lead, inventory));
    }

    @Test
    void shouldFailMatchWhenQuantityIsBelowSeventyPercent() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(100)
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("RETURNS")
                .quantity(69) // 69% of requested
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        assertFalse(matchEngine.isMatch(lead, inventory));
        assertEquals(0.0, matchEngine.calculateScore(lead, inventory), 0.001);
    }

    @Test
    void shouldMatchWhenQuantityIsExactlySeventyPercent() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .requestedQuantity(100)
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("RETURNS")
                .quantity(70)
                .askingPrice(new BigDecimal("5000.00"))
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        // composited score: (price=1.0 + quantity=0.7 + geo=1.0) / 3 = 0.90 -> matches (> 0.75)
        assertTrue(matchEngine.isMatch(lead, inventory));
        assertEquals(0.90, matchEngine.calculateScore(lead, inventory), 0.001);
    }

    @Test
    void shouldReduceScoreButMatchWhenLocationDiffers() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .preferredGeographicArea("Texas")
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("RETURNS")
                .location("California")
                .availabilityStatus(InventoryStatus.AVAILABLE)
                .build();

        // composited score: (price=1.0 + quantity=1.0 + geo=0.8) / 3 = 0.933 -> matches
        assertTrue(matchEngine.isMatch(lead, inventory));
        assertEquals(0.933, matchEngine.calculateScore(lead, inventory), 0.005);
    }

    @Test
    void shouldFailMatchWhenInventoryIsNotAvailable() {
        Lead lead = Lead.builder()
                .inventoryCategory("ELECTRONICS")
                .inventoryCondition("RETURNS")
                .build();

        Inventory inventory = Inventory.builder()
                .category("ELECTRONICS")
                .condition("RETURNS")
                .availabilityStatus(InventoryStatus.RESERVED)
                .build();

        assertFalse(matchEngine.isMatch(lead, inventory));
        assertEquals(0.0, matchEngine.calculateScore(lead, inventory), 0.001);
    }
}
