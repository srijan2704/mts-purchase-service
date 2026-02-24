package com.mts.mts_purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Product variant entity that models packaging configurations per product.
 */
@Entity
@Table(name = "PRODUCT_VARIANTS")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "VARIANT_ID")
    private Long variantId;

    // Many variants belong to one product.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    // Unit lookup (kg, L, pcs, etc.) for this variant.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UNIT_ID", nullable = false)
    private Unit unit;

    @Column(name = "VARIANT_LABEL", nullable = false, length = 100)
    private String variantLabel;

    @Column(name = "PACK_SIZE", nullable = false, precision = 10, scale = 3)
    private BigDecimal packSize;

    @Column(name = "PIECES_PER_PACK", nullable = false)
    private Integer piecesPerPack = 1;

    @Column(name = "BARCODE", length = 50)
    private String barcode;

    // Oracle boolean pattern via NUMBER(1,0): 1 active, 0 inactive.
    @Column(name = "IS_ACTIVE", nullable = false)
    private int isActive = 1;

    /** Required by JPA. */
    public ProductVariant() {
    }

    /** Returns variant id. */
    public Long getVariantId() {
        return variantId;
    }

    /** Assigns variant id. */
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    /** Returns parent product relation. */
    public Product getProduct() {
        return product;
    }

    /** Assigns parent product relation. */
    public void setProduct(Product product) {
        this.product = product;
    }

    /** Returns unit relation. */
    public Unit getUnit() {
        return unit;
    }

    /** Assigns unit relation. */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /** Returns variant label. */
    public String getVariantLabel() {
        return variantLabel;
    }

    /** Assigns variant label. */
    public void setVariantLabel(String variantLabel) {
        this.variantLabel = variantLabel;
    }

    /** Returns pack size for each piece. */
    public BigDecimal getPackSize() {
        return packSize;
    }

    /** Assigns pack size for each piece. */
    public void setPackSize(BigDecimal packSize) {
        this.packSize = packSize;
    }

    /** Returns pieces count inside one pack. */
    public Integer getPiecesPerPack() {
        return piecesPerPack;
    }

    /** Assigns pieces count inside one pack. */
    public void setPiecesPerPack(Integer piecesPerPack) {
        this.piecesPerPack = piecesPerPack;
    }

    /** Returns optional barcode. */
    public String getBarcode() {
        return barcode;
    }

    /** Assigns optional barcode. */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /** Returns raw numeric active status from Oracle. */
    public int getIsActive() {
        return isActive;
    }

    /** Assigns raw numeric active status from Oracle. */
    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    /** Returns true when variant is active. */
    public boolean isActive() {
        return this.isActive == 1;
    }

    /** Converts boolean active status to Oracle numeric representation. */
    public void setActive(boolean active) {
        this.isActive = active ? 1 : 0;
    }
}
