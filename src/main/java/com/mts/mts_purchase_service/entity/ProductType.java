package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Product type lookup entity.
 */
@Entity
@Table(name = "PRODUCT_TYPES")
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TYPE_ID")
    private Long typeId;

    @Column(name = "TYPE_NAME", nullable = false, length = 100, unique = true)
    private String typeName;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    /** Required by JPA. */
    public ProductType() {
    }

    /** Convenience constructor used in tests and mapping flows. */
    public ProductType(Long typeId, String typeName, String description) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.description = description;
    }

    /** Returns primary key id. */
    public Long getTypeId() {
        return typeId;
    }

    /** Assigns primary key id. */
    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    /** Returns product type display name. */
    public String getTypeName() {
        return typeName;
    }

    /** Assigns product type display name. */
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /** Returns optional description text. */
    public String getDescription() {
        return description;
    }

    /** Assigns optional description text. */
    public void setDescription(String description) {
        this.description = description;
    }
}
