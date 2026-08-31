package com.simpatico.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * JPA Entity mapping the "lead" table.
 * Represents a purchasing request/opportunity submitted by a buyer.
 */
@Entity
@Table(name = "lead")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    @NotNull(message = "Buyer is required")
    private Buyer buyer;

    @NotBlank(message = "Inventory category is required")
    @Column(name = "inventory_category", nullable = false, length = 50)
    private String inventoryCategory;

    @NotBlank(message = "Inventory condition is required")
    @Column(name = "inventory_condition", nullable = false, length = 50)
    private String inventoryCondition;

    @Column(name = "requested_quantity")
    private Integer requestedQuantity;

    @Column(name = "budget", precision = 12, scale = 2)
    private BigDecimal budget;

    @Column(name = "preferred_geographic_area", length = 100)
    private String preferredGeographicArea;

    @Column(name = "purchase_frequency", length = 50)
    private String purchaseFrequency;

    @Column(name = "additional_requirements", columnDefinition = "TEXT")
    private String additionalRequirements;

    @NotNull(message = "Lead status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private LeadStatus status;

    @NotNull(message = "Lead source is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private LeadSource source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
