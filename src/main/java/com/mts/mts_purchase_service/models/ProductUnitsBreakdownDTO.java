package com.mts.mts_purchase_service.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product-level purchased units with nested variant drill-down.
 */
public class ProductUnitsBreakdownDTO {

    private Long productId;
    private String productName;
    private BigDecimal totalUnits;
    private List<VariantUnitsBreakdownDTO> variants = new ArrayList<>();

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

    /** Returns total purchased units for this product. */
    public BigDecimal getTotalUnits() {
        return totalUnits;
    }

    /** Assigns total purchased units for this product. */
    public void setTotalUnits(BigDecimal totalUnits) {
        this.totalUnits = totalUnits;
    }

    /** Returns variant-level breakdown list. */
    public List<VariantUnitsBreakdownDTO> getVariants() {
        return variants;
    }

    /** Assigns variant-level breakdown list. */
    public void setVariants(List<VariantUnitsBreakdownDTO> variants) {
        this.variants = variants;
    }
}
