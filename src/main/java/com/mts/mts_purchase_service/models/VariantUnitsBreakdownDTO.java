package com.mts.mts_purchase_service.models;

import java.math.BigDecimal;

/**
 * Drill-down variant unit totals under one product.
 */
public class VariantUnitsBreakdownDTO {

    private Long variantId;
    private String variantLabel;
    private BigDecimal totalUnits;

    /** Returns variant id. */
    public Long getVariantId() {
        return variantId;
    }

    /** Assigns variant id. */
    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    /** Returns variant label. */
    public String getVariantLabel() {
        return variantLabel;
    }

    /** Assigns variant label. */
    public void setVariantLabel(String variantLabel) {
        this.variantLabel = variantLabel;
    }

    /** Returns aggregated units for this variant. */
    public BigDecimal getTotalUnits() {
        return totalUnits;
    }

    /** Assigns aggregated units for this variant. */
    public void setTotalUnits(BigDecimal totalUnits) {
        this.totalUnits = totalUnits;
    }
}
