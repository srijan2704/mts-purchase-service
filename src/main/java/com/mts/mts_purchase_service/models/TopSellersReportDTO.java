package com.mts.mts_purchase_service.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seller ranking report payload for bar chart.
 */
public class TopSellersReportDTO {

    private LocalDate from;
    private LocalDate to;
    private List<SellerPurchasePointDTO> sellers = new ArrayList<>();

    /** Returns report start date (inclusive). */
    public LocalDate getFrom() {
        return from;
    }

    /** Assigns report start date (inclusive). */
    public void setFrom(LocalDate from) {
        this.from = from;
    }

    /** Returns report end date (inclusive). */
    public LocalDate getTo() {
        return to;
    }

    /** Assigns report end date (inclusive). */
    public void setTo(LocalDate to) {
        this.to = to;
    }

    /** Returns seller ranking points. */
    public List<SellerPurchasePointDTO> getSellers() {
        return sellers;
    }

    /** Assigns seller ranking points. */
    public void setSellers(List<SellerPurchasePointDTO> sellers) {
        this.sellers = sellers;
    }
}
