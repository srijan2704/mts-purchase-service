package com.mts.mts_purchase_service.controller;

import com.mts.mts_purchase_service.models.ApiResponseDTO;
import com.mts.mts_purchase_service.models.DailyPurchaseTrendReportDTO;
import com.mts.mts_purchase_service.models.DailySummaryDTO;
import com.mts.mts_purchase_service.models.ProductUnitsTrendReportDTO;
import com.mts.mts_purchase_service.models.SellerHistoryReportDTO;
import com.mts.mts_purchase_service.models.TopSellersReportDTO;
import com.mts.mts_purchase_service.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller for reporting endpoints.
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Reports", description = "Read-only business reports.")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * Returns daily spend summary.
     */
    @GetMapping("/daily")
    @Operation(summary = "Get daily summary", description = "Returns total spend and line-level summary for a date using CONFIRMED orders only.")
    public ResponseEntity<ApiResponseDTO<DailySummaryDTO>> getDailySummary(@RequestParam(required = false) LocalDate date) {
        DailySummaryDTO summary = reportService.getDailySummary(date);
        return ResponseEntity.ok(ApiResponseDTO.success("Daily summary fetched successfully", summary));
    }

    /**
     * Returns daily total purchase trend for graph plotting.
     */
    @GetMapping("/trends/daily-purchase")
    @Operation(
            summary = "Get daily purchase trend",
            description = "Returns date-wise total purchase trend from CONFIRMED orders only. Default range is last 30 days if no dates are provided. Maximum selectable range is 3 months."
    )
    public ResponseEntity<ApiResponseDTO<DailyPurchaseTrendReportDTO>> getDailyPurchaseTrend(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        DailyPurchaseTrendReportDTO report = reportService.getDailyPurchaseTrend(from, to);
        return ResponseEntity.ok(ApiResponseDTO.success("Daily purchase trend fetched successfully", report));
    }

    /**
     * Returns product ranking by purchased units with variant drill-down.
     */
    @GetMapping("/trends/top-products")
    @Operation(
            summary = "Get top products by purchased units",
            description = "Returns products in descending order by total purchased units from CONFIRMED orders only, with nested variant-level drill-down. Default range is 1 month; maximum selectable range is 1 year."
    )
    public ResponseEntity<ApiResponseDTO<ProductUnitsTrendReportDTO>> getTopProductsByUnits(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        ProductUnitsTrendReportDTO report = reportService.getTopProductsByUnits(from, to);
        return ResponseEntity.ok(ApiResponseDTO.success("Top products by units fetched successfully", report));
    }

    /**
     * Returns seller ranking by total purchase value.
     */
    @GetMapping("/trends/top-sellers")
    @Operation(
            summary = "Get top sellers by purchase value",
            description = "Returns sellers in descending order by total purchase value from CONFIRMED orders only. Default range is 1 year; maximum selectable range is 1 year."
    )
    public ResponseEntity<ApiResponseDTO<TopSellersReportDTO>> getTopSellersByPurchase(
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        TopSellersReportDTO report = reportService.getTopSellersByPurchase(from, to);
        return ResponseEntity.ok(ApiResponseDTO.success("Top sellers by purchase fetched successfully", report));
    }

    /**
     * Returns one seller's order history for date range.
     */
    @GetMapping("/seller/{id}")
    @Operation(summary = "Get seller history report", description = "Returns seller-wise order history for a date range using CONFIRMED orders only.")
    public ResponseEntity<ApiResponseDTO<SellerHistoryReportDTO>> getSellerHistory(
            @PathVariable Long id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        SellerHistoryReportDTO report = reportService.getSellerHistory(id, from, to);
        return ResponseEntity.ok(ApiResponseDTO.success("Seller report fetched successfully", report));
    }
}
