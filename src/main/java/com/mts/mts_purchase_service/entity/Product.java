package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Product master entity. Variants are modeled in a separate table.
 */
@Entity
@Table(name = "PRODUCTS")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "PRODUCT_NAME", nullable = false, length = 150)
    private String productName;

    // Many products can map to one product type lookup entry.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID", nullable = false)
    private ProductType type;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

    // Oracle boolean pattern via NUMBER(1,0): 1 active, 0 inactive.
    @Column(name = "IS_ACTIVE", nullable = false)
    private int isActive = 1;

    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    /** Required by JPA. */
    public Product() {
    }

    /** Initializes create-time defaults when persisting new product. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.isActive = 1;
    }

    /** Returns product id. */
    public Long getProductId() {
        return productId;
    }

    /** Assigns product id. */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /** Returns product name. */
    public String getProductName() {
        return productName;
    }

    /** Assigns product name. */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /** Returns product type relationship. */
    public ProductType getType() {
        return type;
    }

    /** Assigns product type relationship. */
    public void setType(ProductType type) {
        this.type = type;
    }

    /** Returns optional description. */
    public String getDescription() {
        return description;
    }

    /** Assigns optional description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Returns raw numeric active status from Oracle. */
    public int getIsActive() {
        return isActive;
    }

    /** Assigns raw numeric active status from Oracle. */
    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    /** Returns true when product is active. */
    public boolean isActive() {
        return this.isActive == 1;
    }

    /** Converts boolean active status to Oracle numeric representation. */
    public void setActive(boolean active) {
        this.isActive = active ? 1 : 0;
    }

    /** Returns create timestamp. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Assigns create timestamp. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
