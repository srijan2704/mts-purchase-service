package com.mts.mts_purchase_service.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Product quantity ranking report payload for bar chart + drill-down.
 */
public class ProductUnitsTrendReportDTO {

    private LocalDate from;
    private LocalDate to;
    private List<ProductUnitsBreakdownDTO> products = new ArrayList<>();

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

    /** Returns product ranking with nested variant drill-down. */
    public List<ProductUnitsBreakdownDTO> getProducts() {
        return products;
    }

    /** Assigns product ranking with nested variant drill-down. */
    public void setProducts(List<ProductUnitsBreakdownDTO> products) {
        this.products = products;
    }
}
