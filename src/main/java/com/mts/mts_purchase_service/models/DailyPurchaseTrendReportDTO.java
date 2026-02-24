package com.mts.mts_purchase_service.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Date-range report payload for daily total purchase trend graph.
 */
public class DailyPurchaseTrendReportDTO {

    private LocalDate from;
    private LocalDate to;
    private List<DailyPurchaseTrendPointDTO> points = new ArrayList<>();

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

    /** Returns all daily graph points. */
    public List<DailyPurchaseTrendPointDTO> getPoints() {
        return points;
    }

    /** Assigns all daily graph points. */
    public void setPoints(List<DailyPurchaseTrendPointDTO> points) {
        this.points = points;
    }
}
