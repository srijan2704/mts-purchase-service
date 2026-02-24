package com.mts.mts_purchase_service.models;

import java.math.BigDecimal;

/**
 * Seller-wise purchase aggregate point.
 */
public class SellerPurchasePointDTO {

    private Long sellerId;
    private String sellerName;
    private BigDecimal totalPurchase;

    /** Returns seller id. */
    public Long getSellerId() {
        return sellerId;
    }

    /** Assigns seller id. */
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    /** Returns seller name. */
    public String getSellerName() {
        return sellerName;
    }

    /** Assigns seller name. */
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    /** Returns total purchase amount for the seller. */
    public BigDecimal getTotalPurchase() {
        return totalPurchase;
    }

    /** Assigns total purchase amount for the seller. */
    public void setTotalPurchase(BigDecimal totalPurchase) {
        this.totalPurchase = totalPurchase;
    }
}
