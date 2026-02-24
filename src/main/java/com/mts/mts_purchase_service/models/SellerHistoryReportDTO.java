package com.mts.mts_purchase_service.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seller history report DTO for date-range queries.
 */
public class SellerHistoryReportDTO {

    private Long sellerId;
    private String sellerName;
    private LocalDate from;
    private LocalDate to;
    private long totalOrders;
    private BigDecimal totalSpend;
    private List<PurchaseOrderDTO> orders = new ArrayList<>();

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

    /** Returns report start date. */
    public LocalDate getFrom() {
        return from;
    }

    /** Assigns report start date. */
    public void setFrom(LocalDate from) {
        this.from = from;
    }

    /** Returns report end date. */
    public LocalDate getTo() {
        return to;
    }

    /** Assigns report end date. */
    public void setTo(LocalDate to) {
        this.to = to;
    }

    /** Returns total orders count in range. */
    public long getTotalOrders() {
        return totalOrders;
    }

    /** Assigns total orders count in range. */
    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    /** Returns total spend in range. */
    public BigDecimal getTotalSpend() {
        return totalSpend;
    }

    /** Assigns total spend in range. */
    public void setTotalSpend(BigDecimal totalSpend) {
        this.totalSpend = totalSpend;
    }

    /** Returns detailed order list. */
    public List<PurchaseOrderDTO> getOrders() {
        return orders;
    }

    /** Assigns detailed order list. */
    public void setOrders(List<PurchaseOrderDTO> orders) {
        this.orders = orders;
    }
}
