package com.mts.mts_purchase_service.models;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One point in daily purchase trend graph.
 */
public class DailyPurchaseTrendPointDTO {

    private LocalDate date;
    private BigDecimal totalPurchase;

    /** Returns trend date. */
    public LocalDate getDate() {
        return date;
    }

    /** Assigns trend date. */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /** Returns aggregated total purchase for this date. */
    public BigDecimal getTotalPurchase() {
        return totalPurchase;
    }

    /** Assigns aggregated total purchase for this date. */
    public void setTotalPurchase(BigDecimal totalPurchase) {
        this.totalPurchase = totalPurchase;
    }
}
