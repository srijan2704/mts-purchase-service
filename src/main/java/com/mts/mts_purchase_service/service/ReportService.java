package com.mts.mts_purchase_service.service;

import com.mts.mts_purchase_service.models.DailyPurchaseTrendReportDTO;
import com.mts.mts_purchase_service.models.DailySummaryDTO;
import com.mts.mts_purchase_service.models.ProductUnitsTrendReportDTO;
import com.mts.mts_purchase_service.models.SellerHistoryReportDTO;
import com.mts.mts_purchase_service.models.TopSellersReportDTO;

import java.time.LocalDate;

/**
 * Reporting service contract.
 */
public interface ReportService {

    /** Returns aggregated daily spend summary. */
    DailySummaryDTO getDailySummary(LocalDate date);

    /** Returns daily total purchase trend for graph plotting by date range. */
    DailyPurchaseTrendReportDTO getDailyPurchaseTrend(LocalDate from, LocalDate to);

    /** Returns product-wise purchased units with variant drill-down by date range. */
    ProductUnitsTrendReportDTO getTopProductsByUnits(LocalDate from, LocalDate to);

    /** Returns seller-wise maximum purchase ranking by date range. */
    TopSellersReportDTO getTopSellersByPurchase(LocalDate from, LocalDate to);

    /** Returns seller-specific order history within a date range. */
    SellerHistoryReportDTO getSellerHistory(Long sellerId, LocalDate from, LocalDate to);
}
