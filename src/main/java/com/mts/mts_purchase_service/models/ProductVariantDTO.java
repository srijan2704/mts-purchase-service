package com.mts.mts_purchase_service.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for product variant data used in API requests/responses.
 */
public class ProductVariantDTO {

    private Long variantId;
    private Long productId;

    @NotNull(message = "Unit id is required")
    private Long unitId;

    private String unitName;

    @NotBlank(message = "Variant label is required")
    @Size(max = 100, message = "Variant label must not exceed 100 characters")
    private String variantLabel;

    @NotNull(message = "Pack size is required")
    @DecimalMin(value = "0.001", message = "Pack size must be greater than 0")
    private BigDecimal packSize;

    @NotNull(message = "Pieces per pack is required")
    @Min(value = 1, message = "Pieces per pack must be at least 1")
    private Integer piecesPerPack;

    @Size(max = 50, message = "Barcode must not exceed 50 characters")
    private String barcode;

    private Boolean active;

    public ProductVariantDTO() {
    }

    public ProductVariantDTO(Long variantId, Long productId, Long unitId, String unitName,
                             String variantLabel, BigDecimal packSize, Integer piecesPerPack,
                             String barcode, Boolean active) {
        this.variantId = variantId;
        this.productId = productId;
        this.unitId = unitId;
        this.unitName = unitName;
        this.variantLabel = variantLabel;
        this.packSize = packSize;
        this.piecesPerPack = piecesPerPack;
        this.barcode = barcode;
        this.active = active;
    }

    /** Returns variant id when available (response/update scenario). */
    public Long getVariantId() {
        return variantId;
    }

    /** Assigns variant id. */
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    /** Returns parent product id for this variant. */
    public Long getProductId() {
        return productId;
    }

    /** Assigns parent product id for this variant. */
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    /** Returns unit id selected for this packaging variant. */
    public Long getUnitId() {
        return unitId;
    }

    /** Assigns unit id selected for this packaging variant. */
    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    /** Returns human-readable unit name for response payloads. */
    public String getUnitName() {
        return unitName;
    }

    /** Assigns human-readable unit name for response payloads. */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /** Returns display label (for example: 1L x 16 Box). */
    public String getVariantLabel() {
        return variantLabel;
    }

    /** Assigns display label used in dropdowns and reports. */
    public void setVariantLabel(String variantLabel) {
        this.variantLabel = variantLabel;
    }

    /** Returns size of one piece in the chosen unit. */
    public BigDecimal getPackSize() {
        return packSize;
    }

    /** Assigns size of one piece in the chosen unit. */
    public void setPackSize(BigDecimal packSize) {
        this.packSize = packSize;
    }

    /** Returns number of pieces inside a pack/box. */
    public Integer getPiecesPerPack() {
        return piecesPerPack;
    }

    /** Assigns number of pieces inside a pack/box. */
    public void setPiecesPerPack(Integer piecesPerPack) {
        this.piecesPerPack = piecesPerPack;
    }

    /** Returns optional barcode for this variant. */
    public String getBarcode() {
        return barcode;
    }

    /** Assigns optional barcode for this variant. */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /** Returns whether the variant is active. */
    public Boolean isActive() {
        return active;
    }

    /** Assigns active flag for this variant. */
    public void setActive(Boolean active) {
        this.active = active;
    }
}
