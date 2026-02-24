package com.mts.mts_purchase_service.models;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Order item request/response DTO.
 */
public class OrderItemDTO {

    private Long itemId;

    @NotNull(message = "Variant id is required")
    private Long variantId;

    private String productName;
    private String variantLabel;
    private String unitAbbr;
    private BigDecimal packSize;
    private Integer piecesPerPack;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private BigDecimal quantity;

    @NotNull(message = "Rate per unit is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Rate per unit must be >= 0")
    private BigDecimal ratePerUnit;

    private BigDecimal lineTotal;
    private BigDecimal totalVolume;
    private String notes;

    /** Returns line item id. */
    public Long getItemId() {
        return itemId;
    }

    /** Assigns line item id. */
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    /** Returns variant id selected in this line. */
    public Long getVariantId() {
        return variantId;
    }

    /** Assigns variant id selected in this line. */
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    /** Returns product name for response display. */
    public String getProductName() {
        return productName;
    }

    /** Assigns product name for response display. */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /** Returns variant label for response display. */
    public String getVariantLabel() {
        return variantLabel;
    }

    /** Assigns variant label for response display. */
    public void setVariantLabel(String variantLabel) {
        this.variantLabel = variantLabel;
    }

    /** Returns unit abbreviation for response display. */
    public String getUnitAbbr() {
        return unitAbbr;
    }

    /** Assigns unit abbreviation for response display. */
    public void setUnitAbbr(String unitAbbr) {
        this.unitAbbr = unitAbbr;
    }

    /** Returns pack size for selected variant. */
    public BigDecimal getPackSize() {
        return packSize;
    }

    /** Assigns pack size for selected variant. */
    public void setPackSize(BigDecimal packSize) {
        this.packSize = packSize;
    }

    /** Returns pieces per pack for selected variant. */
    public Integer getPiecesPerPack() {
        return piecesPerPack;
    }

    /** Assigns pieces per pack for selected variant. */
    public void setPiecesPerPack(Integer piecesPerPack) {
        this.piecesPerPack = piecesPerPack;
    }

    /** Returns quantity in pack units. */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /** Assigns quantity in pack units. */
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    /** Returns rate per pack unit. */
    public BigDecimal getRatePerUnit() {
        return ratePerUnit;
    }

    /** Assigns rate per pack unit. */
    public void setRatePerUnit(BigDecimal ratePerUnit) {
        this.ratePerUnit = ratePerUnit;
    }

    /** Returns line total amount. */
    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    /** Assigns line total amount. */
    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    /** Returns derived total volume value. */
    public BigDecimal getTotalVolume() {
        return totalVolume;
    }

    /** Assigns derived total volume value. */
    public void setTotalVolume(BigDecimal totalVolume) {
        this.totalVolume = totalVolume;
    }

    /** Returns optional notes. */
    public String getNotes() {
        return notes;
    }

    /** Assigns optional notes. */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
