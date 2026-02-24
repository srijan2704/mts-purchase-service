package com.mts.mts_purchase_service.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Daily report summary DTO.
 */
public class DailySummaryDTO {

    private LocalDate date;
    private long totalOrders;
    private BigDecimal totalSpend;

    // Keeps insertion order stable for API readability.
    private Map<String, BigDecimal> spendBySeller = new LinkedHashMap<>();
    private Map<String, BigDecimal> spendByProduct = new LinkedHashMap<>();
    private Map<String, BigDecimal> spendByVariant = new LinkedHashMap<>();

    /** Returns summary date. */
    public LocalDate getDate() {
        return date;
    }

    /** Assigns summary date. */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** Returns count of orders for the day. */
    public long getTotalOrders() {
        return totalOrders;
    }

    /** Assigns count of orders for the day. */
    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    /** Returns total spend for the day. */
    public BigDecimal getTotalSpend() {
        return totalSpend;
    }

    /** Assigns total spend for the day. */
    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend;
    }

    /** Returns per-seller spend map. */
    public Map<String, BigDecimal> getSpendBySeller() {
        return spendBySeller;
    }

    /** Assigns per-seller spend map. */
    public void setSpendBySeller(Map<String, BigDecimal> spendBySeller) {
        this.spendBySeller = spendBySeller;
    }

    /** Returns per-product spend map. */
    public Map<String, BigDecimal> getSpendByProduct() {
        return spendByProduct;
    }

    /** Assigns per-product spend map. */
    public void setSpendByProduct(Map<String, BigDecimal> spendByProduct) {
        this.spendByProduct = spendByProduct;
    }

    /** Returns per-variant spend map. */
    public Map<String, BigDecimal> getSpendByVariant() {
        return spendByVariant;
    }

    /** Assigns per-variant spend map. */
    public void setSpendByVariant(Map<String, BigDecimal> spendByVariant) {
        this.spendByVariant = spendByVariant;
    }
}
