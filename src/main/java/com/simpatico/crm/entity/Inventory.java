package com.simpatico.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the "inventory" table.
 * Represents an available wholesale liquidation offering from a Supplier.
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @NotBlank(message = "Title is required")
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Condition is required")
    @Column(name = "condition", nullable = false, length = 50)
    private String condition;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotBlank(message = "Unit type is required")
    @Column(name = "unit_type", nullable = false, length = 20)
    private String unitType;

    @NotNull(message = "Asking price is required")
    @Positive(message = "Asking price must be greater than zero")
    @Column(name = "asking_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal askingPrice;

    @Column(name = "location", length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false, length = 20)
    @Builder.Default
    private InventoryStatus availabilityStatus = InventoryStatus.AVAILABLE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
